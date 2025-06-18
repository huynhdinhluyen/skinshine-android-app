package com.example.skinshine.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private ImageView imageProduct;
    private TextView textName, textBrand, textPrice, textDescription, textIngredients, textCategory, textSkinTypes;
    private RatingBar ratingBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        // Ánh xạ view
        imageProduct = view.findViewById(R.id.imageProduct);
        textName = view.findViewById(R.id.textName);
        textBrand = view.findViewById(R.id.textBrand);
        textPrice = view.findViewById(R.id.textPrice);
        textDescription = view.findViewById(R.id.textDescription);
        textIngredients = view.findViewById(R.id.textIngredients);
        textCategory = view.findViewById(R.id.textCategory);
        textSkinTypes = view.findViewById(R.id.textSkinTypes);
        ratingBar = view.findViewById(R.id.ratingBar);

        ImageView iconBack = view.findViewById(R.id.iconBack);
        iconBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Lấy productId từ Bundle
        String productId = getArguments() != null ? getArguments().getString("productId") : null;
        if (productId != null) {
            fetchProductFromFirebase(productId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchProductFromFirebase(String productId) {
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            updateUI(product);
                        } else {
                            Toast.makeText(getContext(), "Không thể hiển thị sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(Product product) {
        // Load ảnh sản phẩm
        Glide.with(requireContext())
                .load(product.getImageUrl())
                .placeholder(R.color.placeholder_bg)
                .error(R.drawable.ic_error_placeholder)
                .into(imageProduct);

        // Gán dữ liệu với nhãn rõ ràng
        textName.setText(product.getName()); // tên có thể giữ nguyên riêng
        textBrand.setText("🏷️ Thương hiệu: " + product.getBrand());
        textPrice.setText("💵 Giá: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));
        textDescription.setText("📃 Mô tả: " + product.getDescription());
        textIngredients.setText("🧪 Thành phần: " + product.getIngredients());
        textCategory.setText("🧴 Loại sản phẩm: " + product.getCategory());

        // Rating
        if (product.getRating() > 0) {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(product.getRating());
        } else {
            ratingBar.setVisibility(View.GONE);
        }

        // Phù hợp loại da
        List<String> skinTypes = product.getSuitableSkinTypes();

        if (skinTypes != null && !skinTypes.isEmpty()) {
            List<String> cleaned = new ArrayList<>();
            for (String s : skinTypes) {
                android.util.Log.d("ProductDetail", "Skin type: " + s);
                cleaned.add(s.replace("\"", "").trim());
            }
            textSkinTypes.setText("✅ Phù hợp: " + String.join(", ", cleaned));
        } else {
            android.util.Log.d("ProductDetail", "Skin types is null");
            textSkinTypes.setText("Phù hợp: Không rõ");
        }

    }
}
