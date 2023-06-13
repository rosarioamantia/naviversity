package com.rosario.naviversity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogin = (Button) this.findViewById(R.id.btnLogin);
    }

    public void login(View v){
        Intent myIntent = new Intent(MainActivity.this, HomepageActivity.class);
        MainActivity.this.startActivity(myIntent);
    }
}