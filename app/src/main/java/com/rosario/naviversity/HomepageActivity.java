package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;

public class HomepageActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homepage);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        replaceFragment(new SearchRideFragment());
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                frameLayout.setVisibility(View.VISIBLE);

                int itemId = item.getItemId();
                if(itemId == R.id.ride){
                    Toast.makeText(getApplicationContext(), "ride", Toast.LENGTH_SHORT).show();
                    replaceFragment(new SearchRideFragment());
                }else if(itemId == R.id.activities){
                    Toast.makeText(getApplicationContext(), "activities", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ActivitiesFragment());
                }else if(itemId == R.id.profile){
                    Toast.makeText(getApplicationContext(), "profile", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ProfileFragment());
                }
                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}