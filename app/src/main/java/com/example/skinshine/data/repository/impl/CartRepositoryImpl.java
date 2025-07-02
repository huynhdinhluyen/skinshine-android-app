package com.example.skinshine.data.repository.impl;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.CartItem;
import com.example.skinshine.data.repository.CartRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartRepositoryImpl implements CartRepository {
    private static final String TAG = "CartRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<List<CartItem>> cartItemsLiveData;
    private ListenerRegistration cartListener;

    public CartRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.cartItemsLiveData = new MutableLiveData<>();
        attachCartListener();
    }

    @Override
    public LiveData<List<CartItem>> getCartItems() {
        return cartItemsLiveData;
    }

    private void attachCartListener() {
        if (auth.getCurrentUser() == null) {
            cartItemsLiveData.setValue(new ArrayList<>());
            return;
        }
        detachListener();

        String userId = auth.getCurrentUser().getUid();
        if (cartListener != null) {
            cartListener.remove();
        }

        cartListener = db.collection("users")
                .document(userId)
                .collection("cartItems")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to cart changes", error);
                        return;
                    }

                    if (snapshots != null) {
                        List<CartItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            CartItem item = doc.toObject(CartItem.class);
                            items.add(item);
                        }
                        cartItemsLiveData.setValue(items);
                    }
                });
    }

    @Override
    public void addToCart(String productId, String productName, String imageUrl, double price, int quantity, CartCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String cartPath = "users/" + userId + "/cartItems";

        db.collection(cartPath)
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Update existing item
                        long currentQuantity = doc.getLong("quantity") != null ? doc.getLong("quantity") : 0;
                        long newQuantity = currentQuantity + quantity;

                        db.collection(cartPath)
                                .document(productId)
                                .update("quantity", newQuantity)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        // Add new item
                        Map<String, Object> data = new HashMap<>();
                        data.put("productId", productId);
                        data.put("productName", productName);
                        data.put("imageUrl", imageUrl);
                        data.put("price", price);
                        data.put("quantity", quantity);

                        db.collection(cartPath)
                                .document(productId)
                                .set(data)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateQuantity(String productId, int newQuantity, CartCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("cartItems")
                .document(productId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void removeItem(String productId, CartCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("cartItems")
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void removeSelectedItems(List<String> productIds, CartCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        WriteBatch batch = db.batch();

        for (String productId : productIds) {
            batch.delete(db.collection("users")
                    .document(userId)
                    .collection("cartItems")
                    .document(productId));
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void clearCart(CartCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("cartItems")
                .get()
                .addOnSuccessListener(snapshots -> {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void refreshCart() {
        detachListener();
        attachCartListener();
    }

    public void detachListener() {
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
    }

    public void onAuthStateChanged() {
        attachCartListener();
    }
}