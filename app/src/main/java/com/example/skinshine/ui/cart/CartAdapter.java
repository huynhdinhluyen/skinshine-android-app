package com.example.skinshine.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onDeleteItem(CartItem item);
        void onItemSelected(CartItem item, boolean isSelected);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.textName.setText(item.getProductName());
        holder.textPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(item.getPrice()));
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));
        holder.checkBox.setChecked(item.isSelected());

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.imageProduct);

        // Xử lý checkbox chọn
        holder.checkBox.setOnCheckedChangeListener(null); // tránh trigger khi bind lại
        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            listener.onItemSelected(item, isChecked);
        });

        // Tăng số lượng
        holder.btnPlus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            holder.textQuantity.setText(String.valueOf(newQuantity));
            listener.onQuantityChanged(item, newQuantity);
        });

        // Giảm số lượng
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                holder.textQuantity.setText(String.valueOf(newQuantity));
                listener.onQuantityChanged(item, newQuantity);
            }
        });

        // Xoá sản phẩm
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView imageProduct;
        TextView textName, textPrice, textQuantity;
        ImageView btnPlus, btnMinus, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxSelect);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
