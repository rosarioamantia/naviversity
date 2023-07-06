package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateRideFragment extends Fragment {
    FirebaseDatabase mDatabase;
    DatabaseReference placeReference;
    List<Place> listStart;
    List<Place> listStop;
    Place start;
    BottomSheetDialog dialog;
    Place stop;

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
                        showDialog(confirmRideCreationView);
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
        placeReference = mDatabase.getReference("place");
        listStart = new ArrayList<Place>();
        listStop = new ArrayList<Place>();
        dialog = new BottomSheetDialog(getContext());

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

    public void getPlaces(GoogleMap googleMap){
        placeReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public boolean isStart(Place place){
        return (place.getType().equals("START") ? true : false);
    }

    public Place getSelectedPlace(Marker marker){
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

    public void showMarker(GoogleMap googleMap, Place place, String placeType){
        double lLatitude = place.getLatitude();
        double lLongitude = place.getLongitude();
        LatLng placePosition = new LatLng(lLatitude, lLongitude);
        googleMap.addMarker(new MarkerOptions().position(placePosition).title(place.getName())).setSnippet("Tocca qui per selezionare il punto di " + placeType);
    }
    private void showDialog(View v){
        dialog.setContentView(v);
        //fillDialogData(v);
        dialog.show();
    }
}