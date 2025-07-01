package com.example.skinshine.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private final List<CartItem> items;

    public CheckoutAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_product, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textQuantity, textPrice;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textPrice = itemView.findViewById(R.id.textPrice);
        }

        void bind(CartItem item) {
            textProductName.setText(item.getProductName());
            textQuantity.setText("Số lượng: " + item.getQuantity());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textPrice.setText(formatter.format(item.getPrice() * item.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_error_placeholder)
                    .into(imageProduct);
        }
    }
}
