package com.example.skinshine.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.data.repository.ProductRepository;
import com.example.skinshine.data.repository.impl.ProductRepositoryImpl;
import com.example.skinshine.ui.cart.CartViewModel;
import com.example.skinshine.utils.cart.CartBadgeHelper;
import com.example.skinshine.utils.product.ComparisonManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private ImageView imageProduct;
    private TextView textName, textBrand, textPrice, textDescription, textIngredients, textCategory, textSkinTypes;
    private RatingBar ratingBar;
    private Product currentProduct;
    private CartViewModel cartViewModel;
    private ProductComparisonViewModel comparisonViewModel;
    private boolean isComparisonMode = false;
    private ProductRepository productRepository;
    private ComparisonManager comparisonManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.activity_product_detail, container, false);
        productRepository = new ProductRepositoryImpl();

        imageProduct = view.findViewById(R.id.imageProduct);
        textName = view.findViewById(R.id.textName);
        textBrand = view.findViewById(R.id.textBrand);
        textPrice = view.findViewById(R.id.textPrice);
        textDescription = view.findViewById(R.id.textDescription);
        textIngredients = view.findViewById(R.id.textIngredients);
        textCategory = view.findViewById(R.id.textCategory);
        textSkinTypes = view.findViewById(R.id.textSkinTypes);
        ratingBar = view.findViewById(R.id.ratingBar);

        ImageView iconBack = view.findViewById(R.id.iconBack);
        ImageView iconCart = view.findViewById(R.id.iconCart);

        iconCart.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.cartFragment);

        });
        iconBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                showAddToCartBottomSheet(currentProduct);
            } else {
                Toast.makeText(getContext(), "Dữ liệu chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            }
        });

        String productId = getArguments() != null ? getArguments().getString("productId") : null;
        if (productId != null) {
            fetchProduct(productId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
        }
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        comparisonViewModel = new ViewModelProvider(this).get(ProductComparisonViewModel.class);
        CardView compareCard = view.findViewById(R.id.compareCard);
        if (compareCard != null) {
            compareCard.setOnClickListener(v -> showProductComparison());
        }

        return view;
    }

    private void setupComparisonManager() {
        comparisonManager = ComparisonManager.getInstance();
    }

    private void showProductComparison() {
        if (currentProduct == null) {
            Toast.makeText(getContext(), "Vui lòng đợi tải sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set current product cho comparison
        comparisonManager.setCurrentProduct(currentProduct);

        // Show bottom sheet để chọn sản phẩm so sánh
        showComparisonBottomSheet();
    }

    private void showComparisonBottomSheet() {
        ComparisonBottomSheetFragment bottomSheet = new ComparisonBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), "comparison_bottom_sheet");
    }

    private void fetchProduct(String productId) {
        productRepository.getProductById(productId).observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess() && result.getData() != null) {
                    currentProduct = result.getData();
                    updateUI(currentProduct);

                    if (comparisonViewModel != null) {
                        comparisonManager.setCurrentProduct(currentProduct);
                    }
                } else if (result.isError()) {
                    Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    result.isLoading();
                }
            }
        });
    }

    private void updateUI(Product product) {
        this.currentProduct = product;

        Glide.with(requireContext())
                .load(product.getImageUrl())
                .placeholder(R.color.placeholder_bg)
                .error(R.drawable.ic_error_placeholder)
                .into(imageProduct);

        textName.setText(product.getName());
        textPrice.setText("💵 Giá: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));
        textDescription.setText("📃 Mô tả: " + product.getDescription());
        textIngredients.setText("🧪 Thành phần: " + product.getIngredients());

        if (product.getBrand() != null) {
            product.getBrand().get().addOnSuccessListener(doc -> {
                textBrand.setText("🏷️ Thương hiệu: " + (doc.exists() ? doc.getString("name") : "Không rõ"));
            }).addOnFailureListener(e -> textBrand.setText("🏷️ Thương hiệu: Không rõ"));
        }

        if (product.getCategory() != null) {
            product.getCategory().get().addOnSuccessListener(doc -> {
                textCategory.setText("🧴 Loại sản phẩm: " + (doc.exists() ? doc.getString("name") : "Không rõ"));
            }).addOnFailureListener(e -> textCategory.setText("🧴 Loại sản phẩm: Không rõ"));
        }

        if (product.getRating() > 0) {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(product.getRating());
        } else {
            ratingBar.setVisibility(View.GONE);
        }

        List<String> skinTypes = product.getSuitableSkinTypes();
        if (skinTypes != null && !skinTypes.isEmpty()) {
            List<String> cleaned = new ArrayList<>();
            for (String s : skinTypes) {
                cleaned.add(s.replace("\"", "").trim());
            }
            textSkinTypes.setText("✅ Phù hợp: " + String.join(", ", cleaned));
        } else {
            textSkinTypes.setText("Phù hợp: Không rõ");
        }
    }

    private void showAddToCartBottomSheet(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_bottom_cart, null);
        dialog.setContentView(view);

        ImageView img = view.findViewById(R.id.imageProduct);
        TextView txtName = view.findViewById(R.id.textProductName);
        TextView txtPrice = view.findViewById(R.id.textDiscountedPrice);
        TextView txtOriginal = view.findViewById(R.id.textOriginalPrice);
        TextView txtTotal = view.findViewById(R.id.textTotalPrice);
        TextView txtQuantity = view.findViewById(R.id.textQuantity);
        Button btnMinus = view.findViewById(R.id.btnMinus);
        Button btnPlus = view.findViewById(R.id.btnPlus);
        Button btnConfirm = view.findViewById(R.id.btnConfirmAddToCart);

        Glide.with(this).load(product.getImageUrl()).into(img);
        txtName.setText(product.getName());
        txtPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));
        txtOriginal.setVisibility(View.GONE);

        final int[] quantity = {1};
        txtQuantity.setText("1");
        txtTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));

        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            txtQuantity.setText(String.valueOf(quantity[0]));
            txtTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice() * quantity[0]));
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
                txtTotal.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice() * quantity[0]));
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            cartViewModel.addToCart(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    quantity[0]
            );

            Toast.makeText(getContext(), "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComparisonManager();
        View cartBadgeContainer = view.findViewById(R.id.cartBadgeContainer);
        if (cartBadgeContainer != null) {
            TextView cartBadge = cartBadgeContainer.findViewById(R.id.cartBadge);
            cartViewModel.getCartItemCount().observe(getViewLifecycleOwner(), count -> {
                if (count != null && count > 0) {
                    cartBadge.setVisibility(View.VISIBLE);
                    cartBadge.setText(String.valueOf(count));
                } else {
                    cartBadge.setVisibility(View.GONE);
                }
            });
            ImageView iconCart = cartBadgeContainer.findViewById(R.id.iconCart);
            if (iconCart != null) {
                iconCart.setOnClickListener(v -> {
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
                        try {
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                            navController.navigate(R.id.action_productDetailFragment_to_loginFragment);
                        } catch (Exception e) {
                            try {
                                Intent intent = new Intent(getActivity(), com.example.skinshine.ui.login.LoginActivity.class);
                                startActivity(intent);
                            } catch (Exception ex) {
                                Log.e("ProductDetail", "Fallback failed: " + ex.getMessage(), ex);
                            }
                        }
                    } else {
                        try {
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                            navController.navigate(R.id.cartFragment);
                        } catch (Exception e) {
                            Log.e("ProductDetail", "Error navigating to cart: " + e.getMessage(), e);
                        }
                    }
                });
            }
        }

        EditText searchInput = view.findViewById(R.id.searchInput);
        if (searchInput != null) {
            searchInput.setOnEditorActionListener((v, actionId, event) -> {
                return false;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        View cartBadgeContainer = getView() != null ? getView().findViewById(R.id.cartBadgeContainer) : null;
        if (cartBadgeContainer != null) {
            CartBadgeHelper.updateCartBadge(cartBadgeContainer);
        }
    }
}
