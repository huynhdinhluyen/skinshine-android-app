package com.example.skinshine.ui.category;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.Category;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.ui.home.ProductAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoryContentFragment extends Fragment {

    private RecyclerView recyclerCategory, recyclerProducts;
    private FirebaseFirestore db;

    public CategoryContentFragment() {
        super(R.layout.fragment_category_content);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerCategory = view.findViewById(R.id.recyclerCategory);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);
        db = FirebaseFirestore.getInstance();

        loadCategories();
    }

    private void loadCategories() {
        db.collection("categories").get()
                .addOnSuccessListener(snapshot -> {
                    List<Category> categories = new ArrayList<>();
                    for (var doc : snapshot) {
                        Category c = doc.toObject(Category.class);
                        c.setId(doc.getId());
                        categories.add(c);
                    }

                    CategoryAdapter adapter = new CategoryAdapter(categories, selected -> {
                        loadProductsByCategory(selected.getId());
                    });

                    recyclerCategory.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerCategory.setAdapter(adapter);

                    if (!categories.isEmpty()) {
                        loadProductsByCategory(categories.get(0).getId());
                    }
                })
                .addOnFailureListener(e -> Log.e("CategoryLoad", "Lỗi tải category: " + e.getMessage()));
    }

    private void loadProductsByCategory(String categoryId) {
        DocumentReference ref = db.collection("categories").document(categoryId);
        db.collection("products")
                .whereEqualTo("category", ref)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (var doc : snapshot) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        products.add(p);
                    }

                    ProductAdapter productAdapter = new ProductAdapter(getContext(), products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getId());
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        navController.navigate(R.id.productDetailFragment, bundle);
                    });


                    recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerProducts.setAdapter(productAdapter);
                });
    }
}
