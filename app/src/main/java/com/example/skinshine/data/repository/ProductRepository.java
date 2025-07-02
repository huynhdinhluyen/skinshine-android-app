package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.model.Result;

import java.util.List;

public interface ProductRepository {
    LiveData<Result<List<Product>>> getProducts();

    LiveData<Result<List<Product>>> searchProducts(String query);

    LiveData<Result<Product>> getProductById(String productId);

    LiveData<List<String>> getProductNames();

    void refreshProducts();

    LiveData<Result<List<Product>>> getProductsByCategory(String categoryId);
}