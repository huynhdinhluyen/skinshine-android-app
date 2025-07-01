package com.example.skinshine.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.skinshine.R;
import com.example.skinshine.data.model.Product;
import com.example.skinshine.utils.product.ComparisonManager;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductComparisonFragment extends Fragment implements ComparisonManager.OnComparisonChangeListener {

    private ComparisonManager comparisonManager;
    private ImageView imageCurrentProduct, imageCompareProduct;
    private TextView textCurrentName, textCompareName;
    private LinearLayout comparisonContainer;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_comparison, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        rootView = view;
        setupWindowInsets(view);
        initViews(view);
        setupComparisonManager();

        // Load comparison nếu đã có data
        if (comparisonManager.isComparing()) {
            updateComparisonUI(comparisonManager.getCurrentProduct(), comparisonManager.getCompareProduct());
        }
    }

    private void setupWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top padding for status bar
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);

            return insets;
        });
    }

    private void initViews(View view) {
        imageCurrentProduct = view.findViewById(R.id.imageCurrentProduct);
        imageCompareProduct = view.findViewById(R.id.imageCompareProduct);
        textCurrentName = view.findViewById(R.id.textCurrentName);
        textCompareName = view.findViewById(R.id.textCompareName);
        comparisonContainer = view.findViewById(R.id.comparisonContainer);

        ImageView btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void setupComparisonManager() {
        comparisonManager = ComparisonManager.getInstance();
        comparisonManager.setOnComparisonChangeListener(this);
    }

    @Override
    public void onComparisonUpdated(Product current, Product compare) {
        if (getView() != null) {
            updateComparisonUI(current, compare);
        }
    }

    @Override
    public void onComparisonCleared() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (comparisonManager != null) {
            comparisonManager.setOnComparisonChangeListener(null);
        }
    }

    private void updateComparisonUI(Product currentProduct, Product compareProduct) {
        if (currentProduct == null || compareProduct == null) {
            return;
        }

        // Load images
        Glide.with(this)
                .load(currentProduct.getImageUrl())
                .placeholder(R.color.placeholder_bg)
                .error(R.drawable.ic_error_placeholder)
                .into(imageCurrentProduct);

        Glide.with(this)
                .load(compareProduct.getImageUrl())
                .placeholder(R.color.placeholder_bg)
                .error(R.drawable.ic_error_placeholder)
                .into(imageCompareProduct);

        // Update names
        textCurrentName.setText(currentProduct.getName());
        textCompareName.setText(compareProduct.getName());

        // Build comparison table
        buildComparisonTable(currentProduct, compareProduct);
    }

    private void buildComparisonTable(Product currentProduct, Product compareProduct) {
        comparisonContainer.removeAllViews();

        // Price comparison
        addComparisonRow("💰 Giá",
                NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(currentProduct.getPrice()),
                NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(compareProduct.getPrice()));

        // Brand comparison
        loadBrandComparison(currentProduct, compareProduct);

        // Category comparison
        loadCategoryComparison(currentProduct, compareProduct);

        // Rating comparison
        addComparisonRow("⭐ Đánh giá",
                currentProduct.getRating() + "/5",
                compareProduct.getRating() + "/5");

        // Description comparison
        addComparisonRow("📝 Mô tả",
                truncateText(currentProduct.getDescription(), 100),
                truncateText(compareProduct.getDescription(), 100));

        // Ingredients comparison
        addComparisonRow("🧪 Thành phần",
                truncateText(currentProduct.getIngredients(), 150),
                truncateText(compareProduct.getIngredients(), 150));

        // Skin types comparison
        String currentSkinTypes = currentProduct.getSuitableSkinTypes() != null ?
                String.join(", ", currentProduct.getSuitableSkinTypes()) : "Không rõ";
        String compareSkinTypes = compareProduct.getSuitableSkinTypes() != null ?
                String.join(", ", compareProduct.getSuitableSkinTypes()) : "Không rõ";

        addComparisonRow("✅ Phù hợp", currentSkinTypes, compareSkinTypes);
    }

    private void loadBrandComparison(Product currentProduct, Product compareProduct) {
        String[] brandNames = new String[2];

        if (currentProduct.getBrand() != null) {
            currentProduct.getBrand().get().addOnSuccessListener(doc -> {
                brandNames[0] = doc.exists() ? doc.getString("name") : "Không rõ";
                checkAndAddBrandRow(brandNames);
            }).addOnFailureListener(e -> {
                brandNames[0] = "Không rõ";
                checkAndAddBrandRow(brandNames);
            });
        } else {
            brandNames[0] = "Không rõ";
        }

        if (compareProduct.getBrand() != null) {
            compareProduct.getBrand().get().addOnSuccessListener(doc -> {
                brandNames[1] = doc.exists() ? doc.getString("name") : "Không rõ";
                checkAndAddBrandRow(brandNames);
            }).addOnFailureListener(e -> {
                brandNames[1] = "Không rõ";
                checkAndAddBrandRow(brandNames);
            });
        } else {
            brandNames[1] = "Không rõ";
        }
    }

    private void checkAndAddBrandRow(String[] brandNames) {
        if (brandNames[0] != null && brandNames[1] != null) {
            addComparisonRow("🏷️ Thương hiệu", brandNames[0], brandNames[1]);
        }
    }

    private void loadCategoryComparison(Product currentProduct, Product compareProduct) {
        String[] categoryNames = new String[2];

        if (currentProduct.getCategory() != null) {
            currentProduct.getCategory().get().addOnSuccessListener(doc -> {
                categoryNames[0] = doc.exists() ? doc.getString("name") : "Không rõ";
                checkAndAddCategoryRow(categoryNames);
            }).addOnFailureListener(e -> {
                categoryNames[0] = "Không rõ";
                checkAndAddCategoryRow(categoryNames);
            });
        } else {
            categoryNames[0] = "Không rõ";
        }

        if (compareProduct.getCategory() != null) {
            compareProduct.getCategory().get().addOnSuccessListener(doc -> {
                categoryNames[1] = doc.exists() ? doc.getString("name") : "Không rõ";
                checkAndAddCategoryRow(categoryNames);
            }).addOnFailureListener(e -> {
                categoryNames[1] = "Không rõ";
                checkAndAddCategoryRow(categoryNames);
            });
        } else {
            categoryNames[1] = "Không rõ";
        }
    }

    private void checkAndAddCategoryRow(String[] categoryNames) {
        if (categoryNames[0] != null && categoryNames[1] != null) {
            addComparisonRow("🧴 Loại sản phẩm", categoryNames[0], categoryNames[1]);
        }
    }

    private void addComparisonRow(String label, String currentValue, String compareValue) {
        View rowView = LayoutInflater
                .from(getContext())
                .inflate(R.layout.item_comparison_row, comparisonContainer, false);

        TextView textLabel = rowView.findViewById(R.id.textLabel);
        TextView textCurrentValue = rowView.findViewById(R.id.textCurrentValue);
        TextView textCompareValue = rowView.findViewById(R.id.textCompareValue);

        textLabel.setText(label);
        textCurrentValue.setText(currentValue != null ? currentValue : "Không rõ");
        textCompareValue.setText(compareValue != null ? compareValue : "Không rõ");

        comparisonContainer.addView(rowView);
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "Không có thông tin";
        }
        return text.substring(0, maxLength) + "...";
    }
}