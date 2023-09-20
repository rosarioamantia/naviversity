package com.rosario.naviversity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

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
    List<String> listNotify;
    CardView notificationCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        notificationRecyclerView = findViewById(R.id.mRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        TextView t = findViewById(R.id.badge_count);
        ImageView i = findViewById(R.id.imageView);
        notificationCard = findViewById(R.id.notification_card);

        dbReference.child("user").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if(currentUser.getNotification() != null){
                    if(currentUser.getLastNotificationNumber() < currentUser.getNotification().size()){
                        t.setVisibility(View.VISIBLE);
                        int ciao = currentUser.getNotification().size() - currentUser.getLastNotificationNumber();
                        t.setText(ciao + "");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notificationCard.getVisibility() == View.GONE){
                    notificationCard.setVisibility(View.VISIBLE);
                    t.setVisibility(View.GONE);

                    currentUser.setLastNotificationNumber(currentUser.getNotification().size());
                    Map<String, Object> userValues = currentUser.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();

                    childUpdates.put("/user/" + mAuth.getUid() , userValues);
                    dbReference.updateChildren(childUpdates);

                    dbReference.child("user").child(mAuth.getUid()).child("notification").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(listNotify == null){
                                listNotify = new ArrayList<>();
                            }
                            for(DataSnapshot item : snapshot.getChildren()){
                                listNotify.add((String) item.getValue());
                            }
                            //HashMap<String, String> ciao = snapshot.getValue(HashMap.class);
                            //listNotify = extractListNotify(ciao);
                            if(listNotify != null){
                                Collections.reverse(listNotify);
                                NotificationRecyclerViewAdapter adapter = new NotificationRecyclerViewAdapter(listNotify);
                                notificationRecyclerView.setAdapter(adapter);
                                notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    notificationCard.setVisibility(View.GONE);
                }

            }
        });


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);
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

    private List<String> extractListNotify(HashMap<String, String> notification){
        HashMap<String, String> actualUserNotification = notification;

        List<String> notificationList = new ArrayList<>();


        if(actualUserNotification != null){
            for(Map.Entry<String, String> entry : actualUserNotification.entrySet()) {
                String notific = entry.getValue();
                notificationList.add(notific);
            }
            return notificationList;
        }
        return null;
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        //transaction.addToBackStack(null); // Aggiungi la transazione allo stack indietro, se necessario :TODO cancellare ma appunta
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bottomNavigationView.setOnItemSelectedListener(null);
        finish();
    }
}