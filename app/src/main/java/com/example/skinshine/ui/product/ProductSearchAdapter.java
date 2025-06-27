package com.example.skinshine.ui.product;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductSearchAdapter extends RecyclerView.Adapter<ProductSearchAdapter.SearchViewHolder> {
    private List<Product> products = new ArrayList<>();
    private OnProductSelectListener listener;

    public interface OnProductSelectListener {
        void onProductSelected(Product product);
    }

    public ProductSearchAdapter(OnProductSelectListener listener) {
        this.listener = listener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_search, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProduct;
        private final TextView textName;
        private final TextView textPrice;
        private final TextView textBrand;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textBrand = itemView.findViewById(R.id.textBrand);
        }

        public void bind(Product product, OnProductSelectListener listener) {
            textName.setText(product.getName());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textPrice.setText(formatter.format(product.getPrice()));

            // Load brand name
            if (product.getBrand() != null) {
                product.getBrand().get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        textBrand.setText(doc.getString("name"));
                    } else {
                        textBrand.setText("Không rõ thương hiệu");
                    }
                }).addOnFailureListener(e -> textBrand.setText("Không rõ thương hiệu"));
            } else {
                textBrand.setText("Không rõ thương hiệu");
            }

            // Load product image
            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.color.placeholder_bg)
                    .error(R.drawable.ic_error_placeholder)
                    .into(imageProduct);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductSelected(product);
                }
            });
        }
    }
}
