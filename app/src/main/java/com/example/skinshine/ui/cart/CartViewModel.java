package com.example.skinshine.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CartViewModel extends ViewModel {

    private final MutableLiveData<Integer> cartItemCount = new MutableLiveData<>(0);
    private ListenerRegistration cartListener;

    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }

    public void attachCartListener() {
        detachCartListener();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            cartItemCount.postValue(0);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        cartListener = db.collection("users").document(userId).collection("cartItems")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        cartItemCount.postValue(0);
                        return;
                    }

                    if (snapshots != null) {
                        int totalItems = 0;
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Long quantity = doc.getLong("quantity");
                            if (quantity != null) {
                                totalItems += quantity.intValue();
                            }
                        }
                        cartItemCount.postValue(totalItems);
                    }
                });
    }

    public void detachCartListener() {
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
        cartItemCount.postValue(0);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        detachCartListener();
    }
}