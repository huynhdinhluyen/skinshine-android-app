package com.example.skinshine.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.model.ProductComparison;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.ProductRepository;
import com.example.skinshine.data.repository.impl.ProductRepositoryImpl;

import java.util.Iterator;
import java.util.List;

public class ProductComparisonViewModel extends ViewModel {
    private final ProductRepository productRepository;
    private final MutableLiveData<ProductComparison> comparisonLiveData;
    private final MediatorLiveData<List<Product>> searchResultsLiveData;
    private final MediatorLiveData<Boolean> isSearchingLiveData;
    private final MediatorLiveData<String> errorLiveData;
    private final LiveData<List<String>> productNames;

    public ProductComparisonViewModel() {
        this.productRepository = new ProductRepositoryImpl();
        this.comparisonLiveData = new MutableLiveData<>(new ProductComparison());
        this.searchResultsLiveData = new MediatorLiveData<>();
        this.isSearchingLiveData = new MediatorLiveData<>();
        this.errorLiveData = new MediatorLiveData<>();
        productNames = productRepository.getProductNames();
    }

    public LiveData<ProductComparison> getComparison() {
        return comparisonLiveData;
    }

    public LiveData<List<String>> getProductNames() {
        return productNames;
    }

    public LiveData<List<Product>> getSearchResults() {
        return searchResultsLiveData;
    }

    public LiveData<Boolean> getIsSearching() {
        return isSearchingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void setCurrentProduct(Product product) {
        ProductComparison current = comparisonLiveData.getValue();
        if (current != null) {
            current.setCurrentProduct(product);
            comparisonLiveData.setValue(current);
        }
    }

    public void setCompareProduct(Product product) {
        ProductComparison current = comparisonLiveData.getValue();
        if (current != null) {
            current.setCompareProduct(product);
            comparisonLiveData.setValue(current);
        }
    }

    public void clearComparison() {
        ProductComparison current = comparisonLiveData.getValue();
        if (current != null) {
            current.clearComparison();
            comparisonLiveData.setValue(current);
        }
        searchResultsLiveData.setValue(null);
    }

    public void searchProducts(String query) {
        LiveData<Result<List<Product>>> searchResult = productRepository.searchProducts(query);

        // Remove previous source if exists
        searchResultsLiveData.removeSource(searchResult);
        isSearchingLiveData.removeSource(searchResult);
        errorLiveData.removeSource(searchResult);

        // Add new sources
        searchResultsLiveData.addSource(searchResult, result -> {
            if (result != null && result.isSuccess()) {
                List<Product> products = result.getData();

                // Filter out current product
                ProductComparison current = comparisonLiveData.getValue();
                if (current != null && current.getCurrentProduct() != null && products != null) {
                    // Use iterator for Android compatibility
                    Iterator<Product> iterator = products.iterator();
                    while (iterator.hasNext()) {
                        Product p = iterator.next();
                        if (p.getId() != null && p.getId().equals(current.getCurrentProduct().getId())) {
                            iterator.remove();
                        }
                    }
                }

                searchResultsLiveData.setValue(products);
            }
        });

        isSearchingLiveData.addSource(searchResult, result -> {
            if (result != null) {
                isSearchingLiveData.setValue(result.isLoading());
            }
        });

        errorLiveData.addSource(searchResult, result -> {
            if (result != null && result.isError()) {
                errorLiveData.setValue(result.getMessage());
            } else {
                errorLiveData.setValue(null);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (productRepository instanceof ProductRepositoryImpl) {
            ((ProductRepositoryImpl) productRepository).detachListener();
        }
    }
}