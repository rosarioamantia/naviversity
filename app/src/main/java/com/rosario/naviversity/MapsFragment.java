package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.ktx.Firebase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsFragment extends Fragment {
    Marker mark;
    Geocoder geocoder;

    FirebaseDatabase mDatabase;

    DatabaseReference rideReference;
    Ride ride;

    List<String> listMembers;

    String rideId;

    BottomSheetDialog dialog;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            geocoder = new Geocoder(getContext(), Locale.getDefault());
            Place startPlace = ride.getStart();
            Place stopPlace = ride.getStop();

            //locationName = poi.getName();
            //List<Address> addressList = geocoder.getFromLocationName(locationName, 1);

            LatLng startCoordinates = new LatLng(startPlace.getLatitude(), startPlace.getLongitude());
            mark = googleMap.addMarker(new MarkerOptions().position(startCoordinates).title(startPlace.getName() + " - " +  stopPlace.getName()));
            mark.setSnippet(ride.getDate() + " " + ride.getTime() + " - organizzatore: " + ride.getOwner());
            mark.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(startCoordinates,14, 1, 1)));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mark.hideInfoWindow();
                    showDialog();
                    dialog.show();

                    //*** TODO: controllare a che serve e come settare ***/////
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    return false;
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
        dialog = new BottomSheetDialog(getContext());
        mDatabase = FirebaseDatabase.getInstance();
        getParentFragmentManager().setFragmentResultListener("RideData", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                ride = (Ride) result.getSerializable("ride");
                rideId = result.getString("rideId");
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

    private void showDialog(){
        View v = getLayoutInflater().inflate(R.layout.confirm_ride_dialog, null, false);
        Button btnConfirm = v.findViewById(R.id.btnConfirm);
        fillDialogRideData(v);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), rideId, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                rideReference = mDatabase.getReference("ride");

                //SI DEVE SALVARE l'username nella child
                String actualUser = "genericUserID";
                rideReference.child(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Ride r = snapshot.getValue(Ride.class);
                        if(r.getMembers() != null){
                            r.getMembers().add(actualUser);
                        }else{
                            List<String> firstMemberList = new ArrayList<String>();
                            firstMemberList.add(actualUser);
                            r.setMembers(firstMemberList);
                        }
                        Map<String, Object> rideValues = r.toMap();

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(snapshot.getKey(), rideValues);
                        rideReference.updateChildren(childUpdates);
                        Toast.makeText(getContext(), "Prenotazione confermata", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        dialog.setContentView(v);
    }

    private void fillDialogRideData(View v){
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
}