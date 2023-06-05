package com.rosario.naviversity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RideFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap googleMap;

    public RideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rideFragment = inflater.inflate(R.layout.fragment_ride, container, false);
        //googleMap = rideFragment.findViewById(R.id.map);
        return rideFragment;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap){
        this.googleMap = googleMap;
        this.googleMap.addMarker(new MarkerOptions().position(new LatLng(205937, 789629)).title("PROVA"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(205937, 789629)));
    }
}