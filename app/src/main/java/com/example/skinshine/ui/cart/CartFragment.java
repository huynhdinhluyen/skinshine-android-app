package com.example.skinshine.ui.cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
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
import com.example.skinshine.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {
    private CartViewModel cartViewModel;
    private CartAdapter adapter;
    private LinearLayout emptyCartLayout, fullCartLayout;
    private RecyclerView recyclerView;
    private TextView textTotalPrice, textSelectAll;
    private Button btnDeleteSelected;
    private boolean isAllSelected = false;
    private List<CartItem> cartItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupWindowInsets(view);

        if (!isUserLoggedIn()) {
            redirectToLogin();
            showEmptyCart();
            return;
        }

        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomInset);
                return insets;
            }
        });
    }

    private boolean isUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void redirectToLogin() {
        Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
        try {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("CartFragment", "Error starting LoginActivity", e);
        }
    }

    private void initViews(View view) {
        emptyCartLayout = view.findViewById(R.id.emptyCartLayout);
        fullCartLayout = view.findViewById(R.id.fullCartLayout);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
        textSelectAll = view.findViewById(R.id.textSelectAll);
    }

    private void setupViewModel() {
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(cartItems, new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                cartViewModel.updateQuantity(item.getProductId(), newQuantity);
            }

            @Override
            public void onDeleteItem(CartItem item) {
                showDeleteConfirmation(item);
            }

            @Override
            public void onItemSelected(CartItem item, boolean isSelected) {
                item.setSelected(isSelected);
                updateTotalPrice();
                updateSelectAllButton();
                updateDeleteButtonVisibility();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        ImageView iconBack = getView().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        btnDeleteSelected.setOnClickListener(v -> deleteSelectedItems());

        textSelectAll.setOnClickListener(v -> toggleSelectAll());

        Button btnContinueShopping = getView().findViewById(R.id.btnContinueShopping);
        btnContinueShopping.setOnClickListener(v -> navigateToHome());

        Button btnCheckout = requireView().findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            ArrayList<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }

            // Kiểm tra nếu chưa chọn sản phẩm nào
            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(),
                        "Vui lòng chọn ít nhất một sản phẩm để thanh toán",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo bundle để truyền dữ liệu
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("selectedItems", selectedItems);
            NavController nav = Navigation.findNavController(v);
            nav.navigate(R.id.action_cartFragment_to_checkoutFragment, bundle);
        });
    }

    private void observeViewModel() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), this::updateCartUI);

        cartViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnDeleteSelected.setEnabled(!isLoading);
        });
    }

    private void updateCartUI(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            showEmptyCart();
        } else {
            showCartWithItems(items);
        }
    }

    private void showEmptyCart() {
        emptyCartLayout.setVisibility(View.VISIBLE);
        fullCartLayout.setVisibility(View.GONE);
    }

    private void showCartWithItems(List<CartItem> items) {
        emptyCartLayout.setVisibility(View.GONE);
        fullCartLayout.setVisibility(View.VISIBLE);

        cartItems.clear();
        cartItems.addAll(items);
        adapter.notifyDataSetChanged();

        updateTotalPrice();
        updateSelectAllButton();
        updateDeleteButtonVisibility();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textTotalPrice.setText(formatter.format(total));
    }

    private void updateSelectAllButton() {
        long selectedCount = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedCount = cartItems.stream().mapToLong(item -> item.isSelected() ? 1 : 0).sum();
        }
        isAllSelected = selectedCount == cartItems.size() && !cartItems.isEmpty();
        textSelectAll.setText(isAllSelected ? "Bỏ chọn tất cả" : "Chọn tất cả");
    }

    private void updateDeleteButtonVisibility() {
        boolean hasSelectedItems = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hasSelectedItems = cartItems.stream().anyMatch(CartItem::isSelected);
        }
        btnDeleteSelected.setVisibility(hasSelectedItems ? View.VISIBLE : View.GONE);
    }

    private void toggleSelectAll() {
        isAllSelected = !isAllSelected;
        for (CartItem item : cartItems) {
            item.setSelected(isAllSelected);
        }
        adapter.notifyDataSetChanged();
        updateTotalPrice();
        updateSelectAllButton();
        updateDeleteButtonVisibility();
    }

    private void deleteSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa chọn sản phẩm nào", Toast.LENGTH_SHORT).show();
            return;
        }

        showDeleteSelectedConfirmation(selectedItems);
    }

    private void showDeleteConfirmation(CartItem item) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá sản phẩm này?")
                .setPositiveButton("XÓA", (dialog, which) -> cartViewModel.removeItem(item.getProductId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteSelectedConfirmation(List<CartItem> selectedItems) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá các sản phẩm đã chọn?")
                .setPositiveButton("XÓA", (dialog, which) -> cartViewModel.removeSelectedItems(selectedItems))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateToHome() {
        try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
        } catch (Exception e) {
            Log.e("CartFragment", "Error navigating to home", e);
        }
    }
}