package com.example.skinshine.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.databinding.FragmentHomeBinding;
import com.example.skinshine.ui.cart.CartViewModel;
import com.example.skinshine.utils.cart.CartBadgeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CartViewModel cartViewModel;
    private ProductAdapter productAdapter;
    private BannerAdapter bannerAdapter;
    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private Timer bannerTimer;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupToolbar();
        setupBannerViewPager();
        setupProductRecyclerView();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        setupRecyclerView();
        setupCartBadge(view);
        observeViewModel();
    }

    private void initViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBarHome);
        // Add error TextView to your layout if not exists
        // errorTextView = view.findViewById(R.id.textViewError);
    }

    private void setupViewModels() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this::navigateToProductDetail);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupCartBadge(View view) {
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
            iconCart.setOnClickListener(v -> navigateToCart());
        }
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
        // Observe banners
        homeViewModel.getBanners().observe(getViewLifecycleOwner(), this::handleBannerResult);

        // Observe products với Result wrapper
        homeViewModel.getProducts().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess() && result.getData() != null) {
                    productAdapter.updateProducts(result.getData());
                    showContent();
                } else if (result.isError()) {
                    showError(result.getMessage());
                } else if (result.isLoading()) {
                    showLoading();
                }
            }
        });

        // Observe loading state
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showLoading();
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void handleBannerResult(Result<List<BannerItem>> result) {
        if (result != null) {
            if (result.isSuccess() && result.getData() != null) {
                List<BannerItem> banners = result.getData();
                bannerAdapter.updateBanners(banners);

                if (banners.size() > 1) {
                    startBannerAutoSlide(banners.size());
                }

                Log.d("HomeFragment", "Banners loaded: " + banners.size());
            } else if (result.isError()) {
                Log.e("HomeFragment", "Banner error: " + result.getMessage());
                binding.viewPagerBanner.setVisibility(View.GONE);
            } else if (result.isLoading()) {
                Log.d("HomeFragment", "Loading banners...");
            }
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewProducts.setVisibility(View.GONE);
        if (errorTextView != null) {
            errorTextView.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.VISIBLE);
        binding.viewPagerBanner.setVisibility(View.VISIBLE); // Show banner
        if (errorTextView != null) {
            errorTextView.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.GONE);

        if (errorTextView != null) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(message);
        } else {
            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProductDetail(Product product) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getId());

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.productDetailFragment, bundle);
        } catch (Exception e) {
            Log.e("HomeFragment", "Error navigating to product detail", e);
            Toast.makeText(getContext(), "Không thể mở chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToCart() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.cartFragment);
        } catch (Exception e) {
            Log.e("HomeFragment", "Error navigating to cart", e);
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
                    if (binding != null) {
                        int currentItem = binding.viewPagerBanner.getCurrentItem();
                        int nextItem = (currentItem + 1) % numberOfBanners;
                        binding.viewPagerBanner.setCurrentItem(nextItem, true);
                    }
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
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            View cartBadgeContainer = getView().findViewById(R.id.cartBadgeContainer);
            if (cartBadgeContainer != null) {
                CartBadgeHelper.updateCartBadge(cartBadgeContainer);
            }
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