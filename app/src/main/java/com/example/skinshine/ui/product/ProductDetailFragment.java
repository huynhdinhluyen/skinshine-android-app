package com.example.skinshine.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private ImageView imageProduct;
    private TextView textName, textBrand, textPrice, textDescription, textIngredients, textCategory, textSkinTypes;
    private RatingBar ratingBar;
    private Product currentProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

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
        ImageView iconCart = view.findViewById(R.id.iconCart);

        iconCart.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.cartFragment);

        });
        iconBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                showAddToCartBottomSheet(currentProduct);
            } else {
                Toast.makeText(getContext(), "Dữ liệu chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            }
        });

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
                            product.setId(doc.getId()); // Gán ID
                            updateUI(product);
                        }
                    } else {
                        Toast.makeText(getContext(), "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi tải sản phẩm", Toast.LENGTH_SHORT).show());
    }

    private void updateUI(Product product) {
        this.currentProduct = product;

        Glide.with(requireContext())
                .load(product.getImageUrl())
                .placeholder(R.color.placeholder_bg)
                .error(R.drawable.ic_error_placeholder)
                .into(imageProduct);

        textName.setText(product.getName());
        textPrice.setText("💵 Giá: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));
        textDescription.setText("📃 Mô tả: " + product.getDescription());
        textIngredients.setText("🧪 Thành phần: " + product.getIngredients());

        if (product.getBrand() != null) {
            product.getBrand().get().addOnSuccessListener(doc -> {
                textBrand.setText("🏷️ Thương hiệu: " + (doc.exists() ? doc.getString("name") : "Không rõ"));
            }).addOnFailureListener(e -> textBrand.setText("🏷️ Thương hiệu: Không rõ"));
        }

        if (product.getCategory() != null) {
            product.getCategory().get().addOnSuccessListener(doc -> {
                textCategory.setText("🧴 Loại sản phẩm: " + (doc.exists() ? doc.getString("name") : "Không rõ"));
            }).addOnFailureListener(e -> textCategory.setText("🧴 Loại sản phẩm: Không rõ"));
        }

        if (product.getRating() > 0) {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(product.getRating());
        } else {
            ratingBar.setVisibility(View.GONE);
        }

        List<String> skinTypes = product.getSuitableSkinTypes();
        if (skinTypes != null && !skinTypes.isEmpty()) {
            List<String> cleaned = new ArrayList<>();
            for (String s : skinTypes) {
                cleaned.add(s.replace("\"", "").trim());
            }
            textSkinTypes.setText("✅ Phù hợp: " + String.join(", ", cleaned));
        } else {
            textSkinTypes.setText("Phù hợp: Không rõ");
        }
    }

    private void showAddToCartBottomSheet(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_cart, null);
        dialog.setContentView(view);

        ImageView img = view.findViewById(R.id.imageProduct);
        TextView txtName = view.findViewById(R.id.textProductName);
        TextView txtPrice = view.findViewById(R.id.textDiscountedPrice);
        TextView txtOriginal = view.findViewById(R.id.textOriginalPrice);
        TextView txtTotal = view.findViewById(R.id.textTotalPrice);
        TextView txtQuantity = view.findViewById(R.id.textQuantity);
        Button btnMinus = view.findViewById(R.id.btnMinus);
        Button btnPlus = view.findViewById(R.id.btnPlus);
        Button btnConfirm = view.findViewById(R.id.btnConfirmAddToCart);

        Glide.with(this).load(product.getImageUrl()).into(img);
        txtName.setText(product.getName());
        txtPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));
        txtOriginal.setVisibility(View.GONE);

        final int[] quantity = {1};
        txtQuantity.setText("1");
        txtTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));

        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            txtQuantity.setText(String.valueOf(quantity[0]));
            txtTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice() * quantity[0]));
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
                txtTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice() * quantity[0]));
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String cartPath = "users/" + userId + "/cartItems";
            String productId = product.getId();

            db.collection(cartPath)
                    .document(productId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            long currentQuantity = doc.getLong("quantity") != null ? doc.getLong("quantity") : 0;
                            long newQuantity = currentQuantity + quantity[0];

                            db.collection(cartPath)
                                    .document(productId)
                                    .update("quantity", newQuantity)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Cập nhật giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                        } else {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("productId", productId);
                            data.put("productName", product.getName());
                            data.put("imageUrl", product.getImageUrl());
                            data.put("price", product.getPrice());
                            data.put("quantity", quantity[0]);

                            db.collection(cartPath)
                                    .document(productId)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                        }
                    });
        });

        dialog.show();
    }
}
