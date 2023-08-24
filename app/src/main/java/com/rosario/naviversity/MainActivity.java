package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    TextInputEditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;
    TextView regTxt;
    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            Toast.makeText(getApplicationContext(), "Utente già loggato", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
            startActivity(intent);
            finish();
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
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(!user.isEmailVerified()){
                                    Toast.makeText(getApplicationContext(), "devi verificare l'email", Toast.LENGTH_SHORT).show();
                                }else{
                                    Intent i = new Intent(getApplicationContext(), HomepageActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "signInWithEmail:failure", Toast.LENGTH_SHORT).show();
                                //updateUI(null);
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