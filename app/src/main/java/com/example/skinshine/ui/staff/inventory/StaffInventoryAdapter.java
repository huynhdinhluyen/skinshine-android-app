package com.example.skinshine.ui.staff.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StaffInventoryAdapter extends RecyclerView.Adapter<StaffInventoryAdapter.InventoryViewHolder> {

    private List<Product> products;

    public StaffInventoryAdapter() {
        // Empty constructor
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_product, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        if (products != null && position < products.size()) {
            holder.bind(products.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textProductPrice, textStockStatus;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textStockStatus = itemView.findViewById(R.id.textStockStatus);
        }

        void bind(Product product) {
            textProductName.setText(product.getName());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textProductPrice.setText(formatter.format(product.getPrice()));

            // Stock status
            textStockStatus.setText("Còn hàng");
            textStockStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));

            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_error_placeholder)
                    .error(R.drawable.ic_error_placeholder)
                    .into(imageProduct);
        }
    }
}
