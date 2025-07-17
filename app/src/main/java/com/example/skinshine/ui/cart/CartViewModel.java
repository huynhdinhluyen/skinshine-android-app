package com.example.skinshine.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.CartItem;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.CartRepository;
import com.example.skinshine.data.repository.impl.CartRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {
    private final CartRepository cartRepository;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MediatorLiveData<Integer> cartItemCount;

    public CartViewModel() {
        this.cartRepository = new CartRepositoryImpl();
        this.errorLiveData = new MutableLiveData<>();
        this.loadingLiveData = new MutableLiveData<>(false);
        this.cartItemCount = new MediatorLiveData<>();

        // FIX: Handle Result wrapper correctly
        cartItemCount.addSource(cartRepository.getCartItems(), result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                int total = 0;
                for (CartItem item : result.getData()) {
                    total += item.getQuantity();
                }
                cartItemCount.setValue(total);
            } else {
                cartItemCount.setValue(0);
            }
        });
    }

    public LiveData<Result<List<CartItem>>> getCartItems() {
        return cartRepository.getCartItems();
    }

    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public void addToCart(String productId, String productName, String imageUrl, double price, int quantity) {
        loadingLiveData.setValue(true);
        cartRepository.addToCart(productId, productName, imageUrl, price, quantity, new CartRepository.CartCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateQuantity(String productId, int newQuantity) {
        cartRepository.updateQuantity(productId, newQuantity, new CartRepository.CartCallback() {
            @Override
            public void onSuccess() {
                // Success handled by listener
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void removeItem(String productId) {
        cartRepository.removeItem(productId, new CartRepository.CartCallback() {
            @Override
            public void onSuccess() {
                // Success handled by listener
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void removeSelectedItems(List<CartItem> selectedItems) {
        List<String> productIds = new ArrayList<>();
        for (CartItem item : selectedItems) {
            productIds.add(item.getProductId());
        }

        loadingLiveData.setValue(true);
        cartRepository.removeSelectedItems(productIds, new CartRepository.CartCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void refreshCartItems() {
        cartRepository.refreshCart();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (cartRepository instanceof CartRepositoryImpl) {
            ((CartRepositoryImpl) cartRepository).detachListener();
        }
    }
}