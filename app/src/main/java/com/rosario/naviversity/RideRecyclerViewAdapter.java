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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RideRecyclerViewAdapter extends RecyclerView.Adapter<RideRecyclerViewAdapter.MyViewHolder>{

    Context context;
    ArrayList<Ride> listRides;
    String currentUserId;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;

    public RideRecyclerViewAdapter(Context context, ArrayList<Ride> listRides, String currentUserId){
        this.context = context;
        this.listRides = listRides;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RideRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        mDatabase = FirebaseDatabase.getInstance();
        dbReference = mDatabase.getReference();
        return new RideRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideRecyclerViewAdapter.MyViewHolder holder, int position) {
        Ride ride = listRides.get(position);
        String startName = ride.getStart().getName();
        String stopName = ride.getStop().getName();
        User owner = ride.getMembers().get(ride.getOwner());
        String ownerName = (owner.getCompleteName()) + (currentUserId.equals(owner.getId()) ? " (tu)" : "");
        Car car = ride.getCar();
        String carDetails = car.getCompleteName();
        String time = ride.getTime();
        String date = ride.getDate();

        holder.startTxt.setText(startName);
        holder.stopTxt.setText(stopName);
        holder.carTxt.setText(carDetails);
        holder.dateTxt.setText(date);
        holder.timeTxt.setText(time);
        holder.ownerTxt.setText(ownerName);
        initializeButtons(ride, holder);
    }

    @Override
    public int getItemCount() {
        return listRides.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView startTxt, stopTxt, carTxt, dateTxt, timeTxt, ownerTxt;
        Button rateBtn, deleteBtn;
        //CardView rateCard;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            startTxt = itemView.findViewById(R.id.ride_start_txt);
            stopTxt = itemView.findViewById(R.id.ride_stop_txt);
            carTxt = itemView.findViewById(R.id.ride_car_txt);
            dateTxt = itemView.findViewById(R.id.ride_date_txt);
            timeTxt = itemView.findViewById(R.id.ride_time_txt);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            rateBtn = itemView.findViewById(R.id.rate_btn);
            ownerTxt = itemView.findViewById(R.id.ride_owner);
            //rateCard = itemView.get (R.id.rate_card);
            //lo puoi gestire tutto qui forse, basta usare itemView.getRootView
        }
    }
    public void initializeButtons(Ride ride, RideRecyclerViewAdapter.MyViewHolder holder){
        String rideOwnerId = ride.getOwner();
        String rideTime = ride.getTime();
        String rideDate = ride.getDate();

        if(isRidePassed(rideDate, rideTime)){
            holder.deleteBtn.setEnabled(false);
            if(currentUserIsRideOwner(currentUserId, rideOwnerId) || currentUserAlreadyVoted(ride)){
                holder.rateBtn.setVisibility(View.GONE);
            }else{
                holder.rateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initializeRatingCard(ride, holder);
                    }
                });
            }
        }else{
            Map<String, Object> childUpdates = new HashMap<>();
            if(currentUserIsRideOwner(currentUserId, rideOwnerId)){
                holder.rateBtn.setVisibility(View.GONE);
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        childUpdates.put("/ride/" + ride.getId(), null);
                        dbReference.updateChildren(childUpdates);
                        listRides.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        Toast.makeText(context.getApplicationContext(), "Corsa eliminata", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId, null);
                        dbReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                listRides.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                Toast.makeText(context.getApplicationContext(), "Ti sei cancellato dalla corsa", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                holder.rateBtn.setEnabled(false);
            }
        }
    }
    public void initializeRatingCard(Ride ride, RideRecyclerViewAdapter.MyViewHolder holder){
        View rootView = holder.itemView.getRootView();
        Button confirmBtn = rootView.findViewById(R.id.rate_btn);
        Button discardBtn = rootView.findViewById(R.id.discard_btn);
        CardView rateCard = rootView.findViewById(R.id.rate_card);
        String RideOwnerId = ride.getOwner();

        rateCard.setVisibility(View.VISIBLE);
        RatingBar ratingBar = rateCard.findViewById(R.id.rating_bar);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbReference.child("user").child(RideOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User owner = snapshot.getValue(User.class);
                        int rideScore = (int) ratingBar.getRating();
                        int ratingReceived = owner.getRatingReceived();
                        int actualScore = owner.getScore();
                        int newScore = actualScore + rideScore;
                        owner.setScore(newScore);
                        owner.setRatingReceived(ratingReceived + 1);

                        Map<String, Object> userValues = owner.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();

                        childUpdates.put("/user/" + RideOwnerId, userValues);
                        childUpdates.put("/ride/" + ride.getId() + "/members/" + RideOwnerId, userValues);
                        childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId + "/votedOwner/", "true");

                        dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                rateCard.setVisibility(View.GONE);
                                holder.rateBtn.setEnabled(false);
                                Toast.makeText(context.getApplicationContext(), "grazie per il feedback", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context.getApplicationContext(), "Non puoi eseguire questa operazione", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, error.getMessage());
                    }
                });
            }
        });

        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateCard.setVisibility(View.GONE);
            }
        });
    }
    public boolean isRidePassed(String date, String time){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate rideDate =  LocalDate.parse(date, dateFormatter);
        LocalTime rideTime = LocalTime.parse(time);
        LocalDate actualDate = LocalDate.now();
        LocalTime actualTime = LocalTime.now();
        if(actualDate.isAfter(rideDate)) {
            return true;
        }else if(actualDate.isEqual(rideDate)){
            if(actualTime.isAfter(rideTime)) {
                return true;
            }
        }
        return false;
    }
    public boolean currentUserAlreadyVoted(Ride ride){
        User currentUser = ride.getMembers().get(currentUserId);
        return currentUser.getVotedOwner().equals("true");
    }
    public boolean currentUserIsRideOwner(String currentUserId , String rideOwnerId){
        return currentUserId.equals(rideOwnerId);
    }
    public String trucateString(String input, int maxLength) {
        if (input.length() <= maxLength)
            return input;
        else
            return input.substring(0, 3) + "." + input.substring(15);
    }
    public boolean isDepartment(String input){
        if(input.substring(0, 12).equals("Dipartimento")){
            return true;
        }
        return false;
    }
}
