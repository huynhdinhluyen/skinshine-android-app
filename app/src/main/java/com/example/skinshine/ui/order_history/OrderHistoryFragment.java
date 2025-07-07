package com.example.skinshine.ui.order_history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class OrderHistoryFragment extends Fragment {
    private static final String TAG = "OrderHistoryFragment";

    private OrderHistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private LinearLayout emptyStateLayout, loadingLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupToolbar();
        observeViewModel();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerOrderHistory);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        loadingLayout = view.findViewById(R.id.loadingLayout);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = requireView().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getOrderHistory().observe(getViewLifecycleOwner(), result -> {
            Log.d(TAG, "Order history result: " + (result != null ? result.toString() : "null"));

            if (result == null) {
                showEmptyState();
                return;
            }

            if (result.isLoading()) {
                showLoading();
            } else if (result.isSuccess()) {
                hideLoading();
                if (result.getData() != null && !result.getData().isEmpty()) {
                    Log.d(TAG, "Found " + result.getData().size() + " orders");
                    showOrdersList();
                    adapter.updateOrders(result.getData());
                } else {
                    Log.d(TAG, "No orders found");
                    showEmptyState();
                }
            } else if (result.isError()) {
                hideLoading();
                Log.e(TAG, "Error loading orders: " + result.getMessage());
                showEmptyState();
                Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
    }

    private void showOrdersList() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}