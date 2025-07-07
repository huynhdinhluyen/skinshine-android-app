package com.example.skinshine.ui.staff.customers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.ui.staff.orders.StaffOrdersAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class CustomerOrdersFragment extends Fragment {

    private CustomerOrdersViewModel viewModel;
    private RecyclerView recyclerView;
    private StaffOrdersAdapter adapter;
    private TextView emptyView;
    private ProgressBar progressBar;
    private String customerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CustomerOrdersViewModel.class);

        if (getArguments() != null) {
            customerId = getArguments().getString("customerId");
        }

        initViews(view);
        setupRecyclerView();
        observeViewModel();

        // Set customer ID để trigger load data
        if (customerId != null) {
            viewModel.setCustomerId(customerId);
        }
    }

    private void initViews(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        recyclerView = view.findViewById(R.id.recyclerOrders);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StaffOrdersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnOrderClickListener(orderId -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_global_staffOrderDetailFragment, bundle);
        });
    }

    private void observeViewModel() {
        viewModel.getCustomerOrders().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                showLoading(true);
            } else if (result.isSuccess()) {
                showLoading(false);
                if (result.getData() != null && !result.getData().isEmpty()) {
                    showOrdersList(result.getData());
                } else {
                    showEmptyState();
                }
            } else if (result.isError()) {
                showLoading(false);
                showEmptyState();
                Toast.makeText(getContext(),
                        "Lỗi: " + result.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showOrdersList(java.util.List<com.example.skinshine.data.model.Order> orders) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        adapter.updateOrders(orders);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
}