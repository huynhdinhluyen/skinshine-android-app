package com.example.skinshine.ui.product;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.utils.product.ComparisonManager;
import com.example.skinshine.utils.product.PlaceholderManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ComparisonBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "ComparisonBottomSheet";
    public static final String REQUEST_KEY = "comparison_request";
    public static final String KEY_PRODUCT_ID = "selected_product_id";

    private ProductComparisonViewModel viewModel;
    private ProductSearchAdapter adapter;
    private PlaceholderManager placeholderManager;
    private ComparisonManager comparisonManager;

    // Views
    private EditText editTextSearch;
    private ImageView btnClearSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout loadingLayout;
    private LinearLayout emptyLayout;
    private FrameLayout searchContainer;

    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        setupWindowInsets(view);
        initViews(view);
        setupViewModel();
        setupComparisonManager();
        setupRecyclerView();
        setupSearchInput();
        observeViewModel();
    }

    private void setupWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Adjust for keyboard
            v.setPadding(0, 0, 0, Math.max(systemBars.bottom, ime.bottom));

            return insets;
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dlg -> {
            FrameLayout sheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (sheet != null) {
                BottomSheetBehavior<FrameLayout> behavior =
                        BottomSheetBehavior.from(sheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setFitToContents(true);
                behavior.setSkipCollapsed(true);
            }
        });
        // ensure keyboard resize
        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    );
        }
        return dialog;
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        recyclerView = view.findViewById(R.id.recyclerViewSearch);
        progressBar = view.findViewById(R.id.progressBar);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        searchContainer = view.findViewById(R.id.searchContainer);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ProductComparisonViewModel.class);
    }

    private void setupComparisonManager() {
        comparisonManager = ComparisonManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new ProductSearchAdapter(this::onProductSelected);
        adapter.updateProducts(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchInput() {
        placeholderManager = new PlaceholderManager(editTextSearch);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                btnClearSearch.setVisibility(q.isEmpty() ? GONE : VISIBLE);
                placeholderManager.stopRotatingPlaceholder();
                viewModel.searchProducts(q);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                currentSearchQuery = query;

                btnClearSearch.setVisibility(query.isEmpty() ? GONE : VISIBLE);

                if (query.isEmpty()) {
                    adapter.updateProducts(null);
                    showEmptyState(false);
                    placeholderManager.startRotatingPlaceholder();
                } else {
                    placeholderManager.stopRotatingPlaceholder();
                    if (query.length() >= 1) {
                        performSearch(query);
                    } else {
                        showEmptyState(false);
                    }
                }
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            editTextSearch.setText("");
            editTextSearch.requestFocus();
        });
        placeholderManager.startRotatingPlaceholder();
    }

    private void observeViewModel() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                showEmptyState(false);
                List<Product> filteredProducts = new ArrayList<>();
                Product currentProduct = comparisonManager.getCurrentProduct();

                for (Product product : products) {
                    if (currentProduct == null ||
                            !product.getId().equals(currentProduct.getId())) {
                        filteredProducts.add(product);
                    }
                }

                adapter.updateProducts(filteredProducts);
                showResults(filteredProducts.isEmpty());
            } else {
                showEmptyState(true);
            }
        });

        viewModel.getIsSearching().observe(getViewLifecycleOwner(), isSearching -> {
            if (isSearching) {
                showLoading();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });

        viewModel.getProductNames().observe(getViewLifecycleOwner(), names -> {
            if (placeholderManager != null) {
                placeholderManager.setProductNames(names);
            }
        });
    }

    private void performSearch(String query) {
        viewModel.searchProducts(query);
    }

    private void onProductSelected(Product product) {
        Product currentProduct = comparisonManager.getCurrentProduct();
        if (currentProduct != null && product.getId().equals(currentProduct.getId())) {
            Toast.makeText(getContext(), "Không thể so sánh sản phẩm với chính nó", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle result = new Bundle();
        result.putString(KEY_PRODUCT_ID, product.getId());
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        dismiss();
//        comparisonManager.setCompareProduct(product);
//        dismiss();
//
//        try {
//            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
//            navController.navigate(R.id.productComparisonFragment);
//        } catch (Exception e) {
//            Log.e(TAG, "Error navigating to comparison", e);
//            Toast.makeText(getContext(), "Không thể mở màn hình so sánh", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (comparisonManager.isComparing() && getActivity() != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    NavController navController = Navigation.findNavController(
                            requireActivity(), R.id.nav_host_fragment_activity_main);
                    navController.navigate(R.id.productComparisonFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to comparison after dismiss", e);
                    Toast.makeText(getContext(), "Không thể mở màn hình so sánh", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (placeholderManager != null) {
            placeholderManager.stopRotatingPlaceholder();
        }
    }

    private void showLoading() {
        recyclerView.setVisibility(GONE);
        emptyLayout.setVisibility(GONE);
        loadingLayout.setVisibility(VISIBLE);
    }

    private void showResults(boolean isEmpty) {
        loadingLayout.setVisibility(GONE);

        if (isEmpty) {
            showEmptyState(true);
        } else {
            recyclerView.setVisibility(VISIBLE);
            emptyLayout.setVisibility(GONE);
        }
    }

    private void showEmptyState(boolean isSearchResult) {
        recyclerView.setVisibility(GONE);
        loadingLayout.setVisibility(GONE);
        emptyLayout.setVisibility(isSearchResult ? VISIBLE : GONE);
    }
}