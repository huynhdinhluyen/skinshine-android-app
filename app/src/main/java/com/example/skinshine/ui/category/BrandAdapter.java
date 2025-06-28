package com.example.skinshine.ui.category;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Brand;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    private final List<Brand> brandList;
    private final OnBrandClickListener listener;
    private int selectedPosition = 0;

    public BrandAdapter(List<Brand> brandList, OnBrandClickListener listener) {
        this.brandList = brandList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = brandList.get(position);
        holder.textBrandName.setText(brand.getName());

        // Highlight dòng được chọn
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Load logo bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(brand.getLogo_url())
                .placeholder(R.drawable.ic_placeholder) // ảnh tạm thời nếu đang load
                .error(R.drawable.ic_error_placeholder) // nếu lỗi
                .into(holder.imageBrandLogo);

        // Xử lý khi click
        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            listener.onBrandClick(brand);
        });
    }

    @Override
    public int getItemCount() {
        return brandList.size();
    }

    public interface OnBrandClickListener {
        void onBrandClick(Brand brand);
    }

    public static class BrandViewHolder extends RecyclerView.ViewHolder {
        ImageView imageBrandLogo;
        TextView textBrandName;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            imageBrandLogo = itemView.findViewById(R.id.imageBrandLogo);
            textBrandName = itemView.findViewById(R.id.textBrandName);
        }
    }
}
