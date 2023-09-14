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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
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

        if(isDepartment(stopName)){
            stopName = trucateString(stopName, 20);
        }
        holder.ownerTxt.setText(rideOwner.getCompleteName());
        holder.startTxt.setText(startName);
        holder.stopTxt.setText(stopName);
        holder.timeTxt.setText(rideTime);
        holder.dateTxt.setText(rideDate);

        holder.ratingBar.setRating(scoreToShow);
        //initializeButtons(ride, holder);

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
                        HashMap<String, User> members = ride.getMembers();
                        members.put(fAuth.getUid(), user);
                        ride.setMembers(members);
                        Map<String, Object> rideValues = ride.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/ride/" + ride.getId(), rideValues);
                        //childUpdates.put("/user/" + actualUserId + "/rides/" + ride.getId(), rideValues);
                        // TODO sistema in modo che members non venga salvato in User/rides
                        dbReference.updateChildren(childUpdates);
                        Toast.makeText(context.getApplicationContext(), "Prenotazione confermata", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

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
    public void initializeButtons(Ride ride, ConfirmRideRecyclerViewAdapter.MyViewHolder holder){
        String rideOwnerId = ride.getOwner();
        String rideTime = ride.getTime();
        String rideDate = ride.getDate();

//        if(isRidePassed(rideDate, rideTime)){
//            holder.deleteBtn.setVisibility(View.GONE);
//            if(currentUserIsRideOwner(currentUserId, rideOwnerId)){
//                holder.rateBtn.setVisibility(View.GONE);
//                ConstraintLayout constraintLayout = holder.itemView.findViewById(R.id.card_layout);
//                System.out.println(constraintLayout.getChildCount());
//
//                ConstraintSet constraintSet = new ConstraintSet();
//                constraintSet.clone(constraintLayout);
//                constraintSet.connect(R.id.date_icon, ConstraintSet.BOTTOM, R.id.card_layout, ConstraintSet.BOTTOM, 20);
//                constraintSet.applyTo(constraintLayout);
//
//                /*ViewGroup.LayoutParams layoutParams = holder.itemView.findViewById(R.id.card_layout).getLayoutParams();
//                layoutParams.height = 320;
//                holder.itemView.findViewById(R.id.card_layout).setLayoutParams(layoutParams);
//                */
//            }else if(currentUserAlreadyVoted(ride)) {
//                holder.rateBtn.setVisibility(View.GONE);
//            }
//            else{
//                holder.rateBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //initializeRatingCard(view, ride, holder);
//                        initializeRatingCard(ride, holder);
//                    }
//                });
//            }
//        }else{
//            Map<String, Object> childUpdates = new HashMap<>();
//            if(currentUserIsRideOwner(currentUserId, rideOwnerId)){
//                holder.rateBtn.setVisibility(View.GONE);
//                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        childUpdates.put("/ride/" + ride.getId(), null);
//                        dbReference.updateChildren(childUpdates);
//                        listRides.remove(holder.getAdapterPosition());
//                        notifyItemRemoved(holder.getAdapterPosition());
//                        Toast.makeText(context.getApplicationContext(), "Corsa eliminata", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }else{
//                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId, null);
//                        dbReference.updateChildren(childUpdates);
//                        listRides.remove(holder.getAdapterPosition());
//                        notifyItemRemoved(holder.getAdapterPosition());
//                        Toast.makeText(context.getApplicationContext(), "Ti sei cancellato dalla corsa", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                holder.rateBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(context.getApplicationContext(), "La corsa deve ancora avvenire", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        }
    }
    public void initializeRatingCard(Ride ride, ConfirmRideRecyclerViewAdapter.MyViewHolder holder){
        View rootView = holder.itemView.getRootView();
        Button confirmBtn = rootView.findViewById(R.id.rate_btn);
        CardView rateCard = rootView.findViewById(R.id.rate_card);
        String RideOwnerId = ride.getOwner();

        rateCard.setVisibility(View.VISIBLE);
        RatingBar ratingBar = rateCard.findViewById(R.id.rating_bar);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context.getApplicationContext(), "Grazie per il feedback", Toast.LENGTH_SHORT).show();
                dbReference.child("user").child(RideOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        int rideScore = (int) ratingBar.getRating();
                        int ratingReceived = user.getRatingReceived();
                        ratingReceived = ratingReceived + 1;
                        int actualScore = user.getScore();
                        int newScore = actualScore + rideScore;
                        user.setScore(newScore);
                        user.setRatingReceived(ratingReceived);

                        Map<String, Object> userValues = user.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();

                        childUpdates.put("/user/" + RideOwnerId, userValues);
                        childUpdates.put("/ride/" + ride.getId() + "/members/" + RideOwnerId, userValues);

                        dbReference.updateChildren(childUpdates);
                        rateCard.setVisibility(View.GONE);
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

    // TODO cancella
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
