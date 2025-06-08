package com.example.skinshine.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.repository.ProductRepository;
import com.example.skinshine.data.repository.ProductRepositoryImpl;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final LiveData<List<BannerItem>> banners;
    private final LiveData<List<Product>> products;

    public HomeViewModel() {
        ProductRepository productRepository = new ProductRepositoryImpl();
        this.banners = productRepository.getBanners();
        this.products = productRepository.getProducts();
    }

    public LiveData<List<BannerItem>> getBanners() {
        return banners;
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }
}