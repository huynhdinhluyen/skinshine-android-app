package com.example.skinshine.ui.staff.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;

import java.util.ArrayList;

public class OrderStatusListFragment extends Fragment {

    private static final String ARG_STATUS = "order_status";
    private StaffOrdersViewModel viewModel;
    private StaffOrdersAdapter adapter;

    public static OrderStatusListFragment newInstance(String status) {
        OrderStatusListFragment fragment = new OrderStatusListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_status_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(StaffOrdersViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StaffOrdersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnOrderClickListener(orderId -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.action_global_staffOrderDetailFragment, bundle);
        });

        if (getArguments() != null) {
            String status = getArguments().getString(ARG_STATUS);
            observeViewModel(status);
        }
    }

    private void observeViewModel(String status) {
        viewModel.getOrdersByStatus(status).observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                adapter.updateOrders(orders);
            }
        });
    }
}
