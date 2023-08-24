package com.rosario.naviversity;

import static android.content.Context.LOCATION_SERVICE;

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
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.time.LocalTime;
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
    LocationRequest mLocationRequest;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            Toast.makeText(getContext(), "Scegli punto di partenza", Toast.LENGTH_SHORT).show();
            getPlaces(googleMap);
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        LatLng currentLatLang = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(currentLatLang,14, 1, 1)));
                    }else{
                        Toast.makeText(getContext(), "Location null", Toast.LENGTH_SHORT).show();
                    }

                }
            });
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
                        googleMap.clear();
                        View confirmRideCreationView = getLayoutInflater().inflate(R.layout.confirm_ride_creation_dialog, null, false);
                        showConfirmRideCreationDialog(confirmRideCreationView);
                    }
                }
            });

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }

        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(1000)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(getContext(), "posizione no", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // La posizione attuale è disponibile qui (location)
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    currentLocation = location;
                    Toast.makeText(getContext(), "posizione ok", Toast.LENGTH_SHORT).show();

                    // Fai qualcosa con la posizione
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
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

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            mapFragment.getMapAsync(callback);
                        } else {
                            Toast.makeText(getContext(), "Hai bisogno dei permessi alla posizione", Toast.LENGTH_SHORT).show();
                        }
                    });

            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
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

            }
        });
    }

    private boolean isStart(Place place){
        return (place.getType().equals("START") ? true : false);
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
        googleMap.addMarker(new MarkerOptions().position(placePosition).title(place.getName())).setSnippet("Tocca qui per selezionare il punto di " + placeType);
    }
    private void showConfirmRideCreationDialog(View confirmRideCreationView){
        dialog = new BottomSheetDialog(getContext());
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
                if(dateText.getText().toString().matches("")){
                    TextInputLayout t = confirmRideCreationView.findViewById(R.id.dateInputLayout);
                    t.setError("Deve essere selezionata una data");
                }else{
                    // TODO aggiungi con gestione eccezioni
                    if(user != null && user.isCarOwner()){
                        writeNewRide();
                        dialog.hide();
                    }else{
                        Toast.makeText(getContext(), "prima ti serve un'automobile", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void fillDialogData(View v){
        TextView ownerTxt = v.findViewById(R.id.rideOwner);
        TextView startTxt = v.findViewById(R.id.rideStart);
        TextView stopTxt = v.findViewById(R.id.rideStop);
        ownerTxt.setText("idUtenteLoggato");
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