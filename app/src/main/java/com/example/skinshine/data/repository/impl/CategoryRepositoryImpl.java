package com.example.skinshine.data.repository.impl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Category;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.CategoryRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public LiveData<Result<List<Category>>> getAllCategories() {
        MutableLiveData<Result<List<Category>>> categoriesLiveData = new MutableLiveData<>();
        categoriesLiveData.setValue(Result.loading());

        firestore.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categories = new ArrayList<>();
                    Category allCategory = new Category();
                    allCategory.setId("all");
                    allCategory.setName("Tất cả");
                    categories.add(allCategory);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        categories.add(category);
                    }
                    categoriesLiveData.setValue(Result.success(categories));
                })
                .addOnFailureListener(e -> categoriesLiveData.setValue(Result.error(e.getMessage())));

        return categoriesLiveData;
    }
}
