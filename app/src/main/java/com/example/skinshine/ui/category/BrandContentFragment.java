package com.example.skinshine.ui.category;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.Brand;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.ui.home.ProductAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BrandContentFragment extends Fragment {

    private RecyclerView recyclerBrand, recyclerProducts;
    private BrandAdapter brandAdapter;
    private ProductAdapter productAdapter;

    private FirebaseFirestore db;

    public BrandContentFragment() {
        super(R.layout.fragment_brand_content);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerBrand = view.findViewById(R.id.recyclerBrand);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);
        db = FirebaseFirestore.getInstance();

        loadBrands();
    }

    private void loadBrands() {
        db.collection("brands").get()
                .addOnSuccessListener(snapshot -> {
                    List<Brand> brands = new ArrayList<>();
                    for (var doc : snapshot) {
                        Brand b = doc.toObject(Brand.class);
                        b.setId(doc.getId());
                        brands.add(b);
                    }

                    brandAdapter = new BrandAdapter(brands, brand -> {
                        loadProductsByBrand(brand.getId());
                    });

                    recyclerBrand.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerBrand.setAdapter(brandAdapter);

                    if (!brands.isEmpty()) {
                        loadProductsByBrand(brands.get(0).getId());
                    }
                });
    }

    private void loadProductsByBrand(String brandId) {
        DocumentReference brandRef = db.collection("brands").document(brandId);

        db.collection("products")
                .whereEqualTo("brand", brandRef)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (var doc : snapshot) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        products.add(p);
                    }

                    productAdapter = new ProductAdapter(getContext(), products, product -> {
                        // TODO: xử lý khi nhấn vào sản phẩm
                    });

                    recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerProducts.setAdapter(productAdapter);
                });
    }
}
