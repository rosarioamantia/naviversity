package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import static com.rosario.naviversity.Constants.DB_PLACE;
import static com.rosario.naviversity.Constants.DB_RIDE;

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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rosario.naviversity.adapter.ConfirmRideRecyclerViewAdapter;
import com.rosario.naviversity.model.Place;
import com.rosario.naviversity.model.Ride;
import com.rosario.naviversity.model.User;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SearchRideFragment extends Fragment {
    GoogleMap googleMap;
    ArrayList<Ride> listRides = new ArrayList<>();
    final static int MAX_RIDE_MEMBERS_ALLOWED = 4; //owner included
    Place start;
    Place stop;
    Button btnSearch, btnRepeatSearch;
    ImageButton btnClose;
    CardView searchCard;
    List<Place> listStart;
    List<Place> listStop;
    ArrayAdapter<Place> startAdapter;
    ArrayAdapter<Place> stopAdapter;
    DatabaseReference placeReference;
    DatePickerDialog datePicker;
    TextInputEditText timeText;
    TimePickerDialog timePicker;
    TextInputEditText dateText;
    Marker positionMarker;
    FirebaseDatabase mDatabase;
    Ride ride;
    BottomSheetDialog confirmDialog;
    FirebaseAuth mAuth;
    DatabaseReference dbReference;
    AutoCompleteTextView startTxt;
    AutoCompleteTextView stopTxt;
    TextInputLayout startLayout, stopLayout, dateLayout, timeLayout;
    FusedLocationProviderClient fusedLocationClient;
    ValueEventListener fillPlaceListener, searchRideListener;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            setGoogleMap(googleMap);
            googleMap.setMyLocationEnabled(true);
            googleMap.setBuildingsEnabled(false);
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            searchCard.setVisibility(View.GONE);
            btnRepeatSearch.setVisibility(View.VISIBLE);

            //unique and clickable marker to choose ride
            LatLng startCoordinates = new LatLng(start.getLatitude(), start.getLongitude());
            positionMarker = googleMap.addMarker(new MarkerOptions().position(startCoordinates).title(start.getName() + " - " +  stop.getName()));
            positionMarker.setSnippet(getString(R.string.touch_to_view_rides));

            positionMarker.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(startCoordinates,15, 1, 1)));

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    positionMarker.hideInfoWindow();
                    View confirmRideView = getLayoutInflater().inflate(R.layout.confirm_ride_dialog, null, false);

                    ConfirmRideRecyclerViewAdapter adapter = new ConfirmRideRecyclerViewAdapter(getContext(), listRides, confirmDialog);
                    RecyclerView recyclerView = confirmRideView.findViewById(R.id.mRecyclerView);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    adapter.notifyDataSetChanged();

                    confirmDialog.setContentView(confirmRideView);
                    confirmDialog.show();

                    confirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            searchCard.setVisibility(View.VISIBLE);
                            btnRepeatSearch.setVisibility(View.GONE);
                            btnClose.setVisibility(View.GONE);
                        }
                    });

                    confirmDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    return false;
                }
            });

            btnRepeatSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRepeatSearch.setVisibility(View.GONE);
                    searchCard.setVisibility(View.VISIBLE);
                    btnClose.setVisibility(View.VISIBLE);

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchCard.setVisibility(View.GONE);
                            btnRepeatSearch.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        }
    };

    public void setGoogleMap(GoogleMap gMap){
        this.googleMap = gMap;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_ride, container, false);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        btnSearch = view.findViewById(R.id.btn_search);
        btnRepeatSearch = view.findViewById(R.id.btn_repeat_search);
        btnClose = view.findViewById(R.id.btn_close);
        searchCard = view.findViewById(R.id.search_card);
        dateText = view.findViewById(R.id.date);
        timeText = view.findViewById(R.id.time);
        confirmDialog = new BottomSheetDialog(getContext());
        listStart = new ArrayList<Place>();
        listStop = new ArrayList<Place>();
        startAdapter = new ArrayAdapter<Place>(getContext(), android.R.layout.simple_spinner_dropdown_item, listStart);
        stopAdapter = new ArrayAdapter<Place>(getContext(), android.R.layout.simple_spinner_dropdown_item, listStop);
        placeReference = mDatabase.getReference(DB_PLACE);
        startTxt = view.findViewById(R.id.start_txt);
        stopTxt = view.findViewById(R.id.stop_txt);
        startTxt.setAdapter(startAdapter);
        stopTxt.setAdapter(stopAdapter);
        btnRepeatSearch.setVisibility(View.GONE);
        startLayout = view.findViewById(R.id.start_layout);
        stopLayout = view.findViewById(R.id.stop_layout);
        timeLayout = view.findViewById(R.id.time_layout);
        dateLayout = view.findViewById(R.id.date_layout);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        initializeListeners();
        getLastLocation();


        return view;
    }

    public void initializeListeners(){
        stopTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(stopLayout.getError() != null){
                    stopLayout.setError(null);
                }
                stop = (Place) adapterView.getItemAtPosition(i);
            }
        });

        startTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(startLayout.getError() != null){
                    startLayout.setError(null);
                }
                start = (Place) adapterView.getItemAtPosition(i);
            }
        });

        fillPlaceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listStart.clear();
                listStop.clear();
                for(DataSnapshot child : snapshot.getChildren()){
                    Place place = child.getValue(Place.class);
                    if(place.getType().equals(getString(R.string.db_start))){
                        listStart.add(place);
                    }else{
                        listStop.add(place);
                    }
                }
                startAdapter.notifyDataSetChanged();
                stopAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Non puoi eseguire questa operazione", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        };
        placeReference.addListenerForSingleValueEvent(fillPlaceListener);

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
                        if(dateLayout.getError() != null){
                            dateLayout.setError(null);
                        }
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
                        if(timeLayout.getError() != null){
                            timeLayout.setError(null);
                        }
                        timeText.setText(String.format("%02d:%02d", hours, minutes) );
                    }
                }, time.getHour(), time.getMinute(), true);
                timePicker.show();
            }
        });
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
                        Task<Location> task = fusedLocationClient.getLastLocation();
                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null){
                                    searchRides();
                                }else{
                                    btnSearch.setEnabled(false);
                                    Toast.makeText(getContext(), R.string.active_localization_search, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.accept_permission_position, Toast.LENGTH_SHORT).show();
                        btnSearch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View btnView) {
                                Toast.makeText(getContext(), R.string.accept_permission_position_search, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        requestPermissionLauncher.launch(permissions);
    }

    public void updateListRides(DataSnapshot snapshot){
        for(DataSnapshot ds : snapshot.getChildren()){
            ride = ds.getValue(Ride.class);
            if(ride != null){
                ride.setId(ds.getKey());
                if(checkIsRideEligible(ride)){
                    listRides.add(ride);
                }
            }
        }
    }

    public void searchRides(){
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View btnView) {
                    listRides.clear();
                    if(checkSearchValues()){
                        searchRideListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                updateListRides(snapshot);
                                if(!listRides.isEmpty()){
                                    mapFragment.getMapAsync(callback);
                                }else{
                                    Toast.makeText(getContext(), R.string.no_ride_found, Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), getString(R.string.operation_not_permitted), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, error.getMessage());
                            }
                        };
                        dbReference.child(DB_RIDE).addListenerForSingleValueEvent(searchRideListener);
                    }
                }
            });
        }
    }

    //Ride eligible with a difference in time of maximum 30 minutes
    public boolean checkIsRideEligible(Ride ride){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        String searchedRideStartName = ride.getStart().getName();
        String searchedRideStopName = ride.getStop().getName();
        String searchedRideDate = ride.getDate();
        String searchedRideTime = ride.getTime();
        String pickerDate = dateText.getText().toString();
        String pickerTime = timeText.getText().toString();
        HashMap<String, User> rideMembers = ride.getMembers();

        boolean checkStart = searchedRideStartName.equals(start.getName());
        boolean checkStop = searchedRideStopName.equals(stop.getName());
        boolean checkDate = pickerDate.equals(searchedRideDate);
        boolean checkTime = checkIsTimeEligible(pickerTime, searchedRideTime);
        boolean maxMembersReached = rideMembers.size() > MAX_RIDE_MEMBERS_ALLOWED;
        boolean isAlreadyMember = rideMembers.get(currentUserId) != null;

        if(checkStart && checkStop && checkDate && checkTime && !maxMembersReached && !isAlreadyMember){
            return true;
        }
        return false;
    }

    public boolean checkIsTimeEligible(String pickerTime, String rideTime){
        LocalTime superiorLimit = LocalTime.parse(pickerTime).plus(31, ChronoUnit.MINUTES);
        LocalTime inferiorLimit = LocalTime.parse(pickerTime).minus(31, ChronoUnit.MINUTES);
        LocalTime ride = LocalTime.parse(rideTime);

        if(ride.isAfter(inferiorLimit) && ride.isBefore(superiorLimit)){
            return true;
        }
        return false;
    }

    public boolean checkSearchValues(){
        String startValue = startTxt.getText().toString();
        String stopValue = stopTxt.getText().toString();
        String timeValue = timeText.getText().toString();
        String dateValue = dateText.getText().toString();

        if(startValue.isEmpty()){
            startLayout.setError(getString(R.string.insert_start));
            return false;
        }
        if(stopValue.isEmpty()){
            stopLayout.setError(getString(R.string.insert_stop));
            return false;
        }
        if(dateValue.isEmpty()){
            dateLayout.setError(getString(R.string.insert_day));
            dateLayout.setErrorIconDrawable(null);
            return false;
        }
        if(timeValue.isEmpty()){
            timeLayout.setError(getString(R.string.insert_time));
            timeLayout.setErrorIconDrawable(null);
            return false;
        }
        return true;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(googleMap != null){
            googleMap.setOnMarkerClickListener(null);
        }
        confirmDialog.setOnDismissListener(null);
        btnRepeatSearch.setOnClickListener(null);
        btnClose.setOnClickListener(null);
        stopTxt.setOnItemClickListener(null);
        startTxt.setOnItemClickListener(null);
        timeText.setOnClickListener(null);
        dateText.setOnClickListener(null);
        btnSearch.setOnClickListener(null);
        if(searchRideListener != null){
            dbReference.child(DB_RIDE).removeEventListener(searchRideListener);
        }
        placeReference.removeEventListener(fillPlaceListener);
    }
}