package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    Button btnReg;
    TextInputEditText editTextEmail, editTextPassword;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    SwitchMaterial carSwitch;
    TextView logTxt;
    EditText carModelTxt, carPlateTxt;
    TextInputLayout carColorTxt;
    String[] carColors;

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
        btnReg = this.findViewById(R.id.btn_register);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        logTxt = findViewById(R.id.logTxt);
        carSwitch = findViewById(R.id.car_switch);
        carModelTxt = findViewById(R.id.model_txt);
        carPlateTxt = findViewById(R.id.plate_txt);
        carColorTxt = findViewById(R.id.color_input_layout);
        carColors = getResources().getStringArray(R.array.my_array);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        carColors);
        AutoCompleteTextView colorTxt =
                findViewById(R.id.outlined_exposed_dropdown_editable);
        colorTxt.setAdapter(adapter);

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

                /*if(TextUtils.isEmpty(email)){
                    editTextEmail.setError("Inserire un'email");
                    return;
                }else{
                    if(!email.matches(unictRegexPattern)){
                        //Toast.makeText(getApplicationContext(), "Inserire email unict", Toast.LENGTH_SHORT).show();
                        editTextEmail.setError("Inserire un'email unict");
                        return;
                    }
                }

                 */

                if(TextUtils.isEmpty(password)){
                    editTextPassword.setError("Inserire una password");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Ti è stata mandata una e-mail", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            //Toast.makeText(getApplicationContext(), "ENTRATO", Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), task.getException().getClass() + "", Toast.LENGTH_LONG).show();
                            //System.out.println("LOCALIZED MESSAGE " + task.getException().getLocalizedMessage().toString());
                           // System.out.println("MESSAGE " + task.getException().getMessage());


                            // La creazione dell'utente ha fallito
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
            carColorTxt.setVisibility(View.VISIBLE);

            ConstraintLayout constraintLayout = findViewById(R.id.registration_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.btn_register, ConstraintSet.TOP, R.id.color_input_layout, ConstraintSet.BOTTOM,20);
            constraintSet.applyTo(constraintLayout);
        }else{
            Toast.makeText(getApplicationContext(), "Enter ca<z", Toast.LENGTH_SHORT).show();
            carModelTxt.setVisibility(View.GONE);
            carPlateTxt.setVisibility(View.GONE);
            carColorTxt.setVisibility(View.GONE);
            ConstraintLayout constraintLayout = findViewById(R.id.registration_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.btn_register, ConstraintSet.TOP, R.id.car_switch, ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(constraintLayout);
        }
    }
}