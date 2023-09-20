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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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

public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.MemberViewHolder> {
    private List<String> dataList;

    public NotificationRecyclerViewAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_layout_row, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        String data = dataList.get(position);
        holder.singleNotifyTxt.setText(data);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView singleNotifyTxt;

        public MemberViewHolder(@NonNull View itemView){
            super(itemView);
            singleNotifyTxt = itemView.findViewById(R.id.notification_row);
        }
    }
}
