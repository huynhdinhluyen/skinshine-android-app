package com.example.skinshine.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewSupport);
        LinearLayout profileSection = view.findViewById(R.id.profileSection);
        LinearLayout loginSection = view.findViewById(R.id.loginSection);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserPoint = view.findViewById(R.id.tvUserPoint);
        TextView tvUserRole = view.findViewById(R.id.tvUserRole);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (user != null) {
            profileSection.setVisibility(View.VISIBLE);
            loginSection.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);

            String displayName = user.getDisplayName();
            tvUserName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Người dùng");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String role = document.getString("role");
                            Long point = document.getLong("point");

                            if ("admin".equals(role)) {
                                tvUserRole.setVisibility(View.VISIBLE);
                                tvUserRole.setText("Vai trò: Admin");

                                tvUserPoint.setVisibility(View.GONE); // Ẩn điểm khi là admin
                                Toast.makeText(getContext(), "Xin chào Admin!", Toast.LENGTH_SHORT).show();
                            } else {
                                tvUserRole.setVisibility(View.GONE); // Ẩn vai trò nếu không phải admin

                                if (point != null) {
                                    tvUserPoint.setVisibility(View.VISIBLE);
                                    tvUserPoint.setText("Điểm của bạn: " + point);
                                } else {
                                    tvUserPoint.setVisibility(View.GONE);
                                }
                            }

                        } else {
                            tvUserRole.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUserRole.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi khi lấy dữ liệu vai trò", Toast.LENGTH_SHORT).show();
                    });

            List<SupportItem> items = new ArrayList<>();
            items.add(new SupportItem(R.drawable.sharp_accessibility_24, "Chỉnh sửa hồ sơ"));
            recyclerView.setAdapter(new SupportAdapter(items));

        } else {
            profileSection.setVisibility(View.GONE);
            loginSection.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);

            recyclerView.setAdapter(null);
        }

        btnLogin.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ProfileFragment", "Error starting LoginActivity", e);
                Toast.makeText(getContext(), "Không thể mở màn hình đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_self);
        });
    }
}
