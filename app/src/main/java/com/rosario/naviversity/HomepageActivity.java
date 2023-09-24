package com.rosario.naviversity;

import static com.rosario.naviversity.Constants.DB_NOTIFICATION;
import static com.rosario.naviversity.Constants.DB_USER;
import static com.rosario.naviversity.Constants.USER_NODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rosario.naviversity.adapter.NotificationRecyclerViewAdapter;
import com.rosario.naviversity.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomepageActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    RecyclerView notificationRecyclerView;
    FrameLayout frameLayout;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth mAuth;
    User currentUser;
    List<String> notificationList;
    CardView notificationCard;
    ImageView notifcationIcon;
    ValueEventListener getNotificationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        notificationRecyclerView = findViewById(R.id.mRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        TextView badgeCount = findViewById(R.id.badge_count);
        notifcationIcon = findViewById(R.id.notification_icon);
        notificationCard = findViewById(R.id.notification_card);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.fragment_container);

        getNotificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if(currentUser.getNotification() != null){
                    if(currentUser.getLastNotificationNumber() < currentUser.getNotification().size()){
                        badgeCount.setVisibility(View.VISIBLE);
                        int newNotification = currentUser.getNotification().size() - currentUser.getLastNotificationNumber();
                        badgeCount.setText(String.valueOf(newNotification));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //real-time notification updating
        dbReference.child(DB_USER).child(mAuth.getUid()).addValueEventListener(getNotificationListener);

        notifcationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentUser.getNotification() != null){
                    if(notificationCard.getVisibility() == View.GONE){
                        dbReference.child(DB_USER).child(mAuth.getUid()).child(DB_NOTIFICATION).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                notificationList = new ArrayList<>();
                                for(DataSnapshot item : snapshot.getChildren()){
                                    notificationList.add((String) item.getValue());
                                }
                                if(notificationList != null){
                                    currentUser.setLastNotificationNumber(currentUser.getNotification().size());
                                    Map<String, Object> userValues = currentUser.toMap();
                                    Map<String, Object> childUpdates = new HashMap<>();

                                    childUpdates.put(USER_NODE + mAuth.getUid() , userValues);
                                    dbReference.updateChildren(childUpdates);
                                    Collections.reverse(notificationList);
                                    NotificationRecyclerViewAdapter adapter = new NotificationRecyclerViewAdapter(notificationList);
                                    notificationRecyclerView.setAdapter(adapter);
                                    notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                                    notificationCard.setVisibility(View.VISIBLE);
                                    badgeCount.setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else{
                        notificationCard.setVisibility(View.GONE);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.no_notification, Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                frameLayout.setVisibility(View.VISIBLE);
                int itemId = item.getItemId();
                if(itemId == R.id.searchRide){
                    replaceFragment(new SearchRideFragment());
                }else if(itemId == R.id.createRide) {
                    replaceFragment(new CreateRideFragment());
                }else if(itemId == R.id.activities){
                    replaceFragment(new ActivitiesFragment());
                }else if(itemId == R.id.profile){
                    replaceFragment(new ProfileFragment());
                }
                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        notificationCard.setVisibility(View.GONE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bottomNavigationView.setOnItemSelectedListener(null);
        notifcationIcon.setOnClickListener(null);
        finish();
    }

}