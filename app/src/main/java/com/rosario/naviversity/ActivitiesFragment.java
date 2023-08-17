package com.rosario.naviversity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActivitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivitiesFragment extends Fragment {
    ArrayList<Ride> listRides = new ArrayList<>();
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;

    public ActivitiesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getUid();

        dbReference.child("ride").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child : snapshot.getChildren()){
                    Ride ride = child.getValue(Ride.class);
                    ride.setId(child.getKey());
                    HashMap<String, User> rideMembers = ride.getMembers();

                    if(rideMembers.get(currentUserId) != null){
                        listRides.add(ride);
                    }
                }
                RideRecyclerViewAdapter adapter = new RideRecyclerViewAdapter(getContext(), listRides, currentUserId);
                RecyclerView recyclerView = view.findViewById(R.id.mRecyclerView);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }
}