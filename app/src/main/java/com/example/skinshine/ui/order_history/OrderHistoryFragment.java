package com.example.skinshine.ui.order_history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class OrderHistoryFragment extends Fragment {
    private OrderHistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        recyclerView = view.findViewById(R.id.recyclerOrderHistory);
        setupWindowInsets();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {
            int bottomPadding = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomPadding);
            return insets;
        });
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getOrderHistory().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                adapter.updateOrders(result.getData());
            } else if (result.isError()) {
                // Hiển thị lỗi
            }
        });
    }
}