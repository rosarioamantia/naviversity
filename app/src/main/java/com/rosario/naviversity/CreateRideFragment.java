package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import static com.rosario.naviversity.Constants.DB_PLACE;
import static com.rosario.naviversity.Constants.DB_RIDE;
import static com.rosario.naviversity.Constants.DB_USER;
import static com.rosario.naviversity.Constants.NOTIFICATION_NODE;
import static com.rosario.naviversity.Constants.RIDE_NODE;
import static com.rosario.naviversity.Constants.USER_NODE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.rosario.naviversity.model.Car;
import com.rosario.naviversity.model.Place;
import com.rosario.naviversity.model.Ride;
import com.rosario.naviversity.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateRideFragment extends Fragment {
    GoogleMap googleMap;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;
    List<Place> listStart;
    List<Place> listStop;
    Place start;
    BottomSheetDialog creationDialog;
    Place stop;
    DatePickerDialog datePicker;
    TextInputEditText timeText;
    TimePickerDialog timePicker;
    TextInputEditText dateText;
    Button btnConfirm;
    User user;
    FusedLocationProviderClient fusedLocationClient;
    Location currentLocation;
    ValueEventListener fillPlacesListener, getUserListener;
    OnSuccessListener<Location> locationListener;
    Task<Location> locationTask;
    GoogleMap.OnInfoWindowClickListener manageInfoWindowClickListeners;
    private OnMapReadyCallback mapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            setGoogleMap(googleMap);

            Toast.makeText(getContext(), R.string.choose_start, Toast.LENGTH_SHORT).show();
            getPlaces(googleMap);
            googleMap.setMyLocationEnabled(true);
            googleMap.setBuildingsEnabled(false);
            LatLng currentLatLang = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(currentLatLang,14, 1, 1)));

            //managing interactive experience to create a ride
            manageInfoWindowClickListeners = new GoogleMap.OnInfoWindowClickListener() {
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
                    googleMap.setOnInfoWindowClickListener(manageInfoWindowClickListeners);
                }
            };
            googleMap.setOnInfoWindowClickListener(manageInfoWindowClickListeners);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLastLocation();
    }
    private void getLastLocation(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);

                    if (fineLocationGranted && coarseLocationGranted) {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(100);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                            }
                        };

                        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                        locationTask = fusedLocationClient.getLastLocation();
                        locationListener = new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null){
                                    currentLocation = location;
                                    reloadGoogleMap(null);
                                }else{
                                    Toast.makeText(getContext(), R.string.activate_localization, Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                        locationTask.addOnSuccessListener(locationListener);
                    } else {
                        Toast.makeText(getContext(), R.string.accept_permission_position_create, Toast.LENGTH_SHORT).show();
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
        getUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                user.setId(snapshot.getKey());
                dbReference.child(DB_USER).child(mAuth.getUid()).removeEventListener(getUserListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.operation_not_permitted), Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        };

        dbReference.child(DB_USER).child(mAuth.getUid()).addListenerForSingleValueEvent(getUserListener);

        return view;
    }

    public void showStopPlaces(GoogleMap googleMap) {
        Toast.makeText(getContext(), R.string.choose_destination, Toast.LENGTH_SHORT).show();
        for (Place stop : listStop) {
            showMarker(googleMap, stop, getString(R.string.stop));
        }
    }

    //get all places from Firebase to fill data structures
    private void getPlaces(GoogleMap googleMap) {
        fillPlacesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Place place = child.getValue(Place.class);
                        if (isStart(place)) {
                            listStart.add(place);
                            showMarker(googleMap, place, getString(R.string.start));
                        } else {
                            listStop.add(place);
                        }
                    }
                }
                dbReference.child(DB_PLACE).removeEventListener(fillPlacesListener);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.operation_not_permitted), Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        };
        dbReference.child(DB_PLACE).addListenerForSingleValueEvent(fillPlacesListener);
    }

    //check if the place is start type
    private boolean isStart(Place place){
        return place.getType().equals(getString(R.string.db_start));
    }

    //get the Place based on touched Marker info
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
        googleMap.addMarker(new MarkerOptions().position(placePosition).title(place.getName())).setSnippet(getString(R.string.selected_place_type) + placeType);
    }

    //check filled View
    public boolean checkDateTime(View confirmRideCreationView){
        TextInputLayout dateInputLayout = confirmRideCreationView.findViewById(R.id.dateInputLayout);
        TextInputLayout timeInputLayout = confirmRideCreationView.findViewById(R.id.timeInputLayout);
        timeInputLayout.setErrorIconDrawable(null);
        dateInputLayout.setErrorIconDrawable(null);

        if(dateText.getText().toString().isEmpty()){
            dateInputLayout.setError(getString(R.string.insert_value));
            dateInputLayout.setErrorIconDrawable(null);
            return false;
        }else {
            if (dateInputLayout.getError() != null) {
                dateInputLayout.setError(null);
            }
        }
        if(timeText.getText().toString().isEmpty()){
            timeInputLayout.setError(getString(R.string.insert_value));
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

    //check if searching Ride to future
    public boolean checkFutureDateTime(TextInputLayout dateInputLayout, TextInputLayout timeInputLayout){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_format));

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
                timeInputLayout.setError(getString(R.string.invalid_time));
            }
        }else{
            dateInputLayout.setError(getString(R.string.invalid_date));
        }
        return false;
    }

    private void showConfirmRideCreationDialog(View confirmRideCreationView, GoogleMap googleMap){
        creationDialog = new BottomSheetDialog(getContext());
        creationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                reloadGoogleMap(googleMap);
            }
        });
        creationDialog.setContentView(confirmRideCreationView);
        dateText = confirmRideCreationView.findViewById(R.id.rideDate);
        timeText = confirmRideCreationView.findViewById(R.id.rideTime);
        fillDialogData(confirmRideCreationView);
        creationDialog.show();
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
                        timeText.setText(String.format(getString(R.string.time_format), hours, minutes) );
                    }
                }, time.getHour(), time.getMinute(), true);
                timePicker.show();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkDateTime(confirmRideCreationView)){
                    if(user != null && user.isCarOwner()){
                        writeNewRide();
                        creationDialog.hide();
                    }else{
                        Toast.makeText(getContext(), R.string.car_needed, Toast.LENGTH_SHORT).show();
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
        String key = dbReference.child(DB_RIDE).push().getKey();
        Car car = user.getCar();
        HashMap<String, String> userNotification = user.getNotification();

        HashMap<String, User> members = new HashMap<>();
        user.setNotification(null);
        members.put(user.getId(), user);
        Ride ride = new Ride(start, stop, user.getId(), dateText.getText().toString(), timeText.getText().toString(), car, members);

        Map<String, Object> rideValues = ride.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(RIDE_NODE + key, rideValues);

        if(userNotification == null){
            userNotification = new HashMap<>();
        }
        String keyDateTime = generateKeyNotification();
        String message = generateMessageNotificationOwner(ride);
        userNotification.put(keyDateTime, message);
        user.setNotification(userNotification);
        childUpdates.put(USER_NODE + user.getId() + NOTIFICATION_NODE, userNotification);

        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), R.string.confirm_ride_creation, Toast.LENGTH_SHORT).show();
                reloadGoogleMap(googleMap);
            }
        });
    }

    private void setGoogleMap(GoogleMap gMap){
        this.googleMap = gMap;
    }

    public String generateMessageNotificationOwner(Ride ride){
        String message = "Hai creato una corsa per giorno " + ride.getDate() + " (" + ride.getTime() + ") " + "con partenza da " + ride.getStart().getName() + " e destinazione a " + ride.getStop().getName();
        return message;
    }

    //unique key linking date and time (for HashMap)
    public String generateKeyNotification(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getString(R.string.notification_key_pattern));
        String key = now.format(formatter);
        return key;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(btnConfirm != null){
            btnConfirm.setOnClickListener(null);
        }
        if(timeText != null){
            timeText.setOnClickListener(null);
        }
        if(dateText != null){
            dateText.setOnClickListener(null);
        }
        if(creationDialog != null){
            creationDialog.setOnDismissListener(null);
        }
    }
}