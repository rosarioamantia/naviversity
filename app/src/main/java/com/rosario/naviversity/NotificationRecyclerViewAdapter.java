package com.rosario.naviversity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
