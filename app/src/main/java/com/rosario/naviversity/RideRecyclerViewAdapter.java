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
        holder.textView1.setText(listRides.get(position).getStart().getName());
        holder.textView2.setText(listRides.get(position).getStop().getName());
        holder.textView3.setText(listRides.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return listRides.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textView1;
        TextView textView2;
        TextView textView3;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            textView1 = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);
            textView3 = itemView.findViewById(R.id.textView3);
        }
    }
}
