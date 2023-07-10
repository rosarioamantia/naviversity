package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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


    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            Toast.makeText(getContext(), "Scegli punto di partenza", Toast.LENGTH_SHORT).show();
            getPlaces(googleMap);

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    Place selectedPlace = getSelectedPlace(marker);
                    if(isStart(selectedPlace)){
                        start = selectedPlace;
                        googleMap.clear();
                        showStopPlaces(googleMap);
                    }else{
                        stop = selectedPlace;
                        googleMap.clear();
                        View confirmRideCreationView = getLayoutInflater().inflate(R.layout.confirm_ride_creation_dialog, null, false);
                        showConfirmRideCreationDialog(confirmRideCreationView);
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_ride, container, false);

        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        listStart = new ArrayList<Place>();
        listStop = new ArrayList<Place>();

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

    public void showStopPlaces(GoogleMap googleMap){
        Toast.makeText(getContext(), "Scegli punto di arrivo", Toast.LENGTH_SHORT).show();
        for(Place stop : listStop){
            showMarker(googleMap, stop, "arrivo");
        }
    }

    private void getPlaces(GoogleMap googleMap){
        dbReference.child("place").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    for(DataSnapshot child : snapshot.getChildren()){
                        Place place = child.getValue(Place.class);
                        if(isStart(place)){
                            listStart.add(place);
                            showMarker(googleMap, place, "partenza");
                        }else{
                            listStop.add(place);
                        }
                    }
                    LatLng catania = new LatLng(37.510998603200704, 15.084607243486413);
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(catania,14, 1, 1)));
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
                    writeNewRide();
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
        Ride ride = new Ride(start, stop, "idUtenteLoggato", dateText.getText().toString(), timeText.getText().toString());
        Map<String, Object> rideValues = ride.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        //one put -> one update in table
        childUpdates.put("/ride/" + key, rideValues);
        //childUpdates.put("/user/" + key, rideValues);

        dbReference.updateChildren(childUpdates);
        Toast.makeText(getContext(), "Corsa creata correttamente", Toast.LENGTH_SHORT).show();
        dialog.hide();
    }
}