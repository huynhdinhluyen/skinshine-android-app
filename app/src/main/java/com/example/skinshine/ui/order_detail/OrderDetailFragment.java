package com.example.skinshine.ui.order_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.skinshine.data.model.Order;
import com.example.skinshine.ui.staff.order_detail.StaffOrderDetailAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {
    private OrderDetailViewModel viewModel;
    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvTotalAmount;
    private RecyclerView recyclerOrderItems;
    private StaffOrderDetailAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        initViews(view);
        setupToolbar();
        setupRecyclerView();

        Bundle args = getArguments();
        if (args != null) {
            String orderId = args.getString("orderId");
            if (orderId != null) {
                viewModel.loadOrder(orderId);
                observeViewModel();
            } else {
                Toast.makeText(getContext(), "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
        }
    }

    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        recyclerOrderItems = view.findViewById(R.id.recyclerOrderItems);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = requireView().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void observeViewModel() {
        viewModel.order.observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                updateOrderUI(result.getData());
            } else if (result.isError()) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderUI(Order order) {
        tvOrderId.setText("Mã đơn hàng: #" + order.getId());
        tvOrderStatus.setText("Trạng thái: " + getStatusText(order.getStatus()));

        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText("Ngày đặt: " + sdf.format(order.getCreatedAt()));
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText("Tổng cộng: " + formatter.format(order.getTotalAmount()));

        adapter = new StaffOrderDetailAdapter(order.getItems());
        recyclerOrderItems.setAdapter(adapter);
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";

        switch (status.toUpperCase()) {
            case "PENDING_PAYMENT":
                return "Chờ thanh toán";
            case "PROCESSING":
                return "Đang xử lý";
            case "DELIVERING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }
}
