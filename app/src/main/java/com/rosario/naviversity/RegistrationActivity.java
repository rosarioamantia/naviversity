package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    Button btnReg;
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextSurname, editTextPhone;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;
    SwitchMaterial carSwitch;
    TextView logTxt;
    EditText carModelTxt, carPlateTxt;
    TextInputLayout carColorInputLayout;
    String[] carColors;
    AutoCompleteTextView carColorTxt;

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
        btnReg = this.findViewById(R.id.btn_register);
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
        carColors = getResources().getStringArray(R.array.my_array);
        carColorTxt = findViewById(R.id.car_color_txt);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        carColors);
        carColorTxt.setAdapter(adapter);

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
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                String unictRegexPattern = "^[A-Za-z0-9._%+-]+@" + Pattern.quote("studium.unict.it") + "$";

                if(TextUtils.isEmpty(email)){
                    editTextEmail.setError("Inserire un'email");
                    editTextEmail.requestFocus();
                    return;
                }
                /* SBLOCCA PER CONTROLLARE DOMINIO MAIL
                else{
                    if(!email.matches(unictRegexPattern)){
                        editTextEmail.setError("Inserire un'email unict");
                        editTextEmail.requestFocus();
                        return;
                    }
                }

                 */
                if(TextUtils.isEmpty(password)){
                    editTextPassword.setError("Inserire una password");
                    editTextPassword.requestFocus();
                    return;
                }

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

                            //createUserData() -> estrai metodo
                            User user = new User(name, surname, phone);
                            if(carSwitch.isChecked()){
                                String model = String.valueOf(carModelTxt.getText());
                                String plate = String.valueOf(carPlateTxt.getText());
                                String color = String.valueOf(carColorTxt.getText());
                                Car car = new Car(model, plate, color);
                                user.setCar(car);
                                user.setCarOwner(true);
                            }
                            //////////////////////////


                            writeNewUser(user);

                            fUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Ti è stata mandata una e-mail", Toast.LENGTH_SHORT).show();
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
        });
    }

    public void switchCarDetailsVisibility(boolean checked){
        if(checked){
            Toast.makeText(getApplicationContext(), "Enter email", Toast.LENGTH_SHORT).show();
            carModelTxt.setVisibility(View.VISIBLE);
            carPlateTxt.setVisibility(View.VISIBLE);
            carColorInputLayout.setVisibility(View.VISIBLE);

            ConstraintLayout constraintLayout = findViewById(R.id.registration_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.btn_register, ConstraintSet.TOP, R.id.car_color_input_layout, ConstraintSet.BOTTOM,20);
            constraintSet.applyTo(constraintLayout);
        }else{
            Toast.makeText(getApplicationContext(), "Enter ca<z", Toast.LENGTH_SHORT).show();
            carModelTxt.setVisibility(View.GONE);
            carPlateTxt.setVisibility(View.GONE);
            carColorInputLayout.setVisibility(View.GONE);
            ConstraintLayout constraintLayout = findViewById(R.id.registration_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.btn_register, ConstraintSet.TOP, R.id.car_switch, ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(constraintLayout);
        }
    }

    private void writeNewUser(User user){
        String key = dbReference.child("user").push().getKey();
        Map<String, Object> userValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        //one put -> one update in a different table
        childUpdates.put("/user/" + key, userValues);
        //es. childUpdates.put("/ride/organizers" + key, userValues);

        dbReference.updateChildren(childUpdates);
    }
}