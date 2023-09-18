package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfirmRideRecyclerViewAdapter extends RecyclerView.Adapter<ConfirmRideRecyclerViewAdapter.MyViewHolder>{
    Context context;
    ArrayList<Ride> listRides;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;
    FirebaseAuth fAuth;
    BottomSheetDialog dialog;

    public ConfirmRideRecyclerViewAdapter(Context context, ArrayList<Ride> listRides, BottomSheetDialog dialog){
        this.context = context;
        this.listRides = listRides;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public ConfirmRideRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.confirm_ride_recycler_view_row, parent, false);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        fAuth = FirebaseAuth.getInstance();
        return new ConfirmRideRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmRideRecyclerViewAdapter.MyViewHolder holder, int position) {
        Ride ride = listRides.get(position);
        Button confirmBtn = holder.confirmBtn;
        String rideOwnerId = ride.getOwner();
        User rideOwner = ride.getMembers().get(rideOwnerId);
        String startName = ride.getStart().getName();
        String stopName = ride.getStop().getName();
        String rideTime = ride.getTime();
        String rideDate = ride.getDate();
        float score = rideOwner.getScore();
        int ratingReceived = rideOwner.getRatingReceived();
        float scoreToShow = (float) Math.ceil(score / ratingReceived);
        holder.ownerTxt.setText(rideOwner.getCompleteName());
        holder.startTxt.setText(startName);
        holder.stopTxt.setText(stopName);
        holder.timeTxt.setText(rideTime);
        holder.dateTxt.setText(rideDate);
        holder.ratingBar.setRating(scoreToShow);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dbReference.child("user").child(fAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        user.setVotedOwner("false");
                        user.setName(null);
                        user.setSurname(null);
                        user.setPhone(null);
                        writeUserInRide(user, ride);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context.getApplicationContext(), "Non puoi eseguire questa operazione", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, error.getMessage());
                    }
                });
            }
        });
    }

    public void writeUserInRide(User user, Ride ride){
        HashMap<String, User> members = ride.getMembers();
        HashMap<String, String> userNotification = user.getNotifications();

        user.setNotifications(null);
        members.put(fAuth.getUid(), user);
        ride.setMembers(members);
        Map<String, Object> rideValues = ride.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/ride/" + ride.getId(), rideValues);

        if(userNotification == null){
            userNotification = new HashMap<>();
        }
        String keyDateTime = generateKeyNotification();
        String message = generateMessageNotificationMember(ride);
        userNotification.put(keyDateTime, message);
        childUpdates.put("/user/" + user.getId() + "/notifications/", userNotification);
        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context.getApplicationContext(), "Prenotazione confermata", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public String generateMessageNotificationMember(Ride ride){
        String message = "Ti sei inscritto ad una corsa per giorno " + ride.getDate() + " (" + ride.getTime() + ") " + "con partenza da " + ride.getStart().getName() + " e destinazione a " + ride.getStop().getName();
        return message;
    }

    public String generateKeyNotification(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd|HH:mm:ss");
        String key = now.format(formatter);
        return key;
    }

    @Override
    public int getItemCount() {
        return listRides.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView ownerTxt, startTxt, stopTxt, dateTxt, timeTxt;
        RatingBar ratingBar;
        Button confirmBtn;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            ownerTxt = itemView.findViewById(R.id.ride_owner);
            startTxt = itemView.findViewById(R.id.ride_start_txt);
            stopTxt = itemView.findViewById(R.id.ride_stop_txt);
            dateTxt = itemView.findViewById(R.id.ride_date_txt);
            timeTxt = itemView.findViewById(R.id.ride_time_txt);
            ratingBar = itemView.findViewById(R.id.rating_owner);
            confirmBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}
