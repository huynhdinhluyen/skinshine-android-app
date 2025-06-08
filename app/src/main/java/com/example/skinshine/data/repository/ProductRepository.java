package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Product;

import java.util.List;

public interface ProductRepository {
    LiveData<List<BannerItem>> getBanners();
    LiveData<List<Product>> getProducts();
}