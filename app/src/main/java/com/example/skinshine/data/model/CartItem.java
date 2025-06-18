package com.example.skinshine.data.model;

import com.google.firebase.firestore.Exclude;

public class CartItem {
    private String productId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private double price;

    @Exclude
    private boolean selected = false;

    public CartItem() {
    }

    public CartItem(String productId, String productName, String imageUrl, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
    }

    // ✅ Getter/Setter cho "selected"
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // Other GETTER & SETTER
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
