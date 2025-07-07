package com.example.skinshine.ui.staff.order_detail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StaffOrderDetailFragment extends Fragment {

    private StaffOrderDetailViewModel viewModel;
    private TextView tvCustomerName, tvCustomerEmail, tvTotalAmount;
    private RecyclerView recyclerOrderItems;
    private Button btnUpdateStatus;
    private StaffOrderDetailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffOrderDetailViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        if (getArguments() != null) {
            String orderId = getArguments().getString("orderId");
            viewModel.loadOrder(orderId);
        }
    }

    private void initViews(View view) {
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerEmail = view.findViewById(R.id.tvCustomerEmail);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        recyclerOrderItems = view.findViewById(R.id.recyclerOrderItems);
        btnUpdateStatus = view.findViewById(R.id.btnUpdateStatus);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void setupRecyclerView() {
        adapter = new StaffOrderDetailAdapter(new ArrayList<>());
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerOrderItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnUpdateStatus.setOnClickListener(v -> showStatusUpdateDialog());
    }

    private void observeViewModel() {
        viewModel.order.observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                updateOrderUI(result.getData());
            } else if (result.isError()) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.customer.observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                tvCustomerName.setText("Tên: " + result.getData().getName());
                tvCustomerEmail.setText("Email: " + result.getData().getEmail());
            }
        });

        viewModel.getUpdateStatusResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(getContext(), "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
            } else if (result.isError()) {
                Toast.makeText(getContext(), "Lỗi cập nhật: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderUI(Order order) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText("Tổng cộng: " + formatter.format(order.getTotalAmount()));
        adapter = new StaffOrderDetailAdapter(order.getItems());
        recyclerOrderItems.setAdapter(adapter);
        updateButtonByStatus(order.getStatus());
    }

    private void updateButtonByStatus(String status) {
        if (status == null) {
            btnUpdateStatus.setVisibility(View.GONE);
            return;
        }
        switch (status.toUpperCase()) {
            case "PROCESSING":
                btnUpdateStatus.setText("XÁC NHẬN GIAO HÀNG");
                btnUpdateStatus.setVisibility(View.VISIBLE);
                break;
            case "DELIVERING": // Giả sử có trạng thái "Đang giao"
                btnUpdateStatus.setText("XÁC NHẬN ĐÃ GIAO");
                btnUpdateStatus.setVisibility(View.VISIBLE);
                break;
            default: // Các trạng thái DELIVERED, CANCELLED, PENDING_PAYMENT
                btnUpdateStatus.setVisibility(View.GONE);
                break;
        }
    }

    private void showStatusUpdateDialog() {
        String currentStatus = viewModel.order.getValue().getData().getStatus();
        if (currentStatus == null) return;

        String title = "Xác nhận hành động";
        String message = "";
        String positiveButtonText = "XÁC NHẬN";
        String newStatus = "";

        switch (currentStatus.toUpperCase()) {
            case "PROCESSING":
                message = "Bạn có chắc chắn muốn chuyển đơn hàng này sang trạng thái 'Đang giao hàng'?";
                newStatus = "DELIVERING";
                break;
            case "DELIVERING":
                message = "Xác nhận đơn hàng đã được giao thành công đến khách hàng?";
                newStatus = "DELIVERED";
                break;
            default:
                return;
        }

        final String finalNewStatus = newStatus;
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    viewModel.updateOrderStatus(finalNewStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
