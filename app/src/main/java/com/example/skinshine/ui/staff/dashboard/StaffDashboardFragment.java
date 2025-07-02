package com.example.skinshine.ui.staff.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.ui.staff.orders.StaffOrdersAdapter;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StaffDashboardFragment extends Fragment {

    private StaffDashboardViewModel viewModel;
    private TextView tvProcessingCount;
    private TextView tvTodayRevenue;
    private StaffOrdersAdapter processingOrdersAdapter;
    private RecyclerView recyclerProcessingOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffDashboardViewModel.class);

        tvProcessingCount = view.findViewById(R.id.tvProcessingCount);
        tvTodayRevenue = view.findViewById(R.id.tvTodayRevenue);
        MaterialCardView cardProcessingOrders = view.findViewById(R.id.cardProcessingOrders);

        cardProcessingOrders.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_staff_orders);
        });
        recyclerProcessingOrders = view.findViewById(R.id.recyclerProcessingOrders);
        setupRecyclerView();

        observeViewModel();
    }

    private void setupRecyclerView() {
        processingOrdersAdapter = new StaffOrdersAdapter(new ArrayList<>());
        recyclerProcessingOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProcessingOrders.setAdapter(processingOrdersAdapter);
        // Xử lý click để chuyển sang màn hình chi tiết
        processingOrdersAdapter.setOnOrderClickListener(orderId -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_staffDashboardFragment_to_staffOrderDetailFragment, bundle);
        });
    }

    private void observeViewModel() {
        viewModel.processingOrdersCount.observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                tvProcessingCount.setText(String.valueOf(count));
            }
        });

        viewModel.todayRevenue.observe(getViewLifecycleOwner(), revenue -> {
            if (revenue != null) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvTodayRevenue.setText(formatter.format(revenue));
            }
        });
        viewModel.processingOrders.observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                processingOrdersAdapter.updateOrders(orders);
            }
        });
    }
}
