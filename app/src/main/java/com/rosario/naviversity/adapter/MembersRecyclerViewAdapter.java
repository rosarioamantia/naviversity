package com.rosario.naviversity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rosario.naviversity.R;

import java.util.List;

public class MembersRecyclerViewAdapter extends RecyclerView.Adapter<MembersRecyclerViewAdapter.MemberViewHolder> {
    private List<String> dataList;

    public MembersRecyclerViewAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_layout_row, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        String data = dataList.get(position);
        holder.memberTxt.setText(data);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView memberTxt;

        public MemberViewHolder(@NonNull View itemView){
            super(itemView);
            memberTxt = itemView.findViewById(R.id.member_row);
        }
    }
}
