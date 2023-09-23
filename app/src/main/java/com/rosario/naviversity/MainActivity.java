package com.rosario.naviversity;

import static com.rosario.naviversity.Constants.ADMIN_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Button btnLogin;
    EditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;
    TextView regTxt;
    OnCompleteListener<AuthResult> completeSignInListener;
    View.OnClickListener loginListener;
    static final int BASIC_PERMISSION_CODE = 100;
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
                Toast.makeText(getApplicationContext(), R.string.accept_all_permissions, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogin = findViewById(R.id.btnLogin);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        regTxt = findViewById(R.id.regTxt);
        String[] basicPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissions(basicPermissions, BASIC_PERMISSION_CODE);

        loginListener =  new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), R.string.insert_mail, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(), R.string.insert_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                completeSignInListener = new OnCompleteListener<AuthResult>() {
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
                                Toast.makeText(getApplicationContext(), R.string.unict_mail_verify, Toast.LENGTH_SHORT).show();
                            }else{
                                intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), R.string.incorrect_credentials, Toast.LENGTH_SHORT).show();
                        }
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(null);
                    }
                };

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, completeSignInListener);
            }
        };

        btnLogin.setOnClickListener(loginListener);

        regTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        regTxt.setOnClickListener(null);
        btnLogin.setOnClickListener(null);
    }
}