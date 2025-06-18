package com.example.skinshine.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;

import java.util.List;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.SupportViewHolder> {
    private final List<SupportItem> items;

    public SupportAdapter(List<SupportItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SupportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_support, parent, false);
        return new SupportViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportViewHolder holder, int position) {
        SupportItem item = items.get(position);
        holder.title.setText(item.title);
        holder.icon.setImageResource(item.iconRes);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class SupportViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public SupportViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgIcon);
            title = itemView.findViewById(R.id.txtTitle);
        }
    }
}
