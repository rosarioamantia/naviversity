package com.rosario.naviversity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private final int PICK_IMAGE_REQUEST = 100;
    private final int READ_STORAGE_REQUEST = 200;
    FirebaseAuth mAuth;
    FirebaseUser fUser;
    SwitchMaterial carSwitch;
    User currentUser;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    TextView userNameTxt, userSurnameTxt, userEmailTxt;
    RatingBar ratingBar;
    ConstraintLayout carConstraintLayout;
    String[] carColors, carModels;
    AutoCompleteTextView carColorTxt, carModelTxt;
    Button btnModify, btnLogout, btnConfirm;
    EditText carPlateTxt;
    ArrayAdapter<String> carModelsAdapter;
    ArrayAdapter<String> carColorsAdapter;
    TextInputLayout carModelLayout, carPlateLayout, carColorLayout;
    Uri profileImageUri;
    ImageView profileImg;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUIData(view);
    }

    ActivityResultLauncher<Intent> pickerImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        profileImageUri = data.getData();
                        profileImg.setImageURI(profileImageUri);
                        fUser = mAuth.getCurrentUser();
                        updateUserProfileImage();
                        //doSomeOperations();
                    }
                }
            });

    public void updateUserProfileImage(){
        StorageReference profileImgRef = storageReference.child("/profile_images/" + fUser.getUid());
        profileImgRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                profileImgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(profileImageUri)  //TODO valuta se cambiare con uri parametro (riga sopra)
                                .build();
                        fUser.updateProfile(profileUpdates);
                        Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Toast.makeText(getContext(), "SI", Toast.LENGTH_SHORT).show();
                        launchImagePicker();
                    } else {
                        Toast.makeText(getContext(), "Devi accettare i permessi allo storage del dispositivo", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public void launchImagePicker(){
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        pickerImageResultLauncher.launch(galleryIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Devi accettare i permessi allo storage del dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        carSwitch = view.findViewById(R.id.car_switch);
        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        userNameTxt = view.findViewById(R.id.user_name);
        userSurnameTxt = view.findViewById(R.id.user_surname);
        userEmailTxt = view.findViewById(R.id.user_mail);
        ratingBar = view.findViewById(R.id.rating_bar);
        carConstraintLayout = view.findViewById(R.id.car_constraint_layout);
        carColors = getResources().getStringArray(R.array.car_colors);
        carModels = getResources().getStringArray(R.array.car_models);
        btnModify = view.findViewById(R.id.btn_modifiy);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        carModelTxt = view.findViewById(R.id.car_model_txt);
        carColorTxt = view.findViewById(R.id.car_color_txt);
        carPlateTxt = view.findViewById(R.id.car_plate_txt);
        btnLogout = view.findViewById(R.id.btn_logout);
        carModelLayout = view.findViewById(R.id.car_model_input_layout);
        carColorLayout = view.findViewById(R.id.car_color_input_layout);
        carPlateLayout = view.findViewById(R.id.car_plate_input_layout);
        carColorsAdapter = new ArrayAdapter<>(getContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                carColors);
        carModelsAdapter = new ArrayAdapter<>(getContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                carModels);
        profileImageUri = fUser.getPhotoUrl();
        profileImg = view.findViewById(R.id.profile_image);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        return view;
    }
    public void setUIData(View view){
        dbReference.child("user").child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                currentUser.setId(fUser.getUid());
                userNameTxt.setText(currentUser.getName());
                userSurnameTxt.setText(currentUser.getSurname());
                userEmailTxt.setText(fUser.getEmail());
                if(currentUser.isCarOwner()){
                    ratingBar.setVisibility(View.VISIBLE);
                    float score = currentUser.getScore();
                    int ratingReceived = currentUser.getRatingReceived();
                    float scoreToShow = (float) Math.ceil(score / ratingReceived);
                    ratingBar.setRating(scoreToShow);
                }

                setProfleImage();

                if(currentUser.isCarOwner()){
                    carSwitch.setChecked(true);
                    carConstraintLayout.setVisibility(View.VISIBLE);
                    carColorTxt.setText(currentUser.getCar().getColor());
                    carModelTxt.setText(currentUser.getCar().getModel());
                    carPlateTxt.setText(currentUser.getCar().getPlate());

                    carConstraintLayout.setVisibility(View.VISIBLE);
                    carModelTxt.setAdapter(carModelsAdapter);
                    carColorTxt.setAdapter(carColorsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Toast.makeText(getContext(), "Utente logout", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        carSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    carConstraintLayout.setVisibility(View.VISIBLE);
                    carModelTxt.setAdapter(carModelsAdapter);
                    carColorTxt.setAdapter(carColorsAdapter);

                }else{
                    //aggiungi controllo su corse create dall'utente
                    carConstraintLayout.setVisibility(View.GONE);
                    if(currentUser.getCar() != null){
                        deleteUserCar();
                        carModelTxt.setText(null);
                        carColorTxt.setText(null);
                        carPlateTxt.setText(null);
                    }
                }
            }
        });

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                carModelLayout.setEnabled(true);
                carColorLayout.setEnabled(true);
                carPlateLayout.setEnabled(true);
                btnConfirm.setVisibility(View.VISIBLE);
                btnModify.setVisibility(View.GONE);
                carColorTxt.setText(null);
                carModelTxt.setText(null);
                carPlateTxt.setText(null);

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(checkFilledCarValues()){
                            if(currentUser.isCarOwner()){
                                if(checkChangedCarValues()){
                                    updateUserCar();
                                }
                            }else{
                                addUserCar();
                            }
                            btnConfirm.setVisibility(View.GONE);
                            btnModify.setVisibility(View.VISIBLE);
                            carModelLayout.setEnabled(false);
                            carColorLayout.setEnabled(false);
                            carPlateLayout.setEnabled(false);
                        }
                    }
                });
            }
        });
    }
    public void deleteUserCar(){
        currentUser.setCar(null);
        currentUser.setCarOwner(false);
        Map<String, Object> userValues = currentUser.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/user/" + currentUser.getId() , userValues);
        dbReference.updateChildren(childUpdates);
        Toast.makeText(getContext(), "Automobile eliminata correttamente", Toast.LENGTH_SHORT).show();
    }
    public void updateUserCar(){
        String carPlate = carPlateTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carModel = carModelTxt.getText().toString();
        Car newCar = new Car(carModel, carPlate, carColor);
        currentUser.setCar(newCar);
        Map<String, Object> userValues = currentUser.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/user/" + currentUser.getId() , userValues);
        dbReference.updateChildren(childUpdates);
        Toast.makeText(getContext(), "Automobile aggiornata correttamente", Toast.LENGTH_SHORT).show();
    }
    public void addUserCar(){
        String carPlate = carPlateTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carModel = carModelTxt.getText().toString();
        Car newCar = new Car(carModel, carPlate, carColor);
        currentUser.setCar(newCar);
        currentUser.setCarOwner(true);
        Map<String, Object> userValues = currentUser.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/user/" + currentUser.getId() , userValues);
        dbReference.updateChildren(childUpdates);
        Toast.makeText(getContext(), "Automobile aggiunta correttamente", Toast.LENGTH_SHORT).show();
    }
    public boolean checkFilledCarValues(){
        String carModel = carModelTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carPlate = carPlateTxt.getText().toString();
        if(carModel.isEmpty()){
            Toast.makeText(getContext(), "Devi inserire un modello di auto", Toast.LENGTH_SHORT).show();
            return false;
        }else if(carColor.isEmpty()){
            Toast.makeText(getContext(), "Devi inserire un colore di auto", Toast.LENGTH_SHORT).show();
            return false;
        }else if(carPlate.isEmpty()){
            Toast.makeText(getContext(), "Devi inserire una targa di auto", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public boolean checkChangedCarValues(){
        String carPlate = carPlateTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carModel = carModelTxt.getText().toString();
        Car currentCar = currentUser.getCar();
        if(!currentCar.getPlate().equals(carPlate) || !currentCar.getColor().equals(carColor) || !currentCar.getModel().equals(carModel)){
            return true;
        }
        return false;
    }

    public void setProfleImage(){
        StorageReference fileRef = storageReference.child("/profile_images/" + fUser.getUid());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                profileImg.setVisibility(View.VISIBLE);
                //profileImg.setImageURI(uri);
                Picasso.get().load(uri).into(profileImg);
            }
        });
    }
}