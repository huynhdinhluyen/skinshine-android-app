package com.example.skinshine.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.SupportItem;
import com.example.skinshine.data.model.User;
import com.example.skinshine.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private TextView tvUserName, tvUserPoint, tvUserRole;
    private LinearLayout profileSection, loginSection;
    private Button btnLogin, btnLogout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SupportAdapter supportAdapter;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout);
        recyclerView = view.findViewById(R.id.recyclerViewSupport);
        profileSection = view.findViewById(R.id.profileSection);
        loginSection = view.findViewById(R.id.loginSection);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserPoint = view.findViewById(R.id.tvUserPoint);
        tvUserRole = view.findViewById(R.id.tvUserRole);
    }

    private void setupViewModel() {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void setupRecyclerView() {
        List<SupportItem> supportItems = createSupportItems();
        supportAdapter = new SupportAdapter(supportItems, this::onSupportItemClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(supportAdapter);
    }

    private List<SupportItem> createSupportItems() {
        List<SupportItem> items = new ArrayList<>();
        items.add(new SupportItem(R.drawable.ic_edit_profile, "Chỉnh sửa hồ sơ"));
        items.add(new SupportItem(R.drawable.ic_history, "Lịch sử đơn hàng"));
        items.add(new SupportItem(R.drawable.ic_blog, "SkinShine Blog"));
        items.add(new SupportItem(R.drawable.ic_faq, "FAQ"));
        return items;
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> navigateToLogin());
        btnLogout.setOnClickListener(v -> {
            profileViewModel.signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        // Observe authentication state
        profileViewModel.getIsUserAuthenticated().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (isAuthenticated) {
                showProfileSection();
            } else {
                showLoginSection();
            }
        });

        // Observe current user data
        profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::handleUserResult);

        // Observe loading state
        profileViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUserResult(Result<User> result) {
        if (result != null) {
            if (result.isSuccess() && result.getData() != null) {
                updateUserUI(result.getData());
            } else if (result.isError()) {
                showDefaultUserInfo();
            }
        }
    }

    private void updateUserUI(User user) {
        tvUserName.setText(user.getDisplayName());

        if (user.isAdmin()) {
            tvUserRole.setVisibility(View.VISIBLE);
            tvUserRole.setText("Admin");
            tvUserPoint.setVisibility(View.GONE);

            // Show admin welcome message only once
            if (getContext() != null) {
                Toast.makeText(getContext(), "Xin chào Admin!", Toast.LENGTH_SHORT).show();
            }
        } else {
            tvUserRole.setVisibility(View.GONE);

            if (user.getPoint() != null) {
                tvUserPoint.setVisibility(View.VISIBLE);
                tvUserPoint.setText("Điểm của bạn: " + user.getPoint());
            } else {
                tvUserPoint.setVisibility(View.GONE);
            }
        }
    }

    private void showDefaultUserInfo() {
        tvUserName.setText("Người dùng");
        tvUserRole.setVisibility(View.GONE);
        tvUserPoint.setVisibility(View.GONE);
    }

    private void showProfileSection() {
        profileSection.setVisibility(View.VISIBLE);
        loginSection.setVisibility(View.GONE);
        btnLogout.setVisibility(View.VISIBLE);
    }

    private void showLoginSection() {
        profileSection.setVisibility(View.GONE);
        loginSection.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
    }

    private void onSupportItemClick(SupportItem item) {
        switch (item.getTitle()) {
            case "SkinShine Blog":
                Toast.makeText(getContext(), "Chức năng Blog đang được phát triển", Toast.LENGTH_SHORT).show();
                break;
            case "FAQ":
                Toast.makeText(getContext(), "Chức năng FAQ đang được phát triển", Toast.LENGTH_SHORT).show();
                break;
            case "Chỉnh sửa hồ sơ":
                Toast.makeText(getContext(), "Chức năng chỉnh sửa hồ sơ đang được phát triển", Toast.LENGTH_SHORT).show();
                break;
            case "Lịch sử đơn hàng":
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_orderHistoryFragment);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Không thể mở lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error starting LoginActivity", e);
            Toast.makeText(getContext(), "Không thể mở màn hình đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        profileViewModel.refreshUser();
    }
}