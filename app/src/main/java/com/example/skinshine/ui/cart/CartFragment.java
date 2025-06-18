package com.example.skinshine.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.CartItem;
import com.example.skinshine.ui.login.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private LinearLayout emptyCartLayout, fullCartLayout;
    private RecyclerView recyclerView;
    private TextView textTotalPrice;
    private Button btnDeleteSelected;
    private TextView textSelectAll;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;

    private boolean isAllSelected = false;

    public CartFragment() {
        super(R.layout.fragment_cart);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_cartFragment_to_loginFragment);
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        emptyCartLayout = view.findViewById(R.id.emptyCartLayout);
        fullCartLayout = view.findViewById(R.id.fullCartLayout);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
        textSelectAll = view.findViewById(R.id.textSelectAll);
        ImageView iconBack = view.findViewById(R.id.iconBack);
        iconBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnDeleteSelected.setOnClickListener(v -> confirmDeleteSelectedItems(userId));

        textSelectAll.setOnClickListener(v -> {
            isAllSelected = !isAllSelected;
            for (CartItem item : cartItems) {
                item.setSelected(isAllSelected);
            }
            textSelectAll.setText(isAllSelected ? "Bỏ chọn tất cả" : "Chọn tất cả");
            if (adapter != null) adapter.notifyDataSetChanged();
        });

        fetchCartItems(userId);
    }

    private void fetchCartItems(String userId) {
        db.collection("users")
                .document(userId)
                .collection("cartItems")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CartItem item = doc.toObject(CartItem.class);
                        cartItems.add(item);
                    }

                    if (cartItems.isEmpty()) {
                        emptyCartLayout.setVisibility(View.VISIBLE);
                        fullCartLayout.setVisibility(View.GONE);
                        btnDeleteSelected.setVisibility(View.GONE);
                        textSelectAll.setVisibility(View.GONE);
                    } else {
                        emptyCartLayout.setVisibility(View.GONE);
                        fullCartLayout.setVisibility(View.VISIBLE);
                        btnDeleteSelected.setVisibility(View.VISIBLE);
                        textSelectAll.setVisibility(View.VISIBLE);

                        adapter = new CartAdapter(cartItems, new CartAdapter.OnCartActionListener() {
                            @Override
                            public void onQuantityChanged(CartItem item, int newQuantity) {
                                db.collection("users")
                                        .document(userId)
                                        .collection("cartItems")
                                        .document(item.getProductId())
                                        .update("quantity", newQuantity);
                                updateTotalPrice();
                            }

                            @Override
                            public void onDeleteItem(CartItem item) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Xác nhận xoá")
                                        .setMessage("Bạn có chắc muốn xoá sản phẩm này?")
                                        .setPositiveButton("XÓA", (dialog, which) -> {
                                            db.collection("users")
                                                    .document(userId)
                                                    .collection("cartItems")
                                                    .document(item.getProductId())
                                                    .delete()
                                                    .addOnSuccessListener(v -> fetchCartItems(userId));
                                        })
                                        .setNegativeButton("Hủy", null)
                                        .show();
                            }

                            @Override
                            public void onItemSelected(CartItem item, boolean isSelected) {
                                item.setSelected(isSelected);
                            }
                        });

                        recyclerView.setAdapter(adapter);
                        updateTotalPrice();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    emptyCartLayout.setVisibility(View.VISIBLE);
                    fullCartLayout.setVisibility(View.GONE);
                });
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        String formattedTotal = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(total);
        textTotalPrice.setText(formattedTotal);
    }

    private void confirmDeleteSelectedItems(String userId) {
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

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá các sản phẩm đã chọn?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    for (CartItem item : selectedItems) {
                        db.collection("users")
                                .document(userId)
                                .collection("cartItems")
                                .document(item.getProductId())
                                .delete();
                    }
                    fetchCartItems(userId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
