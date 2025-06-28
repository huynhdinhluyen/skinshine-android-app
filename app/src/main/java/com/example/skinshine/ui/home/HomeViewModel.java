package com.example.skinshine.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.BannerRepository;
import com.example.skinshine.data.repository.ProductRepository;
import com.example.skinshine.data.repository.impl.BannerRepositoryImpl;
import com.example.skinshine.data.repository.impl.ProductRepositoryImpl;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final ProductRepository productRepository;
    private final BannerRepository bannerRepository;
    private final LiveData<Result<List<BannerItem>>> banners;
    private final LiveData<Result<List<Product>>> products;
    private final MediatorLiveData<Boolean> isLoading;
    private final MediatorLiveData<String> errorMessage;

    public HomeViewModel() {
        this.bannerRepository = new BannerRepositoryImpl();
        this.banners = bannerRepository.getBanners();

        this.productRepository = new ProductRepositoryImpl();
        this.products = productRepository.getProducts();

        this.isLoading = new MediatorLiveData<>();
        this.errorMessage = new MediatorLiveData<>();

        isLoading.addSource(banners, result -> {
            if (result != null) {
                updateLoadingState();
            }
        });

        isLoading.addSource(products, result -> {
            if (result != null) {
                updateLoadingState();
            }
        });

        // Observe error state
        errorMessage.addSource(banners, result -> {
            if (result != null && result.isError()) {
                errorMessage.setValue("Banner error: " + result.getMessage());
            }
        });

        errorMessage.addSource(products, result -> {
            if (result != null && result.isError()) {
                errorMessage.setValue("Product error: " + result.getMessage());
            } else if (result != null && result.isSuccess()) {
                errorMessage.setValue(null); // Clear error when successful
            }
        });
    }

    private void updateLoadingState() {
        Result<List<BannerItem>> bannerResult = banners.getValue();
        Result<List<Product>> productResult = products.getValue();

        boolean isLoadingBanners = bannerResult != null && bannerResult.isLoading();
        boolean isLoadingProducts = productResult != null && productResult.isLoading();

        isLoading.setValue(isLoadingBanners || isLoadingProducts);
    }

    public LiveData<Result<List<BannerItem>>> getBanners() {
        return banners;
    }

    public LiveData<Result<List<Product>>> getProducts() {
        return products;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshProducts() {
        productRepository.refreshProducts();
    }

    public void refreshBanners() {
        if (bannerRepository instanceof BannerRepositoryImpl) {
            ((BannerRepositoryImpl) bannerRepository).detachListener();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (productRepository instanceof ProductRepositoryImpl) {
            ((ProductRepositoryImpl) productRepository).detachListener();
        }
        if (bannerRepository instanceof BannerRepositoryImpl) {
            ((BannerRepositoryImpl) bannerRepository).detachListener();
        }
    }
}