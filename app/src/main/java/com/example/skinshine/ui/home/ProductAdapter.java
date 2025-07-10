package com.example.skinshine.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final Context context;
    private final OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.textViewProductName.setText(product.getName());

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.textViewProductPrice.setText(currencyFormatter.format(product.getPrice()));

        if (product.getBrand() != null) {
            product.getBrand().get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String brandName = snapshot.getString("name");
                    holder.textViewBrand.setText(brandName != null ? brandName : "Không rõ thương hiệu");
                } else {
                    holder.textViewBrand.setText("Không rõ thương hiệu");
                }
            }).addOnFailureListener(e -> {
                Log.e("ProductAdapter", "Lỗi khi lấy brand: " + e.getMessage());
                holder.textViewBrand.setText("Không rõ thương hiệu");
            });
        } else {
            holder.textViewBrand.setText("Không rõ thương hiệu");
        }

        if (product.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(product.getRating());
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.color.placeholder_bg)
                    .error(R.drawable.ic_error_placeholder)
                    .into(holder.imageViewProduct);
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_error_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    public void updateProducts(List<Product> newProducts) {
        ProductDiffCallback diffCallback = new ProductDiffCallback(this.productList, newProducts);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.productList.clear();
        this.productList.addAll(newProducts);
        diffResult.dispatchUpdatesTo(this);
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewProductPrice;
        TextView textViewBrand;
        RatingBar ratingBar;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewBrand = itemView.findViewById(R.id.textViewBrand);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
