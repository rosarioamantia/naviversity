package com.rosario.naviversity;

import static android.content.ContentValues.TAG;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        List<String> memberList = extractMemberList(ride);
        String startName = ride.getStart().getName();
        String stopName = ride.getStop().getName();
        User owner = ride.getMembers().get(ride.getOwner());
        String ownerName = (owner.getCompleteName()) + (currentUserId.equals(owner.getId()) ? " (tu)" : "");
        Car car = ride.getCar();
        String carDetails = car.getCompleteName();
        String time = ride.getTime();
        String date = ride.getDate();

        MembersRecyclerViewAdapter membersAdapter;
        membersAdapter = new MembersRecyclerViewAdapter(memberList);

        holder.membersRecyclerView.setAdapter(membersAdapter);
        holder.membersRecyclerView.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));
        holder.startTxt.setText(startName);
        holder.stopTxt.setText(stopName);
        holder.carTxt.setText(carDetails);
        holder.dateTxt.setText(date);
        holder.timeTxt.setText(time);
        holder.ownerTxt.setText(ownerName);
        initializeButtons(ride, holder);

        holder.membersDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchVisibilityDropdown(holder);
            }
        });
    }

    public void switchVisibilityDropdown(RideRecyclerViewAdapter.MyViewHolder holder){
        if(holder.membersRecyclerView.getVisibility() == View.VISIBLE){
            holder.membersRecyclerView.setVisibility(View.GONE);
            holder.membersDropdown.setText("Visualizza partecipanti");
            holder.membersDropdown.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_arrow_right_24, 0, 0, 0);
        }else{
            holder.membersRecyclerView.setVisibility(View.VISIBLE);
            holder.membersDropdown.setText("Nascondi partecipanti");
            holder.membersDropdown.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_arrow_drop_down_24, 0, 0, 0);
        }

    }

    public List extractMemberList(Ride ride){
        List<String> memberList = new ArrayList<>();

        for(Map.Entry<String, User> entry : ride.getMembers().entrySet()) {
            User member = entry.getValue();
            if(member.getId().equals(ride.getOwner())){
                memberList.add(member.getCompleteName() + " (organizzatore)");
            }else{
                memberList.add(member.getCompleteName());
            }
        }
        return memberList;
    }

    @Override
    public int getItemCount() {
        return listRides.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView startTxt, stopTxt, carTxt, dateTxt, timeTxt, ownerTxt, membersDropdown;
        Button rateBtn, deleteBtn;
        RecyclerView membersRecyclerView;

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
            membersRecyclerView = itemView.findViewById(R.id.members_recycler_view);
            membersDropdown = itemView.findViewById(R.id.members_dropdown);
        }
    }
    public void initializeButtons(Ride ride, RideRecyclerViewAdapter.MyViewHolder holder){
        String rideOwnerId = ride.getOwner();
        String rideTime = ride.getTime();
        String rideDate = ride.getDate();

        if(isRidePassed(rideDate, rideTime)){
            holder.deleteBtn.setEnabled(false);
            if(currentUserIsRideOwner(currentUserId, rideOwnerId)){
                holder.rateBtn.setVisibility(View.GONE);
            }else if(currentUserAlreadyVoted(ride)){
                holder.rateBtn.setEnabled(false);
            }
            else{
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
                        deleteRide(ride);
                        listRides.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        Toast.makeText(context.getApplicationContext(), "Hai eliminato la corsa", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dbReference.child("user").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User currentUser = snapshot.getValue(User.class);
                                currentUser.setId(snapshot.getKey());
                                deleteRideMember(ride, currentUser, holder);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
                holder.rateBtn.setEnabled(false);
            }
        }
    }

    public void deleteRideMember(Ride ride, User currentUser, RideRecyclerViewAdapter.MyViewHolder holder){
        Map<String, Object> childUpdates = new HashMap<>();

        dbReference.child("user").child(ride.getOwner()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User rideOwner = snapshot.getValue(User.class);
                childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId, null);

                HashMap<String, String> ownerNotification = rideOwner.getNotification();
                HashMap<String, String> userNotification = currentUser.getNotification();

                if(ownerNotification == null){
                    ownerNotification = new HashMap<>();
                }
                if(userNotification == null){
                    userNotification = new HashMap<>();
                }
                String keyDateTime = generateKeyNotification();

                String memberMessage = generateMessageNotificationMember(ride);
                userNotification.put(keyDateTime, memberMessage);
                childUpdates.put("/user/" + currentUser.getId() + "/notification/", userNotification);

                String ownerMessage = generateMessageNotificationOwner(ride, currentUser);
                ownerNotification.put(keyDateTime, ownerMessage);
                childUpdates.put("/user/" + ride.getOwner() + "/notification/", ownerNotification);

                dbReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context.getApplicationContext(), "Ti sei cancellato dalla corsa", Toast.LENGTH_SHORT).show();
                        listRides.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context.getApplicationContext(), "Non puoi eseguire questa operazione", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        });




//        dbReference.child("user").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() { TODO cancella!!!
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User dbUser = snapshot.getValue(User.class);
//                dbUser.setId(snapshot.getKey());
//                HashMap<String, String> notifications = dbUser.getNotifications();
//
//                if(notifications == null){
//                    notifications = new HashMap<>();
//                }
//                String keyDateTime = generateKeyNotification();
//                String message = generateMessageNotificationMember(ride);
//                notifications.put(keyDateTime, message);
//                dbUser.setNotifications(notifications);   //ti sei cancellato dalla corsa...       lo studente xx si è cancellato dalla tua corsa
//                childUpdates.put("/user/" + snapshot.getKey(), dbUser);
//                childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId, null);
//
//
//                dbReference.updateChildren(childUpdates);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, error.getMessage());
//            }
//        });
    }
    public void deleteRide(Ride ride){
        Map<String, Object> childUpdates = new HashMap<>();

        HashMap<String, User> rideMembers = ride.getMembers();
        dbReference.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child : snapshot.getChildren()){
                    User dbUser = child.getValue(User.class);
                    dbUser.setId(child.getKey());

                    if(rideMembers.get(child.getKey()) != null){
                        //write notification in dbUSer
                        HashMap<String, String> notifications = dbUser.getNotification();
                        if(notifications == null){
                            notifications = new HashMap<>();
                        }
                        String keyDateTime = generateKeyNotification();
                        String message = generateMessageNotification(ride, dbUser);
                        notifications.put(keyDateTime, message);
                        dbUser.setNotification(notifications);
                        childUpdates.put("/user/" + dbUser.getId(), dbUser);
                    }
                }
                childUpdates.put("/ride/" + ride.getId(), null);
                dbReference.updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    public String generateMessageNotificationMember(Ride ride){
        String message = "Ti sei cancellato dalla corsa  di giorno " + ride.getDate() + " (" + ride.getTime() + ") " + "che andava da " + ride.getStart().getName() + " a " + ride.getStop().getName();
        return message;
    }

    public String generateMessageNotificationOwner(Ride ride, User memberDeleted){
        String message = "Lo studente " + memberDeleted.getCompleteName() + " si è cancellato dalla corsa che hai organizzato per giorno " + ride.getDate() +  " che va da " + ride.getStart().getName() + " a " + ride.getStop().getName();
        return message;
    }

    public String generateKeyNotification(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd|HH:mm:ss");
        String key = now.format(formatter);
        return key;
    }

    public String generateMessageNotification(Ride ride, User dbUser){
        String message = "";
        if(ride.getOwner().equals(dbUser.getId())){
            message = "Hai eliminato la corsa ";
        }else{
            message = "L'organizzatore ha eliminato la corsa ";
        }
        message += "di giorno " + ride.getDate() + " (" + ride.getTime() + ") " + "che andava da " + ride.getStart().getName() + " a " + ride.getStop().getName();
        return message;
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
}
