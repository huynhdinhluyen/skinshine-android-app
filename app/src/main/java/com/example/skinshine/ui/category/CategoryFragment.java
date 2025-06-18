package com.example.skinshine.ui.category;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.skinshine.R;

public class CategoryFragment extends Fragment {

    private TextView tabCategory, tabBrand;
    private FrameLayout contentContainer;

    public CategoryFragment() {
        super(R.layout.fragment_category);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        tabCategory = view.findViewById(R.id.tabCategory);
        tabBrand = view.findViewById(R.id.tabBrand);
        contentContainer = view.findViewById(R.id.contentContainer);

        // Mặc định hiển thị tab "Danh mục"
        showCategoryContent();

        // Xử lý sự kiện tab
        tabCategory.setOnClickListener(v -> showCategoryContent());
        tabBrand.setOnClickListener(v -> showBrandContent());
    }

    private void showCategoryContent() {
        tabCategory.setTextColor(Color.BLACK);
        tabBrand.setTextColor(Color.GRAY);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, new CategoryContentFragment())
                .commit();
    }

    private void showBrandContent() {
        tabCategory.setTextColor(Color.GRAY);
        tabBrand.setTextColor(Color.BLACK);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.contentContainer, new BrandContentFragment())
                .commit();
    }
}
