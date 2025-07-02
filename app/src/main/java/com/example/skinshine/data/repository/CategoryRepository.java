package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Category;
import com.example.skinshine.data.model.Result;

import java.util.List;

public interface CategoryRepository {
    LiveData<Result<List<Category>>> getAllCategories();
}
