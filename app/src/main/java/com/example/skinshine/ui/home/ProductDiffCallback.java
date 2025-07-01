package com.example.skinshine.ui.home;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.skinshine.data.model.Product;

import java.util.List;
import java.util.Objects;

public class ProductDiffCallback extends DiffUtil.Callback {

    private final List<Product> oldProductList;
    private final List<Product> newProductList;

    public ProductDiffCallback(List<Product> oldProductList, List<Product> newProductList) {
        this.oldProductList = oldProductList;
        this.newProductList = newProductList;
    }

    @Override
    public int getOldListSize() {
        return oldProductList.size();
    }

    @Override
    public int getNewListSize() {
        return newProductList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // So sánh dựa trên ID duy nhất của sản phẩm
        return Objects.equals(oldProductList.get(oldItemPosition).getId(), newProductList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Product oldProduct = oldProductList.get(oldItemPosition);
        Product newProduct = newProductList.get(newItemPosition);

        return Objects.equals(oldProduct.getName(), newProduct.getName()) &&
                Objects.equals(oldProduct.getPrice(), newProduct.getPrice()) &&
                Objects.equals(oldProduct.getImageUrl(), newProduct.getImageUrl()) &&
                Objects.equals(oldProduct.getDescription(), newProduct.getDescription());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Optional: Implement this if you want to support partial updates
        // (e.g., only update price TextView if only price changed).
        // For now, returning null means the entire item view will be rebound.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
