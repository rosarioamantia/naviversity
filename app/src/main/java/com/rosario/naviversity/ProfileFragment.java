package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import static com.rosario.naviversity.Constants.DB_USER;
import static com.rosario.naviversity.Constants.USER_NODE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.rosario.naviversity.model.Car;
import com.rosario.naviversity.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
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
    ProgressBar progressBar;
    ValueEventListener UIDataListener;

    public ProfileFragment() {
        // Required empty public constructor
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
                        Intent data = result.getData();
                        profileImageUri = data.getData();
                        fUser = mAuth.getCurrentUser();
                        updateUserProfileImage();
                    }
                }
            });

    public void updateUserProfileImage(){
        StorageReference profileImgRef = storageReference.child(getString(R.string.profile_images_path) + fUser.getUid());

        progressBar.setVisibility(View.VISIBLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        profileImgRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                profileImgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(profileImageUri)
                                .build();
                        fUser.updateProfile(profileUpdates);
                        progressBar.setVisibility(View.GONE);
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        profileImg.setImageURI(profileImageUri);
                        Toast.makeText(getContext(), R.string.image_updated, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        launchImagePicker();
                    } else {
                        Toast.makeText(getContext(), R.string.accept_storage_permission, Toast.LENGTH_SHORT).show();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        btnConfirm = view.findViewById(R.id.delete_btn);
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
        progressBar = view.findViewById(R.id.progress_bar);
        return view;
    }
    public void setUIData(View view){
        UIDataListener = new ValueEventListener() {
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

                setProfileImage();

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
                Toast.makeText(getContext(), getString(R.string.operation_not_permitted), Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        };

        dbReference.child(DB_USER).child(fUser.getUid()).addListenerForSingleValueEvent(UIDataListener);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Toast.makeText(getContext(), R.string.post_logout, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
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
                    carConstraintLayout.setVisibility(View.GONE);
                    if(currentUser.getCar() != null){
                        deleteUserCar();
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
                            ratingBar.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }
    public void deleteUserCar(){
        ratingBar.setVisibility(View.GONE);
        currentUser.setCar(null);
        currentUser.setCarOwner(false);
        Map<String, Object> userValues = currentUser.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(USER_NODE + currentUser.getId() , userValues);
        dbReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                carModelTxt.setText(null);
                carColorTxt.setText(null);
                carPlateTxt.setText(null);
                Toast.makeText(getContext(), R.string.confirm_car_delete, Toast.LENGTH_SHORT).show();            }
        });
    }
    public void updateUserCar(){
        String carPlate = carPlateTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carModel = carModelTxt.getText().toString();
        Car newCar = new Car(carModel, carPlate, carColor);
        currentUser.setCar(newCar);
        Map<String, Object> userValues = currentUser.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(USER_NODE + currentUser.getId() , userValues);
        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), R.string.confirm_car_update, Toast.LENGTH_SHORT).show();
            }
        });
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

        childUpdates.put(USER_NODE + currentUser.getId() , userValues);
        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), R.string.confirm_car_addition, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean checkFilledCarValues(){
        String carModel = carModelTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carPlate = carPlateTxt.getText().toString();
        if(carModel.isEmpty()){
            Toast.makeText(getContext(), R.string.car_model_insert, Toast.LENGTH_SHORT).show();
            return false;
        }else if(carColor.isEmpty()){
            Toast.makeText(getContext(), R.string.car_color_insert, Toast.LENGTH_SHORT).show();
            return false;
        }else if(carPlate.isEmpty()){
            Toast.makeText(getContext(), R.string.car_plate_insert, Toast.LENGTH_SHORT).show();
            return false;
        }else if(!isValidCarPlate(carPlate.toUpperCase())){
            Toast.makeText(getContext(), R.string.car_plate_valid_insert, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isValidCarPlate(String carPlate){
        String plateRegex = getString(R.string.italian_plate_regex);
        return carPlate.matches(plateRegex);
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

    public void setProfileImage(){
        StorageReference fileRef = storageReference.child(getString(R.string.profile_images_path) + fUser.getUid());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImg);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileImg.setOnClickListener(null);
        btnModify.setOnClickListener(null);
        dbReference.child(DB_USER).child(fUser.getUid()).removeEventListener(UIDataListener);
        btnLogout.setOnClickListener(null);
        carSwitch.setOnCheckedChangeListener(null);
        btnConfirm.setOnClickListener(null);
    }
}