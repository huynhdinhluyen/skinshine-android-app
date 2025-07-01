package com.example.skinshine.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.SupportItem;

import java.util.List;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.SupportViewHolder> {
    private final List<SupportItem> supportItems;
    private final OnSupportItemClickListener listener;

    public SupportAdapter(List<SupportItem> supportItems, OnSupportItemClickListener listener) {
        this.supportItems = supportItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SupportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_support, parent, false);
        return new SupportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportViewHolder holder, int position) {
        SupportItem item = supportItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return supportItems.size();
    }

    public interface OnSupportItemClickListener {
        void onItemClick(SupportItem item);
    }

    static class SupportViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconImageView;
        private final TextView titleTextView;

        public SupportViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconSupport);
            titleTextView = itemView.findViewById(R.id.textSupportTitle);
        }

        public void bind(SupportItem item, OnSupportItemClickListener listener) {
            iconImageView.setImageResource(item.getIconRes());
            titleTextView.setText(item.getTitle());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}