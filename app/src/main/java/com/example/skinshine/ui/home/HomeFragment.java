package com.example.skinshine.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.databinding.FragmentHomeBinding;
import com.example.skinshine.ui.cart.CartViewModel;
import com.example.skinshine.ui.login.LoginActivity;
import com.example.skinshine.utils.CartBadgeHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private BannerAdapter bannerAdapter;
    private ProductAdapter productAdapter;
    private CartViewModel cartViewModel;
    private Timer bannerTimer;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupToolbar();
        setupBannerViewPager();
        setupProductRecyclerView();
        observeViewModel();
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        return root;
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbarHome;
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }


    private void setupBannerViewPager() {
        bannerAdapter = new BannerAdapter(getContext(), new ArrayList<>());
        binding.viewPagerBanner.setAdapter(bannerAdapter);
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this::onProductClicked);
        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewProducts.setAdapter(productAdapter);
        binding.recyclerViewProducts.setNestedScrollingEnabled(false);
    }

    private void onProductClicked(Product product) {
        NavController navController = Navigation.findNavController(
                requireActivity(),
                R.id.nav_host_fragment_activity_main
        );
        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getId());
        navController.navigate(R.id.productDetailFragment, bundle);
    }

    private void observeViewModel() {
        binding.progressBarHome.setVisibility(View.VISIBLE);

        homeViewModel.getBanners().observe(getViewLifecycleOwner(), banners -> {
            if (banners != null && !banners.isEmpty()) {
                bannerAdapter.updateBanners(banners);
                startBannerAutoSlide(banners.size());
            }
            if (homeViewModel.getProducts().getValue() != null || banners == null) {
                checkAndHideProgressBar();
            }
        });

        homeViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.updateProducts(products);
            }
            if (homeViewModel.getBanners().getValue() != null || products == null) {
                checkAndHideProgressBar();
            }
        });
    }
    private void checkAndHideProgressBar() {
        boolean bannersLoadedOrFailed = homeViewModel.getBanners().getValue() != null || (homeViewModel.getBanners().getValue() == null && bannerAdapter.getItemCount() == 0);
        boolean productsLoadedOrFailed = homeViewModel.getProducts().getValue() != null || (homeViewModel.getProducts().getValue() == null && productAdapter.getItemCount() == 0);

        if (bannersLoadedOrFailed && productsLoadedOrFailed) {
            binding.progressBarHome.setVisibility(View.GONE);
        }
    }


    private void startBannerAutoSlide(int numberOfBanners) {
        if (numberOfBanners <= 1) return;

        stopBannerAutoSlide(); 
        bannerTimer = new Timer();
        long BANNER_SLIDE_DELAY = 3000;
        long BANNER_SLIDE_PERIOD = 5000;
        bannerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                bannerHandler.post(() -> {
                    int currentItem = binding.viewPagerBanner.getCurrentItem();
                    int nextItem = (currentItem + 1) % numberOfBanners;
                    binding.viewPagerBanner.setCurrentItem(nextItem, true);
                });
            }
        }, BANNER_SLIDE_DELAY, BANNER_SLIDE_PERIOD);
    }

    private void stopBannerAutoSlide() {
        if (bannerTimer != null) {
            bannerTimer.cancel();
            bannerTimer = null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View cartBadgeContainer = view.findViewById(R.id.cartBadgeContainer);
        if (cartBadgeContainer != null) {
            TextView cartBadge = cartBadgeContainer.findViewById(R.id.cartBadge);
            cartViewModel.getCartItemCount().observe(getViewLifecycleOwner(), count -> {
                if (count != null && count > 0) {
                    cartBadge.setVisibility(View.VISIBLE);
                    cartBadge.setText(String.valueOf(count));
                } else {
                    cartBadge.setVisibility(View.GONE);
                }
            });

            ImageView iconCart = cartBadgeContainer.findViewById(R.id.iconCart);
            if (iconCart != null) {
                iconCart.setOnClickListener(v -> {
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();

                        try {
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                            navController.navigate(R.id.action_homeFragment_to_loginFragment);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error navigating to login: " + e.getMessage(), e);
                            try {
                                Intent intent = new Intent(getActivity(), com.example.skinshine.ui.login.LoginActivity.class);
                                startActivity(intent);
                            } catch (Exception ex) {
                                Log.e("HomeFragment", "Fallback failed: " + ex.getMessage(), ex);
                            }
                        }
                    } else {
                        try {
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                            navController.navigate(R.id.cartFragment);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error navigating to cart: " + e.getMessage(), e);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        View cartBadgeContainer = getView().findViewById(R.id.cartBadgeContainer);
        if (cartBadgeContainer != null) {
            CartBadgeHelper.updateCartBadge(cartBadgeContainer);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerAutoSlide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopBannerAutoSlide();
        binding = null;
    }
}