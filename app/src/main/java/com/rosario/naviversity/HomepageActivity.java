package com.rosario.naviversity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rosario.naviversity.databinding.ActivityHomepageBinding;

public class HomepageActivity extends AppCompatActivity {

    //ActivityHomepageBinding binding;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), "ciao", Toast.LENGTH_LONG).show();

        //to link layout xml components in Java Objects
        //binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_homepage);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        replaceFragment(new RideFragment());
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int itemId = item.getItemId();
            if(itemId == R.id.ride){
                Toast.makeText(getApplicationContext(), "ride", Toast.LENGTH_SHORT).show();
                replaceFragment(new RideFragment());
            }else if(itemId == R.id.activities){
                Toast.makeText(getApplicationContext(), "activities", Toast.LENGTH_SHORT).show();
                replaceFragment(new ActivitiesFragment());
            }else if(itemId == R.id.profile){
                Toast.makeText(getApplicationContext(), "profile", Toast.LENGTH_SHORT).show();
                replaceFragment(new ProfileFragment());
        }
            /*
            switch (item.getItemId()) {

                case R.id.ride:
                    replaceFragment(new RideFragment());
                    break;
                case R.id.activities:
                    replaceFragment(new ActivitiesFragment());
                    break;
                case R.id.profile:
                    replaceFragment(new ProfileFragment());
                    break;

            }
             */
            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}