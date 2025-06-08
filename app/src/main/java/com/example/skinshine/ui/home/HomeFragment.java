package com.example.skinshine.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.skinshine.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private BannerAdapter bannerAdapter;
    private ProductAdapter productAdapter;
    private Timer bannerTimer;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupToolbar();
        setupBannerViewPager();
        setupProductRecyclerView();
        observeViewModel();

        return root;
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbarHome;
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        // ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
    }


    private void setupBannerViewPager() {
        bannerAdapter = new BannerAdapter(getContext(), new ArrayList<>());
        binding.viewPagerBanner.setAdapter(bannerAdapter);
        // Optional: Add page transformer for animations
        // binding.viewPagerBanner.setPageTransformer(new ZoomOutPageTransformer());
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>() /*, this::onProductClicked */);
        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 cột
        binding.recyclerViewProducts.setAdapter(productAdapter);
        binding.recyclerViewProducts.setNestedScrollingEnabled(false); // Quan trọng khi RecyclerView trong NestedScrollView
    }

    // private void onProductClicked(Product product) {
    // Navigate to product detail screen
    // NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
    // HomeFragmentDirections.ActionNavigationHomeToProductDetailFragment action =
    //        HomeFragmentDirections.actionNavigationHomeToProductDetailFragment(product.getId());
    // navController.navigate(action);
    // }

    private void observeViewModel() {
        binding.progressBarHome.setVisibility(View.VISIBLE);

        homeViewModel.getBanners().observe(getViewLifecycleOwner(), banners -> {
            if (banners != null && !banners.isEmpty()) {
                bannerAdapter.updateBanners(banners);
                startBannerAutoSlide(banners.size());
            }
            // Ẩn ProgressBar khi cả hai đều đã tải xong hoặc có lỗi
            if (homeViewModel.getProducts().getValue() != null || banners == null) {
                checkAndHideProgressBar();
            }
        });

        homeViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.updateProducts(products);
            }
            // Ẩn ProgressBar khi cả hai đều đã tải xong hoặc có lỗi
            if (homeViewModel.getBanners().getValue() != null || products == null) {
                checkAndHideProgressBar();
            }
        });
    }
    private void checkAndHideProgressBar() {
        // Chỉ ẩn ProgressBar nếu cả banner và product đều đã có dữ liệu (hoặc null nếu lỗi)
        boolean bannersLoadedOrFailed = homeViewModel.getBanners().getValue() != null || (homeViewModel.getBanners().getValue() == null && bannerAdapter.getItemCount() == 0);
        boolean productsLoadedOrFailed = homeViewModel.getProducts().getValue() != null || (homeViewModel.getProducts().getValue() == null && productAdapter.getItemCount() == 0);

        if (bannersLoadedOrFailed && productsLoadedOrFailed) {
            binding.progressBarHome.setVisibility(View.GONE);
        }
    }


    private void startBannerAutoSlide(int numberOfBanners) {
        if (numberOfBanners <= 1) return;

        stopBannerAutoSlide(); // Dừng timer cũ nếu có
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
    public void onPause() {
        super.onPause();
        stopBannerAutoSlide();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
            startBannerAutoSlide(bannerAdapter.getItemCount());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopBannerAutoSlide();
        binding = null;
    }
}