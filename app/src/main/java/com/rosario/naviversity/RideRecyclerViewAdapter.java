package com.rosario.naviversity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
        Car car = ride.getCar();
        String carModel = car.getModel();
        String carColor = car.getColor();
        String carPlate = car.getPlate();
        String time = ride.getTime();
        String date = ride.getDate();

        if(isDepartment(stopName)){
            stopName = trucateString(stopName, 20);
        }
        holder.startTxt.setText(startName);
        holder.stopTxt.setText(stopName);
        holder.carTxt.setText(carModel + " " + carColor + " (" + carPlate + ")");
        holder.dateTxt.setText(date);
        holder.timeTxt.setText(time);
        initializeButtons(ride, holder);
    }

    @Override
    public int getItemCount() {
        return listRides.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView startTxt;
        TextView stopTxt;
        TextView carTxt;
        TextView dateTxt;
        TextView timeTxt;
        ImageView rateBtn, deleteBtn;
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
            //rateCard = itemView.get (R.id.rate_card);
            //lo puoi gestire tutto qui forse, basta usare itemView.getRootView
        }
    }
    public void initializeButtons(Ride ride, RideRecyclerViewAdapter.MyViewHolder holder){
        String rideOwnerId = ride.getOwner();
        String rideTime = ride.getTime();
        String rideDate = ride.getDate();

        if(isRidePassed(rideDate, rideTime)){
            holder.deleteBtn.setVisibility(View.GONE);
            if(currentUserIsRideOwner(currentUserId, rideOwnerId)){
                holder.rateBtn.setVisibility(View.GONE);
                ConstraintLayout constraintLayout = holder.itemView.findViewById(R.id.card_layout);
                System.out.println(constraintLayout.getChildCount());

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(R.id.date_icon, ConstraintSet.BOTTOM, R.id.card_layout, ConstraintSet.BOTTOM, 20);
                constraintSet.applyTo(constraintLayout);

                /*ViewGroup.LayoutParams layoutParams = holder.itemView.findViewById(R.id.card_layout).getLayoutParams();
                layoutParams.height = 320;
                holder.itemView.findViewById(R.id.card_layout).setLayoutParams(layoutParams);
                */
            }else if(currentUserAlreadyVoted(ride)) {
                holder.rateBtn.setVisibility(View.GONE);
            }
            else{
                holder.rateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //initializeRatingCard(view, ride, holder);
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
                        dbReference.updateChildren(childUpdates);
                        listRides.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        Toast.makeText(context.getApplicationContext(), "Ti sei cancellato dalla corsa", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.rateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context.getApplicationContext(), "La corsa deve ancora avvenire", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    public void initializeRatingCard(Ride ride, RideRecyclerViewAdapter.MyViewHolder holder){
        View rootView = holder.itemView.getRootView();
        Button confirmBtn = rootView.findViewById(R.id.rate_btn);
        CardView rateCard = rootView.findViewById(R.id.rate_card);
        String RideOwnerId = ride.getOwner();

        rateCard.setVisibility(View.VISIBLE);
        RatingBar ratingBar = rateCard.findViewById(R.id.rating_bar);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context.getApplicationContext(), "grazie per il feedback", Toast.LENGTH_SHORT).show();
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
                        childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId + "/votedOwner/", "true");

                        dbReference.updateChildren(childUpdates);
                        rateCard.setVisibility(View.GONE);
                        holder.rateBtn.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
