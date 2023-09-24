package com.rosario.naviversity;

import static android.content.ContentValues.TAG;
import static com.rosario.naviversity.Constants.DB_RIDE;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rosario.naviversity.adapter.RideRecyclerViewAdapter;
import com.rosario.naviversity.model.Ride;
import com.rosario.naviversity.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitiesFragment extends Fragment {
    ArrayList<Ride> listRides = new ArrayList<>();
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;
    TextView missingActivitiesTxt;
    ValueEventListener ridesDataListener;

    public ActivitiesFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getUid();
        missingActivitiesTxt = view.findViewById(R.id.missing_activities);


        //Get data Rides
        ridesDataListener = new ValueEventListener() {
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
                if(listRides.isEmpty()){
                    missingActivitiesTxt.setVisibility(View.VISIBLE);
                }
                RideRecyclerViewAdapter adapter = new RideRecyclerViewAdapter(getContext(), listRides, currentUserId);
                RecyclerView recyclerView = view.findViewById(R.id.mRecyclerView);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.operation_not_permitted, Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        };

        dbReference.child(DB_RIDE).addListenerForSingleValueEvent(ridesDataListener);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbReference.child(DB_RIDE).removeEventListener(ridesDataListener);
    }
}