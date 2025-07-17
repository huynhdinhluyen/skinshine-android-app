package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.CartItem;
import com.example.skinshine.data.model.Result;

import java.util.List;

public interface CartRepository {
    LiveData<Result<List<CartItem>>> getCartItems();

    void addToCart(String productId, String productName, String imageUrl, double price, int quantity, CartCallback callback);

    void updateQuantity(String productId, int newQuantity, CartCallback callback);

    void removeItem(String productId, CartCallback callback);

    void removeSelectedItems(List<String> productIds, CartCallback callback);

    void clearCart(CartCallback callback);

    void refreshCart();

    interface CartCallback {
        void onSuccess();

        void onError(String error);
    }
}
