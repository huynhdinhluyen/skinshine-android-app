package com.example.skinshine.utils.product;

import com.example.skinshine.data.model.Product;

public class ComparisonManager {
    private static ComparisonManager instance;
    private Product currentProduct;
    private Product compareProduct;
    private OnComparisonChangeListener listener;

    private ComparisonManager() {
    }

    public static ComparisonManager getInstance() {
        if (instance == null) {
            instance = new ComparisonManager();
        }
        return instance;
    }

    public void setOnComparisonChangeListener(OnComparisonChangeListener listener) {
        this.listener = listener;
    }

    public Product getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(Product product) {
        this.currentProduct = product;
        notifyListener();
    }

    public Product getCompareProduct() {
        return compareProduct;
    }

    public void setCompareProduct(Product product) {
        // Đảm bảo không so sánh với chính nó
        if (currentProduct != null && product != null &&
                currentProduct.getId() != null &&
                currentProduct.getId().equals(product.getId())) {
            return; // Không cho phép so sánh với chính nó
        }

        this.compareProduct = product;
        notifyListener();
    }

    public boolean isComparing() {
        return currentProduct != null && compareProduct != null;
    }

    public boolean canCompare() {
        return currentProduct != null;
    }

    public void clearComparison() {
        this.currentProduct = null;
        this.compareProduct = null;
        if (listener != null) {
            listener.onComparisonCleared();
        }
    }

    private void notifyListener() {
        if (listener != null && isComparing()) {
            listener.onComparisonUpdated(currentProduct, compareProduct);
        }
    }

    public interface OnComparisonChangeListener {
        void onComparisonUpdated(Product current, Product compare);

        void onComparisonCleared();
    }
}
