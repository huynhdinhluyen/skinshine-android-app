package com.example.skinshine.data.repository.impl;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.ProductRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {
    private static final String TAG = "ProductRepository";
    private final FirebaseFirestore db;
    private final MutableLiveData<Result<List<Product>>> productsLiveData;
    private final MutableLiveData<Result<List<Product>>> searchResultsLiveData;
    private ListenerRegistration productsListener;

    public ProductRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.productsLiveData = new MutableLiveData<>();
        this.searchResultsLiveData = new MutableLiveData<>();
        attachProductsListener();
    }

    @Override
    public LiveData<Result<List<Product>>> getProducts() {
        return productsLiveData;
    }

    @Override
    public LiveData<Result<List<Product>>> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResultsLiveData.setValue(Result.success(new ArrayList<>()));
            return searchResultsLiveData;
        }

        searchResultsLiveData.setValue(Result.loading());
        String searchQuery = query.trim().toLowerCase();

        db.collection("products")
                .whereEqualTo("is_active", true)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> filteredProducts = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());

                            if (product.getName() != null) {
                                String productName = product.getName().toLowerCase();
                                if (productName.contains(searchQuery)) {
                                    filteredProducts.add(product);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing product: " + e.getMessage());
                        }
                    }

                    searchResultsLiveData.setValue(Result.success(filteredProducts));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Search failed: " + e.getMessage());
                    searchResultsLiveData.setValue(Result.error(e.getMessage()));
                });

        return searchResultsLiveData;
    }

    @Override
    public LiveData<Result<Product>> getProductById(String productId) {
        MutableLiveData<Result<Product>> productLiveData = new MutableLiveData<>();
        productLiveData.setValue(Result.loading());

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        try {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                productLiveData.setValue(Result.success(product));
                            } else {
                                productLiveData.setValue(Result.error("Product data is null"));
                            }
                        } catch (Exception e) {
                            productLiveData.setValue(Result.error("Error parsing product: " + e.getMessage()));
                        }
                    } else {
                        productLiveData.setValue(Result.error("Product not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    productLiveData.setValue(Result.error(e.getMessage()));
                });

        return productLiveData;
    }

    @Override
    public void refreshProducts() {
        // Force refresh by detaching and reattaching listener
        if (productsListener != null) {
            productsListener.remove();
        }
        attachProductsListener();
    }

    private void attachProductsListener() {
        if (productsListener != null) {
            productsListener.remove();
        }

        productsLiveData.setValue(Result.loading());

        productsListener = db.collection("products")
                .whereEqualTo("is_active", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to products", error);
                        productsLiveData.setValue(Result.error(error.getMessage()));
                        return;
                    }

                    if (snapshots != null) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                Product product = doc.toObject(Product.class);
                                product.setId(doc.getId());
                                products.add(product);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing product: " + e.getMessage());
                            }
                        }
                        productsLiveData.setValue(Result.success(products));
                    }
                });
    }

    public void detachListener() {
        if (productsListener != null) {
            productsListener.remove();
            productsListener = null;
        }
    }
}