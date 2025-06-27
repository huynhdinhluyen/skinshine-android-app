package com.example.skinshine.ui.product;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.utils.product.PlaceholderManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ComparisonBottomSheetFragment extends BottomSheetDialogFragment {

    private ProductComparisonViewModel viewModel;
    private ProductSearchAdapter adapter;
    private EditText editTextSearch;
    private ImageView btnClearSearch;
    private RecyclerView recyclerSearchResults;
    private LinearLayout loadingLayout, emptyLayout;
    private String currentSearchQuery = "";
    private PlaceholderManager placeholderManager;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Set window soft input mode
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(true);
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchInput();
        observeViewModel();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (placeholderManager != null && editTextSearch.getText().toString().trim().isEmpty()) {
            placeholderManager.startRotatingPlaceholder();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (placeholderManager != null) {
            placeholderManager.stopRotatingPlaceholder();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (placeholderManager != null) {
            placeholderManager.stopRotatingPlaceholder();
        }
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        recyclerSearchResults = view.findViewById(R.id.recyclerSearchResults);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        emptyLayout = view.findViewById(R.id.emptyLayout);

        placeholderManager = new PlaceholderManager(editTextSearch);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ProductComparisonViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ProductSearchAdapter(this::onProductSelected);
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSearchResults.setAdapter(adapter);
    }

    private void setupSearchInput() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                currentSearchQuery = query;

                // Show/hide clear button
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (query.isEmpty()) {
                    // Clear results và bắt đầu rotating placeholder
                    adapter.updateProducts(null);
                    showEmptyState(false);
                    placeholderManager.startRotatingPlaceholder();
                } else {
                    // Dừng rotating placeholder khi người dùng nhập
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
            placeholderManager.startRotatingPlaceholder();
        });

        // Focus và bắt đầu rotating placeholder
        editTextSearch.requestFocus();
        placeholderManager.startRotatingPlaceholder();
    }

    private void performSearch(String query) {
        Log.d("ComparisonBottomSheet", "Performing search for: '" + query + "'");
        showLoading(true);
        viewModel.searchProducts(query);
    }

    private void observeViewModel() {
        viewModel.getSearchResults().observe(this, products -> {
            showLoading(false);

            if (products != null && !products.isEmpty()) {
                adapter.updateProducts(products);
                showRecyclerView();
            } else if (!currentSearchQuery.isEmpty()) {
                showEmptyState(true);
            } else {
                showRecyclerView();
            }
        });

        viewModel.getIsSearching().observe(this, isSearching -> {
            showLoading(isSearching != null && isSearching);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showLoading(false);
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }

    private void showRecyclerView() {
        recyclerSearchResults.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void onProductSelected(Product product) {
        viewModel.setCompareProduct(product);
        dismiss();

        // Navigate to comparison view
        showFullComparison();
    }

    private void showFullComparison() {
        try {
            androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.productComparisonFragment);
        } catch (Exception e) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, new ProductComparisonFragment())
                    .addToBackStack("comparison")
                    .commit();
        }
    }
}