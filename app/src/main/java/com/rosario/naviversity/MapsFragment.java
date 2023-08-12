package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.lang.reflect.Array;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsFragment extends Fragment {
    Button btnSearch;
    CardView cardSearch;
    List<Place> listStart;
    List<Place> listStop;
    Spinner startSpinner;
    Spinner stopSpinner;
    ArrayAdapter<Place> startAdapter;
    ArrayAdapter<Place> stopAdapter;
    DatabaseReference placeReference;
    DatePickerDialog datePicker;
    TextInputEditText timeText;
    TimePickerDialog timePicker;
    TextInputEditText dateText;
    Marker mark;
    Geocoder geocoder;
    FirebaseDatabase mDatabase;
    DatabaseReference rideReference;
    Ride ride;
    BottomSheetDialog dialog;
    FirebaseAuth mAuth;
    DatabaseReference dbReference;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mark.hideInfoWindow();
                    View confirmRideView = getLayoutInflater().inflate(R.layout.confirm_ride_dialog, null, false);
                    createDialog(confirmRideView);
                    Button btnConfirm = confirmRideView.findViewById(R.id.btnConfirm);
                    btnConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateRideMembers(ride);
                            dialog.dismiss();
                        }
                    });

                    //controlla a che serve
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    return false;
                }
            });
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //agisci qui per gestire più ride e proporle (limit to first)
                    rideReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){
                                ride = ds.getValue(Ride.class);
                                if(ride != null){
                                    ride.setId(ds.getKey());
                                    Place start = (Place) startSpinner.getSelectedItem();
                                    Place stop = (Place) stopSpinner.getSelectedItem();
                                    String pickerDate = dateText.getText().toString();
                                    String pickerTime = timeText.getText().toString();

                                    if(ride.getStart().getName().equals(start.getName())  && ride.getStop().getName().equals(stop.getName()) &&
                                            pickerDate.equals(ride.getDate()) && isTimeElegible(pickerTime, ride.getTime())){
                                        googleMap.getUiSettings().setAllGesturesEnabled(true);
                                        cardSearch.setVisibility(View.GONE);

                                        //inserisci marker
                                        LatLng startCoordinates = new LatLng(start.getLatitude(), start.getLongitude());
                                        mark = googleMap.addMarker(new MarkerOptions().position(startCoordinates).title(start.getName() + " - " +  stop.getName()));
                                        mark.setSnippet(ride.getDate() + " " + ride.getTime() + " - organizzatore: " + ride.getOwner());
                                        mark.showInfoWindow();
                                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(startCoordinates,15, 1, 1)));
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        btnSearch = view.findViewById(R.id.btnSearch);
        cardSearch = view.findViewById(R.id.cardSearch);
        startSpinner = view.findViewById(R.id.startSpinner);
        dateText = view.findViewById(R.id.date);
        timeText = view.findViewById(R.id.time);
        stopSpinner = view.findViewById(R.id.stopSpinner);
        dialog = new BottomSheetDialog(getContext());
        mDatabase = FirebaseDatabase.getInstance();
        listStart = new ArrayList<Place>();
        listStop = new ArrayList<Place>();
        startAdapter = new ArrayAdapter<Place>(getContext(), android.R.layout.simple_spinner_dropdown_item, listStart);
        stopAdapter = new ArrayAdapter<Place>(getContext(), android.R.layout.simple_spinner_dropdown_item, listStop);
        startSpinner.setAdapter(startAdapter);
        stopSpinner.setAdapter(stopAdapter);
        placeReference = mDatabase.getReference("place");
        dbReference = mDatabase.getReference();
        rideReference = mDatabase.getReference("ride");
        mAuth = FirebaseAuth.getInstance();

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

        startSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                placeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        listStart.clear();
                        for(DataSnapshot child : snapshot.getChildren()){
                            Place place = child.getValue(Place.class);
                            if(place.getType().equals("START")){
                                listStart.add(place);
                            }
                        }
                        startAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return false;
            }
        });
        stopSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                placeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        listStop.clear();
                        for(DataSnapshot child : snapshot.getChildren()){
                            Place place = child.getValue(Place.class);
                            if(place.getType().equals("STOP")){
                                listStop.add(place);
                            }
                        }
                        stopAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return false;
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
            mapFragment.getMapAsync(callback);
        }
    }
    private void createDialog(View v){
        fillDialogData(v);
        dialog.setContentView(v);
        dialog.show();
    }
    private void fillDialogData(View v){
        TextView owner = v.findViewById(R.id.rideOwner);
        TextView start = v.findViewById(R.id.rideStart);
        TextView stop = v.findViewById(R.id.rideStop);
        TextView date = v.findViewById(R.id.rideDate);
        TextView time = v.findViewById(R.id.rideTime);
        owner.setText(ride.getOwner());
        start.setText(ride.getStart().getName());
        stop.setText(ride.getStop().getName());
        date.setText(ride.getDate());
        time.setText(ride.getTime());
    }
    public boolean isTimeElegible(String pickerTime, String rideTime){
        LocalTime superiorLimit = LocalTime.parse(pickerTime).plus(31, ChronoUnit.MINUTES);
        LocalTime inferiorLimit = LocalTime.parse(pickerTime).minus(31, ChronoUnit.MINUTES);
        LocalTime ride = LocalTime.parse(rideTime);

        if(ride.isAfter(inferiorLimit) && ride.isBefore(superiorLimit)){
            return true;
        }
        return false;
    }
    public void updateRideMembers(Ride ride){
        String actualUserId = mAuth.getCurrentUser().getUid();
        dbReference.child("user").child(actualUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user.setVotedOwner("false");
                HashMap<String, User> members = ride.getMembers();
                members.put(actualUserId, user);
                ride.setMembers(members);
                Map<String, Object> rideValues = ride.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/ride/" + ride.getId(), rideValues);
                //childUpdates.put("/user/" + actualUserId + "/rides/" + ride.getId(), rideValues);
                // TODO sistema in modo che members non venga salvato in User/rides
                dbReference.updateChildren(childUpdates);
                Toast.makeText(getContext(), "Prenotazione confermata", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}