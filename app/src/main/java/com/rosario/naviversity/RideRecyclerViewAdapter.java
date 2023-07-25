package com.rosario.naviversity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RideRecyclerViewAdapter extends RecyclerView.Adapter<RideRecyclerViewAdapter.MyViewHolder>{

    Context context;
    ArrayList<Ride> listRides;

    public RideRecyclerViewAdapter(Context context, ArrayList<Ride> listRides){
        this.context = context;
        this.listRides = listRides;
    }

    @NonNull
    @Override
    public RideRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new RideRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideRecyclerViewAdapter.MyViewHolder holder, int position) {
        Ride ride = listRides.get(position);
        String startName = ride.getStart().getName();
        String stopName = ride.getStop().getName();
        //String owner = ride.getOwner();
        Car car = ride.getCar();
        String time = ride.getTime();
        String date = ride.getDate();

        if(isDepartment(startName)){
            startName = trucateString(startName, 20);
        }
        if(isDepartment(stopName)){
            stopName = trucateString(stopName, 20);
        }


        holder.startStopTxt.setText(startName +" - " + stopName);
        //holder.ownerTxt.setText(owner);
        holder.carTxt.setText(car.getModel() + " " + car.getColor() + " (" + car.getPlate() + ")");
        holder.dateTxt.setText(date);
        holder.timeTxt.setText(time);
        //holder.textView3.setText(listRides.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return listRides.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView startStopTxt;
        TextView ownerTxt;
        TextView carTxt;
        TextView dateTxt;
        TextView timeTxt;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            startStopTxt = itemView.findViewById(R.id.start_stop_txt);
            //ownerTxt = itemView.findViewById(R.id.ride_owner_txt);
            carTxt = itemView.findViewById(R.id.ride_car_txt);
            dateTxt = itemView.findViewById(R.id.ride_date_txt);
            timeTxt = itemView.findViewById(R.id.ride_time_txt);
            //textView3 = itemView.findViewById(R.id.textView3);
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
