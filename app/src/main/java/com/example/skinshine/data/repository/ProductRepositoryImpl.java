package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<BannerItem>> bannersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>();

    public ProductRepositoryImpl() {
        fetchBanners();
        fetchProducts();
    }

    private void fetchBanners() {
        db.collection("banner_items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BannerItem> bannerList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BannerItem banner = document.toObject(BannerItem.class);
                            bannerList.add(banner);
                        }
                        bannersLiveData.setValue(bannerList);
                    } else {
                        bannersLiveData.setValue(null);
                    }
                });
    }

    private void fetchProducts() {
        db.collection("products")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> productList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }
                        productsLiveData.setValue(productList);
                    } else {
                        productsLiveData.setValue(null);
                    }
                });
    }

    @Override
    public LiveData<List<BannerItem>> getBanners() {
        return bannersLiveData;
    }

    @Override
    public LiveData<List<Product>> getProducts() {
        return productsLiveData;
    }
}
