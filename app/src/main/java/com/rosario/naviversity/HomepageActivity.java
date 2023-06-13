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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.rosario.naviversity.databinding.ActivityHomepageBinding;

public class HomepageActivity extends AppCompatActivity {

    //ActivityHomepageBinding binding;

    BottomNavigationView bottomNavigationView;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), "ciao", Toast.LENGTH_LONG).show();

        //to link layout xml components in Java Objects
        //binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_homepage);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);
        tabLayout = findViewById(R.id.tabLayout);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setAdapter(viewPagerAdapter);

        replaceFragment(new CreateRideFragment());
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                frameLayout.setVisibility(View.VISIBLE);
                viewPager2.setVisibility(View.GONE);

                int itemId = item.getItemId();
                if(itemId == R.id.ride){
                    //tabLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "ride", Toast.LENGTH_SHORT).show();
                    replaceFragment(new CreateRideFragment());
                }else if(itemId == R.id.activities){
                    //tabLayout.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "activities", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ActivitiesFragment());
                }else if(itemId == R.id.profile){
                    //tabLayout.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "profile", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ProfileFragment());
                }
                return true;
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager2.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                    case 1:
                    case 2: tabLayout.getTabAt(position).select();
                }
                super.onPageSelected(position);
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