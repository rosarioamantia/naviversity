package com.rosario.naviversity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RideRecyclerViewAdapter extends RecyclerView.Adapter<RideRecyclerViewAdapter.MyViewHolder>{

    Context context;
    ArrayList<Ride> listRides;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference dbReference;

    public RideRecyclerViewAdapter(Context context, ArrayList<Ride> listRides){
        this.context = context;
        this.listRides = listRides;
    }

    @NonNull
    @Override
    public RideRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbReference = mDatabase.getReference();
        return new RideRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideRecyclerViewAdapter.MyViewHolder holder, int position) {
        Ride ride = listRides.get(position);
        String startName = ride.getStart().getName();
        String stopName = ride.getStop().getName();
        Car car = ride.getOwner().getCar();
        String time = ride.getTime();
        String date = ride.getDate();
        String currentUserId = mAuth.getUid();

        //TODO cancella start
        if(isDepartment(startName)){
            startName = trucateString(startName, 20);
        }
        if(isDepartment(stopName)){
            stopName = trucateString(stopName, 20);
        }

        holder.startTxt.setText(startName);
        holder.stopTxt.setText(stopName);
        holder.carTxt.setText(car.getModel() + " " + car.getColor() + " (" + car.getPlate() + ")");
        holder.dateTxt.setText(date);
        holder.timeTxt.setText(time);
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context.getApplicationContext(), "Premuto " + time, Toast.LENGTH_SHORT).show();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/ride/" + ride.getId() + "/members/" + currentUserId, null);
                //childUpdates.put("/user/" + currentUserId + "/rides/" + ride.getId(), null);
                dbReference.updateChildren(childUpdates);
                listRides.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                Toast.makeText(context.getApplicationContext(), "Corsa eliminata", Toast.LENGTH_SHORT).show();
            }
        });

        holder.confirmBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

            }
        });
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
        ImageView confirmBtn, deleteBtn;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            startTxt = itemView.findViewById(R.id.ride_start_txt);
            stopTxt = itemView.findViewById(R.id.ride_stop_txt);
            carTxt = itemView.findViewById(R.id.ride_car_txt);
            dateTxt = itemView.findViewById(R.id.ride_date_txt);
            timeTxt = itemView.findViewById(R.id.ride_time_txt);
            confirmBtn = itemView.findViewById(R.id.confirm_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
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
