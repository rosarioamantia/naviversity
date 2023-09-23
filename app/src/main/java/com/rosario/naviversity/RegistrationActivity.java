package com.rosario.naviversity;

import static android.content.ContentValues.TAG;
import static com.rosario.naviversity.Constants.USER_NODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rosario.naviversity.model.Car;
import com.rosario.naviversity.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 100;
    private final int READ_STORAGE_REQUEST = 200;
    Button btnReg;
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextSurname, editTextPhone;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;
    SwitchMaterial carSwitch;
    TextView logTxt;
    EditText carPlateTxt;
    TextInputLayout carColorInputLayout, carModelInputLayout, carPlateInputLayout;
    String[] carColors, carModels;
    AutoCompleteTextView carColorTxt, carModelTxt;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    ImageView profileImg;
    Uri profileImageUri;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        btnReg = findViewById(R.id.btn_register);
        editTextEmail = findViewById(R.id.email);
        editTextName = findViewById(R.id.name);
        editTextSurname = findViewById(R.id.surname);
        editTextPhone = findViewById(R.id.phone);
        editTextPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        logTxt = findViewById(R.id.logTxt);
        carSwitch = findViewById(R.id.car_switch);
        carModelTxt = findViewById(R.id.car_model_txt);
        carPlateTxt = findViewById(R.id.car_plate_txt);
        carColorInputLayout = findViewById(R.id.car_color_input_layout);
        carModelInputLayout = findViewById(R.id.car_model_input_layout);
        carPlateInputLayout = findViewById(R.id.car_plate_input_layout);
        carColors = getResources().getStringArray(R.array.car_colors);
        carModels = getResources().getStringArray(R.array.car_models);
        carColorTxt = findViewById(R.id.car_color_txt);
        profileImg = findViewById(R.id.profile_image);
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        carColors);
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                carModels);
        carColorTxt.setAdapter(colorAdapter);
        carModelTxt.setAdapter(modelAdapter);

        carSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                switchCarDetailsVisibility(checked);
            }
        });

        logTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFilledUserValues()){
                    String email, password;
                    email = String.valueOf(editTextEmail.getText());
                    password = String.valueOf(editTextPassword.getText());

                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String name, surname, phone;
                                    name = String.valueOf(editTextName.getText());
                                    surname = String.valueOf(editTextSurname.getText());
                                    phone = String.valueOf(editTextPhone.getText());

                                    FirebaseUser fUser = mAuth.getCurrentUser();
                                    updateUserProfileImage(fUser);
                                    saveNewUserData(name, surname, phone, fUser);

                                    fUser.sendEmailVerification()
                                            .addOnCompleteListener(emailTask -> {
                                                if (emailTask.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), R.string.confirmation_mail_send, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch(FirebaseAuthWeakPasswordException e) {
                                        editTextPassword.setError(getString(R.string.error_weak_password));
                                        editTextPassword.requestFocus();
                                    } catch(FirebaseAuthInvalidCredentialsException e) {
                                        editTextEmail.setError(getString(R.string.error_invalid_email));
                                        editTextEmail.requestFocus();
                                    } catch(FirebaseAuthUserCollisionException e) {
                                        editTextEmail.setError(getString(R.string.error_user_exists));
                                        editTextEmail.requestFocus();
                                    } catch(Exception e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
                            }
                        });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.select_profile_image)), PICK_IMAGE_REQUEST);
                } else {
                    ActivityCompat.requestPermissions(RegistrationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_REQUEST);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.accept_storage_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImg.setImageURI(profileImageUri);
        }
    }

    public void switchCarDetailsVisibility(boolean checked){
        if(checked){
            carPlateInputLayout.setVisibility(View.VISIBLE);
            carColorInputLayout.setVisibility(View.VISIBLE);
            carModelInputLayout.setVisibility(View.VISIBLE);
        }else{
            carPlateInputLayout.setVisibility(View.GONE);
            carColorInputLayout.setVisibility(View.GONE);
            carModelInputLayout.setVisibility(View.GONE);
        }
    }

    private void saveNewUserData(String name, String surname, String phone, FirebaseUser fUser){
        User user = new User(name, surname, phone, Role.USER);
        if(carSwitch.isChecked()){
            String model = String.valueOf(carModelTxt.getText());
            String plate = String.valueOf(carPlateTxt.getText()).toUpperCase();
            String color = String.valueOf(carColorTxt.getText());
            Car car = new Car(model, plate, color);
            user.setCar(car);
            user.setCarOwner(true);
        }
        String key = fUser.getUid();
        Map<String, Object> userValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(USER_NODE + key, userValues);

        dbReference.updateChildren(childUpdates);
    }

    public void updateUserProfileImage(FirebaseUser fUser){
        StorageReference fileRef = storageReference.child(getString(R.string.profile_images_path) + mAuth.getCurrentUser().getUid());
        fileRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(profileImageUri)
                                .build();
                        fUser.updateProfile(profileUpdates);
                    }
                });
            }
        });
    }

    public boolean checkFilledUserValues(){
        String email, password, name, surname, phone;
        name = String.valueOf(editTextName.getText());
        surname = String.valueOf(editTextSurname.getText());
        phone = String.valueOf(editTextPhone.getText());
        email = String.valueOf(editTextEmail.getText());
        password = String.valueOf(editTextPassword.getText());
        String unictRegexPattern = getString(R.string.email_regex_pattern) + Pattern.quote(getString(R.string.mail_unict_domain)) + "$";

        if(TextUtils.isEmpty(email)){
            editTextEmail.setError(getString(R.string.insert_mail));
            editTextEmail.requestFocus();
            return false;
        }
        else{
            if(!email.matches(unictRegexPattern)){
                editTextEmail.setError(getString(R.string.insert_unict_mail));
                editTextEmail.requestFocus();
                return false;
            }
        }
        if(TextUtils.isEmpty(password)){
            editTextPassword.setError(getString(R.string.insert_a_password));
            editTextPassword.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(name)){
            editTextName.setError(getString(R.string.insert_a_name));
            editTextName.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(surname)){
            editTextSurname.setError(getString(R.string.insert_a_surname));
            editTextSurname.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(phone)){
            editTextPhone.setError(getString(R.string.insert_a_phone));
            editTextPhone.requestFocus();
            return false;
        }
        if(profileImageUri == null){
            Toast.makeText(getApplicationContext(), R.string.must_choose_profile_image, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(carSwitch.isChecked() && !checkFilledCarValues()){
            return false;
        }
        return true;
    }

    public boolean checkFilledCarValues(){
        String carModel = carModelTxt.getText().toString();
        String carColor = carColorTxt.getText().toString();
        String carPlate = carPlateTxt.getText().toString();
        if(carModel.isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.car_model_insert), Toast.LENGTH_SHORT).show();
            return false;
        }else if(carColor.isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.car_color_insert), Toast.LENGTH_SHORT).show();
            return false;
        }else if(carPlate.isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.car_plate_insert), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!isValidCarPlate(carPlate.toUpperCase())){
            Toast.makeText(getApplicationContext(), getString(R.string.car_plate_valid_insert), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isValidCarPlate(String carPlate){
        String plateRegex = getString(R.string.italian_plate_regex);
        return carPlate.matches(plateRegex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        carSwitch.setOnCheckedChangeListener(null);
        logTxt.setOnClickListener(null);
        btnReg.setOnClickListener(null);
        btnReg.setOnClickListener(null);
        profileImg.setOnClickListener(null);
    }
}