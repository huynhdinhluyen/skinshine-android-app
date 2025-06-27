package com.example.skinshine.data.model;

public class ProductComparison {
    private Product currentProduct;
    private Product compareProduct;
    private boolean isComparing;

    public ProductComparison() {
        this.isComparing = false;
    }

    public ProductComparison(Product currentProduct, Product compareProduct) {
        this.currentProduct = currentProduct;
        this.compareProduct = compareProduct;
        this.isComparing = true;
    }

    public Product getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }

    public Product getCompareProduct() {
        return compareProduct;
    }

    public void setCompareProduct(Product compareProduct) {
        this.compareProduct = compareProduct;
        this.isComparing = compareProduct != null;
    }

    public boolean isComparing() {
        return isComparing && currentProduct != null && compareProduct != null;
    }

    public void clearComparison() {
        this.compareProduct = null;
        this.isComparing = false;
    }
}
