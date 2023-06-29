package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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

import java.util.Locale;

public class MapsFragment extends Fragment {
    Marker mark;
    Geocoder geocoder;
    String locationName;
    POI poi;
    Destination destination;

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

            //locationName = poi.getName();
            //List<Address> addressList = geocoder.getFromLocationName(locationName, 1);

            LatLng catania = new LatLng(poi.getLatitude(), poi.getLongitude());
            mark = googleMap.addMarker(new MarkerOptions().position(catania).title(poi.getName()));
            mark.setSnippet("24/12/2023" + " Ore 8:00 " + "(Rosario Amantia)");
            mark.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(catania,14, 1, 1)));
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mark.hideInfoWindow();
                    createDialog();
                    dialog.show();
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
        getParentFragmentManager().setFragmentResultListener("DataResult", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                poi = (POI) result.getSerializable("POIData");
                destination = (Destination) result.getSerializable("destinationData");
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

    private void createDialog(){
        View v = getLayoutInflater().inflate(R.layout.bottom_create_dialog, null, false);
        Button b = v.findViewById(R.id.button2);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setContentView(v);
    }

}