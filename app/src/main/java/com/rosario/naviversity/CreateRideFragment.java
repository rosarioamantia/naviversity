package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateRideFragment extends Fragment {
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;
    List<Place> listStart;
    List<Place> listStop;
    Place start;
    BottomSheetDialog dialog;
    Place stop;
    DatePickerDialog datePicker;
    TextInputEditText timeText;
    TimePickerDialog timePicker;
    TextInputEditText dateText;
    Button btnConfirm;
    User user;
    FusedLocationProviderClient fusedLocationClient;
    Location currentLocation;
    private OnMapReadyCallback mapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            Toast.makeText(getContext(), "Scegli punto di partenza", Toast.LENGTH_SHORT).show();
            getPlaces(googleMap);
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(false);
            }

            LatLng currentLatLang = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(currentLatLang,14, 1, 1)));
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    Place selectedPlace = getSelectedPlace(marker);
                    if (isStart(selectedPlace)) {
                        start = selectedPlace;
                        googleMap.clear();
                        showStopPlaces(googleMap);
                    } else {
                        stop = selectedPlace;
                        View confirmRideCreationView = getLayoutInflater().inflate(R.layout.confirm_ride_creation_dialog, null, false);
                        showConfirmRideCreationDialog(confirmRideCreationView, googleMap);
                    }
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLastLocation();
    }
    private void getLastLocation(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.ACCESS_COARSE_LOCATION*/};

        ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);

                    if (fineLocationGranted) {
                        LocationRequest lr = LocationRequest.create();
                        lr.setInterval(100);
                        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        LocationCallback lc = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                            }
                        };

                        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                        fusedLocationClient.requestLocationUpdates(lr, lc, Looper.getMainLooper());
                        Task<Location> task = fusedLocationClient.getLastLocation();
                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null){
                                    currentLocation = location;
                                    reloadGoogleMap(null);
                                }else{
                                    Toast.makeText(getContext(), "Devi attivare la localizzazione per creare una corsa", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Devi accettare i permessi alla posizione per creare una corsa", Toast.LENGTH_SHORT).show();
                    }
                });
        requestPermissionLauncher.launch(permissions);
    }

    public void reloadGoogleMap(GoogleMap googleMap){
        if(googleMap != null){
            googleMap.clear();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapCallback);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_ride, container, false);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        listStart = new ArrayList<>();
        listStop = new ArrayList<>();

        dbReference.child("user").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                user.setId(snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Non puoi eseguire questa operazione", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        });

        return view;
    }

    public void showStopPlaces(GoogleMap googleMap) {
        Toast.makeText(getContext(), "Scegli punto di arrivo", Toast.LENGTH_SHORT).show();
        for (Place stop : listStop) {
            showMarker(googleMap, stop, "arrivo");
        }
    }

    private void getPlaces(GoogleMap googleMap) {
        dbReference.child("place").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Place place = child.getValue(Place.class);
                        if (isStart(place)) {
                            listStart.add(place);
                            showMarker(googleMap, place, "partenza");
                        } else {
                            listStop.add(place);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Non puoi eseguire questa operazione", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private boolean isStart(Place place){
        return place.getType().equals("START");
    }

    private Place getSelectedPlace(Marker marker){
        LatLng markerPosition = marker.getPosition();
        double markerLatitude = markerPosition.latitude;
        double markerLongitude = markerPosition.longitude;
        for(Place place : listStart){
            if(place.getLatitude() == markerLatitude && place.getLongitude() == markerLongitude){
                return place;
            }
        }
        for(Place place : listStop){
            if(place.getLatitude() == markerLatitude && place.getLongitude() == markerLongitude){
                return place;
            }
        }
        return null;
    }

    private void showMarker(GoogleMap googleMap, Place place, String placeType){
        double lLatitude = place.getLatitude();
        double lLongitude = place.getLongitude();
        LatLng placePosition = new LatLng(lLatitude, lLongitude);
        googleMap.addMarker(new MarkerOptions().position(placePosition).title(place.getName())).setSnippet("Tocca qui per selezionare come punto di " + placeType);
    }

    public boolean checkDateTime(View confirmRideCreationView){
        TextInputLayout dateInputLayout = confirmRideCreationView.findViewById(R.id.dateInputLayout);
        TextInputLayout timeInputLayout = confirmRideCreationView.findViewById(R.id.timeInputLayout);
        timeInputLayout.setErrorIconDrawable(null);
        dateInputLayout.setErrorIconDrawable(null);

        if(dateText.getText().toString().matches("")){
            dateInputLayout.setError("Inserisci un valore");
            dateInputLayout.setErrorIconDrawable(null);
            return false;
        }else {
            if (dateInputLayout.getError() != null) {
                dateInputLayout.setError(null);
            }
        }
        if(timeText.getText().toString().matches("")){
            timeInputLayout.setError("Inserisci un valore");
            timeInputLayout.setErrorIconDrawable(null);
            return false;
        }else{
            if (timeInputLayout.getError() != null) {
                timeInputLayout.setError(null);
            }
        }
        if(!checkFutureDateTime(dateInputLayout, timeInputLayout)){
            return false;
        }
        return true;
    }

    public boolean checkFutureDateTime(TextInputLayout dateInputLayout, TextInputLayout timeInputLayout){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        LocalDate selectedDate = LocalDate.parse(dateText.getText().toString(), dateFormatter);
        LocalTime selectedTime = LocalTime.parse(timeText.getText().toString());
        LocalDate actualDate = LocalDate.now();
        LocalTime actualTime = LocalTime.now();
        if(selectedDate.isAfter(actualDate)) {
            return true;
        }else if(selectedDate.isEqual(actualDate)){
            if(selectedTime.isAfter(actualTime)) {
                return true;
            }else{
                timeInputLayout.setError("Orario non valido");
            }
        }else{
            dateInputLayout.setError("Data non valida");
        }
        return false;
    }

    private void showConfirmRideCreationDialog(View confirmRideCreationView, GoogleMap googleMap){
        dialog = new BottomSheetDialog(getContext());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                reloadGoogleMap(googleMap);
            }
        });
        dialog.setContentView(confirmRideCreationView);
        dateText = confirmRideCreationView.findViewById(R.id.rideDate);
        timeText = confirmRideCreationView.findViewById(R.id.rideTime);
        fillDialogData(confirmRideCreationView);
        dialog.show();
        btnConfirm = confirmRideCreationView.findViewById(R.id.btnConfirm);

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dateText.setText(day + "/" + (month+1) + "/" + year);
                    }
                }, year, month, day);
                datePicker.show();
            }
        });

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalTime time = LocalTime.now();
                timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                        timeText.setText(String.format("%02d:%02d", hours, minutes) );
                    }
                }, time.getHour(), time.getMinute(), true);
                timePicker.show();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(checkDateTime(confirmRideCreationView)){ //TODO cambia per aggiungere controllo corretto sui valori di date e time
                if(true){
                    if(user != null && user.isCarOwner()){
                        writeNewRide();
                        dialog.hide();
                        reloadGoogleMap(googleMap);
                    }else{
                        Toast.makeText(getContext(), "Prima ti serve un'automobile", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void fillDialogData(View view){
        TextView startTxt = view.findViewById(R.id.rideStart);
        TextView stopTxt = view.findViewById(R.id.rideStop);
        startTxt.setText(start.getName());
        stopTxt.setText(stop.getName());
    }

    private void writeNewRide(){
        String key = dbReference.child("ride").push().getKey();
        Car car = user.getCar();
        HashMap<String, User> members = new HashMap<>();
        members.put(user.getId(), user);
        Ride ride = new Ride(start, stop, user.getId(), dateText.getText().toString(), timeText.getText().toString(), car, members);

        Map<String, Object> rideValues = ride.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        //one put -> one update in a different table
        childUpdates.put("/ride/" + key, rideValues);
        //childUpdates.put("/user/ride_subscribed/" + key, rideValues);

        dbReference.updateChildren(childUpdates);
        Toast.makeText(getContext(), "Corsa creata correttamente", Toast.LENGTH_SHORT).show();
    }
}