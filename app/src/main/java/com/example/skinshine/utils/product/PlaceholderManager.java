package com.example.skinshine.utils.product;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;

import com.example.skinshine.data.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaceholderManager {
    private static final String TAG = "PlaceholderManager";
    private static final int PLACEHOLDER_INTERVAL = 4000; // 4 seconds

    private final EditText editText;
    private final Handler handler;
    private final List<String> productNames;
    private final Random random;
    private Runnable placeholderRunnable;
    private boolean isActive = false;

    public PlaceholderManager(EditText editText) {
        this.editText = editText;
        this.handler = new Handler(Looper.getMainLooper());
        this.productNames = new ArrayList<>();
        this.random = new Random();
        loadProductNames();
    }

    private void loadProductNames() {
        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("is_active", true)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productNames.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Product product = doc.toObject(Product.class);
                            if (product.getName() != null && !product.getName().trim().isEmpty()) {
                                productNames.add(product.getName());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading product name: " + e.getMessage());
                        }
                    }

                    if (productNames.isEmpty()) {
                        productNames.add("Kem dưỡng ẩm");
                        productNames.add("Serum vitamin C");
                        productNames.add("Sữa rửa mặt");
                        productNames.add("Kem chống nắng");
                        productNames.add("Toner làm sạch");
                    }

                    Log.d(TAG, "Loaded " + productNames.size() + " product names for placeholder");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load product names: " + e.getMessage());
                    productNames.add("Kem dưỡng ẩm");
                    productNames.add("Serum vitamin C");
                    productNames.add("Sữa rửa mặt");
                });
    }

    public void startRotatingPlaceholder() {
        if (isActive || productNames.isEmpty()) {
            return;
        }

        isActive = true;
        placeholderRunnable = new Runnable() {
            @Override
            public void run() {
                if (isActive && editText.getText().toString().trim().isEmpty()) {
                    // Chỉ thay đổi placeholder khi ô input rỗng
                    String randomProductName = getRandomProductName();
                    editText.setHint("Tìm \"" + randomProductName + "\"...");
                }

                if (isActive) {
                    handler.postDelayed(this, PLACEHOLDER_INTERVAL);
                }
            }
        };

        // Set placeholder đầu tiên ngay lập tức
        String initialProduct = getRandomProductName();
        editText.setHint("Tìm \"" + initialProduct + "\"...");

        handler.postDelayed(placeholderRunnable, PLACEHOLDER_INTERVAL);
    }

    public void stopRotatingPlaceholder() {
        isActive = false;
        if (placeholderRunnable != null) {
            handler.removeCallbacks(placeholderRunnable);
        }
        editText.setHint("Nhập tên sản phẩm...");
    }

    private String getRandomProductName() {
        if (productNames.isEmpty()) {
            return "sản phẩm yêu thích";
        }
        return productNames.get(random.nextInt(productNames.size()));
    }

    public void refreshProductNames() {
        loadProductNames();
    }
}
