package com.rosario.naviversity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
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

public class MainActivity extends AppCompatActivity {
    Button btnLogin;
    EditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;
    TextView regTxt;
    static final int BASIC_PERMISSION_CODE = 100;
    static final String ADMIN_ID = "TXk8jICPv7OF647FCW12XrLKtJu1";
    @Override
    public void onStart(){
        super.onStart();
        Intent intent;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(currentUser.getUid().equals(ADMIN_ID)){
                intent = new Intent(getApplicationContext(), AdministrationActivity.class);
                startActivity(intent);
                finish();
            }
            else if(currentUser.isEmailVerified()){
                intent = new Intent(getApplicationContext(), HomepageActivity.class);
                startActivity(intent);
                finish();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BASIC_PERMISSION_CODE) {
            if (grantResults.length > 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "Devi accettare tutti i permessi per usare correttamente l'app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogin = (Button) this.findViewById(R.id.btnLogin);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        regTxt = findViewById(R.id.regTxt);
        String[] basicPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissions(basicPermissions, BASIC_PERMISSION_CODE);

        //MyApplication3 per Toast personalizzato

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                Intent intent;
                                if(currentUser.getUid().equals(ADMIN_ID)){
                                    intent = new Intent(getApplicationContext(), AdministrationActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else if(!currentUser.isEmailVerified()) {
                                    Toast.makeText(getApplicationContext(), "Devi prima verificare la tua email universitaria", Toast.LENGTH_SHORT).show();
                                }else{
                                    intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "Problema con la login", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });
        regTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}