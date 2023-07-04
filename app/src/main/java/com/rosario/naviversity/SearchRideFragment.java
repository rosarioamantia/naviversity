package com.rosario.naviversity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.Clock;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchRideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchRideFragment extends Fragment {
    Button btnSearch;
    Spinner startSpinner;
    Spinner stopSpinner;
    ArrayAdapter<Place> startAdapter;
    ArrayAdapter<Place> stopAdapter;
    FirebaseDatabase mDatabase;
    DatabaseReference startReference;
    DatabaseReference rideReference;
    DatabaseReference stopReference;
    List<Place> listStart;
    List<Ride> listRide;
    List<Place> listStop;
    DatePickerDialog datePicker;
    EditText timeText;
    TimePickerDialog timePicker;

    TextInputEditText dateText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_ride, container, false);
        btnSearch = view.findViewById(R.id.btnSearch);
        dateText = view.findViewById(R.id.date);
        timeText = view.findViewById(R.id.time);
        listRide = new ArrayList<Ride>();
        listStart = new ArrayList<Place>();
        listStop = new ArrayList<Place>();
        mDatabase = FirebaseDatabase.getInstance();

        startSpinner = view.findViewById(R.id.startSpinner);
        startAdapter = new ArrayAdapter<Place>(getContext(), android.R.layout.simple_spinner_dropdown_item, listStart);
        startSpinner.setAdapter(startAdapter);
        startReference = mDatabase.getReference("place");

        stopSpinner = view.findViewById(R.id.stopSpinner);
        stopAdapter = new ArrayAdapter<Place>(getContext(), android.R.layout.simple_spinner_dropdown_item, listStop);
        stopSpinner.setAdapter(stopAdapter);
        stopReference = mDatabase.getReference("place");

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
                startReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                stopReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Place start = (Place) startSpinner.getSelectedItem();
                Place stop = (Place) stopSpinner.getSelectedItem();
                rideReference = mDatabase.getReference("ride");
                rideReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            Ride ride = ds.getValue(Ride.class);
                            String pickerDate = dateText.getText().toString();
                            String pickerTime = timeText.getText().toString();
                            if(ride.getStart(). getName().equals(start.getName()) && ride.getStop().getName().equals(stop.getName()) &&
                                    pickerDate.equals(ride.getDate()) && isTimeElegible(pickerTime, ride.getTime())){

                                String rideId = ds.getKey();
                                //passare l'unico oggetto che rappresenta la ride e cambiare fragment
                                Bundle rideData = new Bundle();
                                rideData.putSerializable("ride", ride);
                                rideData.putString("rideId", rideId);
                                getParentFragmentManager().setFragmentResult("RideData", rideData);
                                //Toast.makeText(getContext(), rideId, Toast.LENGTH_SHORT).show();
                                Fragment mapsFragment = new MapsFragment();
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, mapsFragment);
                                transaction.commit();
                            }

                            //implementare caso senza match
                            //listRide.add(ride);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return view;
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
}