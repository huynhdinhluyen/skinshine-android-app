package com.example.skinshine.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.CartItem;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CheckoutFragment extends Fragment {

    private CheckoutViewModel viewModel;
    private RecyclerView recyclerCheckoutItems;
    private TextView textTotalAmount;
    private Button btnPlaceOrder;
    private MaterialToolbar toolbar;
    private CheckoutAdapter adapter;
    private List<CartItem> selectedItems = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedItems = getArguments().getParcelableArrayList("selectedItems");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        initViews(view);
        setupWindowInsets(view);
        setupToolbar();

        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Không có sản phẩm nào để thanh toán.", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        setupRecyclerView();
        observeViewModel();
        setupClickListeners();
        updateUI();
    }

    private void setupWindowInsets(View view) {
        final View bottomActionContainer = view.findViewById(R.id.bottomActionContainer);
        final int originalBottomPadding = bottomActionContainer.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            bottomActionContainer.setPadding(
                    bottomActionContainer.getPaddingLeft(),
                    bottomActionContainer.getPaddingTop(),
                    bottomActionContainer.getPaddingRight(),
                    originalBottomPadding + navBarInsets
            );
            return insets;
        });
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerCheckoutItems = view.findViewById(R.id.recyclerCheckoutItems);
        textTotalAmount = view.findViewById(R.id.textTotalAmount);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void setupRecyclerView() {
        adapter = new CheckoutAdapter(selectedItems);
        recyclerCheckoutItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCheckoutItems.setAdapter(adapter);
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        calculateAndSetTotalPrice();
    }

    private void calculateAndSetTotalPrice() {
        double total = 0;
        for (CartItem item : selectedItems) {
            total += item.getPrice() * item.getQuantity();
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textTotalAmount.setText(formatter.format(total));
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> {
            if (selectedItems != null && !selectedItems.isEmpty()) {
                viewModel.placeOrderAndRequestPayment(selectedItems, "ZALOPAY");
            }
        });
    }

    private void observeViewModel() {
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        viewModel.zaloPayTokenResult.observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                setLoadingState(true, "Đang tạo đơn hàng...");
            } else {
                setLoadingState(false, "");
                if (result.isSuccess()) {
                    String token = result.getData();
                    PayOrderListener listener = new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                            viewModel.finalizeSuccessfulPayment(selectedItems);
                        }
                        @Override
                        public void onPaymentCanceled(String zpTransToken, String appTransID) {
                            Toast.makeText(getContext(), "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                            Toast.makeText(getContext(), "Lỗi thanh toán: " + zaloPayError.toString(), Toast.LENGTH_LONG).show();
                        }
                    };
                    ZaloPaySDK.getInstance().payOrder(requireActivity(), token, "skinshine://zalopay/callback", listener);
                } else if (result.isError()) {
                    Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Lắng nghe kết quả cuối cùng của toàn bộ quá trình
        viewModel.postPaymentResult.observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                setLoadingState(true, "Đang hoàn tất...");
            } else {
                setLoadingState(false, "");
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), "Đặt hàng và thanh toán thành công!", Toast.LENGTH_LONG).show();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack(R.id.navigation_home, false);
                } else if (result.isError()) {
                    Toast.makeText(getContext(), "Lỗi sau thanh toán: " + result.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setLoadingState(boolean isLoading, String message) {
        btnPlaceOrder.setEnabled(!isLoading);
        btnPlaceOrder.setText(isLoading ? message : "ĐẶT HÀNG");
    }
}