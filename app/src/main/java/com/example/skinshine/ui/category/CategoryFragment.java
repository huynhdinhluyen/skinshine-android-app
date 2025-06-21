package com.example.skinshine.ui.category;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.skinshine.R;
import com.example.skinshine.utils.CartBadgeHelper;

public class CategoryFragment extends Fragment {

    private TextView tabCategory, tabBrand;
    private FrameLayout contentContainer;

    public CategoryFragment() {
        super(R.layout.fragment_category);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View cartBadgeContainer = view.findViewById(R.id.cartBadgeContainer);
        ImageView iconCart = cartBadgeContainer.findViewById(R.id.iconCart);

        iconCart.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_cart);
        });

        CartBadgeHelper.updateCartBadge(cartBadgeContainer);
    }

    @Override
    public void onResume() {
        super.onResume();
        View cartBadgeContainer = getView().findViewById(R.id.cartBadgeContainer);
        if (cartBadgeContainer != null) {
            CartBadgeHelper.updateCartBadge(cartBadgeContainer);
        }
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
