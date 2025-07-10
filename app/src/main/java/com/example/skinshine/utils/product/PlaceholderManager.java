package com.example.skinshine.utils.product;

import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaceholderManager {
    private static final int PLACEHOLDER_INTERVAL = 4000;

    private final EditText editText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<String> productNames = Collections.synchronizedList(new ArrayList<>());
    private Runnable placeholderRunnable;
    private boolean isActive = false;

    public PlaceholderManager(EditText editText) {
        this.editText = editText;
    }

    public void setProductNames(List<String> names) {
        synchronized (productNames) {
            productNames.clear();
            if (names != null && !names.isEmpty()) {
                productNames.addAll(names);
            } else {
                productNames.add("Kem dưỡng ẩm");
                productNames.add("Serum vitamin C");
            }
        }
        if (isActive) {
            startRotatingPlaceholder();
        }
    }

    public void startRotatingPlaceholder() {
        if (isActive) return;
        isActive = true;

        placeholderRunnable = new Runnable() {
            @Override
            public void run() {
                if (isActive && editText.getText().toString().isEmpty()) {
                    editText.setHint("Nhập tên sản phẩm...");
                }
                if (isActive) {
                    handler.postDelayed(this, PLACEHOLDER_INTERVAL);
                }
            }
        };
        handler.post(placeholderRunnable);
    }

    public void stopRotatingPlaceholder() {
        isActive = false;
        if (placeholderRunnable != null) {
            handler.removeCallbacks(placeholderRunnable);
        }
        editText.setHint("Nhập tên sản phẩm...");
    }
}