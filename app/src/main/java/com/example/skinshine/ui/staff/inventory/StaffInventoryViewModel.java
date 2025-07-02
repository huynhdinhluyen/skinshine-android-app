package com.example.skinshine.ui.staff.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Category;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.CategoryRepository;
import com.example.skinshine.data.repository.ProductRepository;
import com.example.skinshine.data.repository.impl.CategoryRepositoryImpl;
import com.example.skinshine.data.repository.impl.ProductRepositoryImpl;

import java.util.List;

public class StaffInventoryViewModel extends ViewModel {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<String> searchQueryLiveData;
    private final MutableLiveData<String> selectedCategoryLiveData;
    private final MediatorLiveData<Result<List<Product>>> productsResult;
    private final LiveData<Result<List<Category>>> categoriesResult;

    public StaffInventoryViewModel() {
        this.productRepository = new ProductRepositoryImpl();
        this.categoryRepository = new CategoryRepositoryImpl();
        this.searchQueryLiveData = new MutableLiveData<>("");
        this.selectedCategoryLiveData = new MutableLiveData<>("all");
        this.productsResult = new MediatorLiveData<>();
        this.categoriesResult = categoryRepository.getAllCategories();

        setupProductsObserver();
    }

    public LiveData<Result<List<Product>>> getProducts() {
        return productsResult;
    }

    public LiveData<Result<List<Category>>> getCategories() {
        return categoriesResult;
    }

    public void searchProducts(String query) {
        searchQueryLiveData.setValue(query);
    }

    public void filterByCategory(String categoryId) {
        selectedCategoryLiveData.setValue(categoryId);
    }

    private void setupProductsObserver() {
        // Observe search query changes
        productsResult.addSource(searchQueryLiveData, query -> updateProducts());

        // Observe category selection changes
        productsResult.addSource(selectedCategoryLiveData, categoryId -> updateProducts());
    }

    private void updateProducts() {
        String query = searchQueryLiveData.getValue();
        String categoryId = selectedCategoryLiveData.getValue();

        LiveData<Result<List<Product>>> source;

        if (query != null && !query.trim().isEmpty()) {
            // Priority: search
            source = productRepository.searchProducts(query);
        } else if (categoryId != null && !"all".equals(categoryId)) {
            // Category filter
            source = productRepository.getProductsByCategory(categoryId);
        } else {
            // Default: load all
            source = productRepository.getProducts();
        }

        productsResult.addSource(source, result -> {
            productsResult.setValue(result);
        });
    }
}
