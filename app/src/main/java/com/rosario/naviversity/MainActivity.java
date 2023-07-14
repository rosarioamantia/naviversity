package com.rosario.naviversity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogin = (Button) this.findViewById(R.id.btnLogin);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);


        createFirebaseData();
        //MyApplication3 per Toast personalizzato
    }

    public void login(View v){
        Intent myIntent = new Intent(MainActivity.this, RegistrationActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void createFirebaseData(){
        //createPlaces();
        //createRides();
        //createUsers();
    }

    private void createRides() {
        mDatabase = FirebaseDatabase.getInstance().getReference("ride");
        DatabaseReference newRide = mDatabase.push();
        Ride ride1 = new Ride();
        Place start = new Place("Piazza Stesicoro", 37.50742724675477, 15.085976081843826, "START");
        Place stop = new Place("Cittadella Universitaria", 37.524462577346235, 15.070930995861861, "STOP");
        Car car = new Car("Fiat", "Panda", "Rossa");
        List<String> listUser = new ArrayList<String>();
        listUser.add("username1");
        listUser.add("username2");
        ride1.setMembers(listUser);
        ride1.setStart(start);
        ride1.setStop(stop);
        ride1.setTime("12:00");
        ride1.setOwner("amantiar");
        ride1.setDate("8/7/2023");
        ride1.setCar(car);
        newRide.setValue(ride1);
    }

    private void createUsers(){
        mDatabase = FirebaseDatabase.getInstance().getReference("user");
        DatabaseReference newUser = mDatabase.push();
        DatabaseReference newUser2 = mDatabase.push();
        User u1 = new User();
        User u2 = new User();
        u1.setName("Rosario");
        u1.setSurname("Amantia");
        u1.setUsername("amantiar");
        u2.setName("Gianluca");
        u2.setSurname("Di Franco");
        u2.setUsername("gdifr");
        newUser.setValue(u1);
        newUser2.setValue(u2);
    }

    public void createPlaces(){
        mDatabase = FirebaseDatabase.getInstance().getReference("place");
        DatabaseReference newPlace = mDatabase.push();
        DatabaseReference newPlace2 = mDatabase.push();
        DatabaseReference newPlace3 = mDatabase.push();
        DatabaseReference newPlace4 = mDatabase.push();
        Place place = new Place("Piazza Stesicoro", 37.50742724675477, 15.085976081843826, "START");
        Place place2 = new Place("Piazza Cavour", 37.51766688148675, 15.083010973925521, "START");
        Place place3 = new Place("Cittadella Universitaria", 37.524462577346235, 15.070930995861861, "STOP");
        Place place4 = new Place("Dipartimento di Economia", 37.51556703190273, 15.095447397185225, "STOP");
        newPlace.setValue(place);
        newPlace2.setValue(place2);
        newPlace3.setValue(place3);
        newPlace4.setValue(place4);
    }

}