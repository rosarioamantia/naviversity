package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import static com.rosario.naviversity.Constants.DB_PLACE;
import static com.rosario.naviversity.Constants.PLACE_NODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rosario.naviversity.model.Place;

import java.util.HashMap;
import java.util.Map;

public class AdministrationActivity extends AppCompatActivity implements OnMapReadyCallback {
    static final double INIT_LAT = 37.51036888646457;
    static final double INIT_LON = 15.085487628719221;
    GoogleMap googleMap;
    FirebaseAuth fAuth;
    DatabaseReference dbReference;
    BottomSheetDialog dialog;
    String[] placeTypes;
    ArrayAdapter<String> typeAdapter;
    HashMap <Marker, Place> markerPlaceMap = new HashMap<>();
    View.OnClickListener addPlaceListener, deletePlaceListener;
    Button btnLogout, confirmAddBtn, confirmDeleteBtn;
    ValueEventListener placeDatabaseListener;
    GoogleMap.OnMapClickListener addMapsPlaceListener;
    GoogleMap.OnInfoWindowClickListener deleteMapsPlaceListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administration);
        fAuth = FirebaseAuth.getInstance();
        Toast.makeText(getApplicationContext(), R.string.touch_to_create_place, Toast.LENGTH_SHORT).show();
        dbReference = FirebaseDatabase.getInstance().getReference();
        placeTypes = getResources().getStringArray(R.array.place_types);
        typeAdapter = new ArrayAdapter<>(getApplicationContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                placeTypes);
        btnLogout = findViewById(R.id.btn_logout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng initCoordinates = new LatLng(INIT_LAT, INIT_LON);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(initCoordinates,14, 1, 1)));

        //get places from Firebase db
        placeDatabaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item : snapshot.getChildren()){
                    Place place = item.getValue(Place.class);
                    place.setId(item.getKey());
                    String placeName = place.getName();
                    double placeLat = place.getLatitude();
                    double placeLon = place.getLongitude();
                    LatLng coordinates = new LatLng(placeLat, placeLon);
                    Marker addedMarker = googleMap.addMarker(new MarkerOptions().position(coordinates).title(placeName));
                    addedMarker.setSnippet(getString(R.string.touch_to_delete_place));
                    markerPlaceMap.put(addedMarker, place);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.operation_not_permitted, Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        };

        dbReference.child(getString(R.string.db_place)).addListenerForSingleValueEvent(placeDatabaseListener);

        addMapsPlaceListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                View confirmRideCreationView = getLayoutInflater().inflate(R.layout.confirm_place_dialog, null, false);
                dialog = new BottomSheetDialog(AdministrationActivity.this);
                dialog.setContentView(confirmRideCreationView);
                confirmAddBtn = confirmRideCreationView.findViewById(R.id.action_btn);
                setInitialDialogDataAdd(confirmRideCreationView, latLng);
                dialog.show();
                addPlaceListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(checkFilledPlaceValues(confirmRideCreationView)){
                            EditText nameTxt = confirmRideCreationView.findViewById(R.id.place_name);
                            AutoCompleteTextView typeTxt = confirmRideCreationView.findViewById(R.id.place_type);
                            String placeName = nameTxt.getText().toString();
                            double placeLat = latLng.latitude;
                            double placeLon = latLng.longitude;
                            LatLng newPlaceCoord = new LatLng(placeLat, placeLon);
                            String placeType = typeTxt.getText().toString().equals(getString(R.string.Start)) ? getString(R.string.db_start) : getString(R.string.db_stop);
                            Place newPlace = new Place(placeName, placeLat, placeLon, placeType);
                            savePlace(newPlace);
                            Marker newMarker = googleMap.addMarker(new MarkerOptions().position(newPlaceCoord).title(placeName).snippet(getString(R.string.touch_to_delete_place)));
                            markerPlaceMap.put(newMarker, newPlace);
                            dialog.hide();
                        }
                    }
                };
                confirmAddBtn.setOnClickListener(addPlaceListener);
            }
        };
        googleMap.setOnMapClickListener(addMapsPlaceListener);

        deleteMapsPlaceListener = new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                Place placeToDelete = markerPlaceMap.get(marker);
                View confirmRideCreationView = getLayoutInflater().inflate(R.layout.confirm_place_dialog, null, false);
                dialog = new BottomSheetDialog(AdministrationActivity.this);
                dialog.setContentView(confirmRideCreationView);
                confirmDeleteBtn = confirmRideCreationView.findViewById(R.id.action_btn);
                setInitialDialogDataDel(confirmRideCreationView, placeToDelete);
                dialog.show();
                deletePlaceListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletePlace(placeToDelete, marker, dialog);
                    }
                };
                confirmDeleteBtn.setOnClickListener(deletePlaceListener);
            }
        };
        googleMap.setOnInfoWindowClickListener(deleteMapsPlaceListener);
    }

    //initial dialog window to add a place
    private void setInitialDialogDataAdd(View dialogView, LatLng latLng){
        EditText placeLatTxt = dialogView.findViewById(R.id.place_lat);
        EditText placeLonTxt = dialogView.findViewById(R.id.place_lon);
        AutoCompleteTextView placeTypeTxt = dialogView.findViewById(R.id.place_type);
        placeLatTxt.setText(String.valueOf(latLng.latitude));
        placeLonTxt.setText(String.valueOf(latLng.longitude));
        placeTypeTxt.setAdapter(typeAdapter);
    }

    //initial dialog window to remove a place
    private void setInitialDialogDataDel(View dialogView, Place placeToDelete){
        TextView dialogTypeTxt = dialogView.findViewById(R.id.dialog_type);
        EditText placeNameTxt = dialogView.findViewById(R.id.place_name);
        EditText placeLatTxt = dialogView.findViewById(R.id.place_lat);
        EditText placeLonTxt = dialogView.findViewById(R.id.place_lon);
        AutoCompleteTextView placeTypeTxt = dialogView.findViewById(R.id.place_type);
        placeNameTxt.setText(placeToDelete.getName());
        placeLatTxt.setText(String.valueOf(placeToDelete.getLatitude()));
        placeLonTxt.setText(String.valueOf(placeToDelete.getLongitude()));
        placeTypeTxt.setText(placeToDelete.getType().equals(getString(R.string.Start)) ? getString(R.string.db_start) : getString(R.string.db_stop));
        dialogTypeTxt.setText(R.string.confirm_deletion);

        placeNameTxt.setFocusable(false);
        placeTypeTxt.setFocusable(false);
    }

    //check if all fields on dialog are filled
    public boolean checkFilledPlaceValues(View dialogView){
        String name, type;
        EditText placeNameTxt = dialogView.findViewById(R.id.place_name);
        EditText placeTypeTxt = dialogView.findViewById(R.id.place_type);
        TextInputLayout placeNameLayout = dialogView.findViewById(R.id.place_name_input_layout);
        TextInputLayout placeTypeLayout = dialogView.findViewById(R.id.place_type_input_layout);
        name = String.valueOf(placeNameTxt.getText());
        type = String.valueOf(placeTypeTxt.getText());

        if(name.isEmpty()){
            placeNameLayout.setError(getString(R.string.choose_name));
            return false;
        }else{
            if(placeNameLayout.getError() != null){
                placeNameLayout.setErrorEnabled(false);
            }
        }
        if(TextUtils.isEmpty(type)){
            placeTypeLayout.setError(getString(R.string.choose_type));
            return false;
        }else{
            placeTypeLayout.setErrorEnabled(false);
        }
        return true;
    }

    private void savePlace(Place newPlace){
        String key = dbReference.child(DB_PLACE).push().getKey();

        Map<String, Object> placeValues = newPlace.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(PLACE_NODE + key, placeValues);
        newPlace.setId(key);

        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), R.string.correct_created_place, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deletePlace(Place placeToDelete, Marker marker, BottomSheetDialog dialog){
        Map<String, Object> childUpdates = new HashMap<>();
        //remove from Firebase
        childUpdates.put(PLACE_NODE + placeToDelete.getId() , null);
        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //remove from googleMap
                marker.remove();

                markerPlaceMap.remove(marker);
                dialog.hide();
                Toast.makeText(getApplicationContext(), R.string.correct_deleted_place, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btnLogout.setOnClickListener(null);
        dbReference.child(getString(R.string.db_place)).removeEventListener(placeDatabaseListener);
        googleMap.setOnMapClickListener(null);
        googleMap.setOnInfoWindowClickListener(null);
    }
}