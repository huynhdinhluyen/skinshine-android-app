package com.example.skinshine.ui.staff.inventory;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.ui.category.CategoryAdapter;

import java.util.ArrayList;

public class StaffInventoryFragment extends Fragment {

    private StaffInventoryViewModel viewModel;
    private StaffInventoryAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StaffInventoryViewModel.class);

        setupRecyclerViews(view);
        setupSearchView(view);
        observeViewModel();
    }

    private void setupRecyclerViews(View view) {
        // Category RecyclerView
        RecyclerView categoryRecyclerView = view.findViewById(R.id.recyclerCategories);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(java.util.Collections.emptyList(), category -> {
            viewModel.filterByCategory(category.getId());
        });
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Products RecyclerView
        RecyclerView productsRecyclerView = view.findViewById(R.id.recyclerProducts);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new StaffInventoryAdapter();
        productsRecyclerView.setAdapter(adapter);
    }

    private void setupSearchView(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                adapter.updateProducts(result.getData());
            } else if (result.isError()) {
                Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                categoryAdapter.updateCategories(result.getData());
            }
        });
    }
}