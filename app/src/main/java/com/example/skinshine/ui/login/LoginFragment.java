package com.example.skinshine.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.skinshine.MainActivity;
import com.example.skinshine.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;

    public LoginFragment() {
        super(R.layout.activity_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        TextInputEditText edtUsername = view.findViewById(R.id.edtUsername);
        TextInputEditText edtPassword = view.findViewById(R.id.edtPassword);
        Button btnLoginConfirm = view.findViewById(R.id.btnLoginConfirm);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView tvBackHome = view.findViewById(R.id.tvBackHome);

        tvBackHome.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_loginFragment_to_homeFragment);
        });

        btnLoginConfirm.setOnClickListener(v -> {
            String email = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            // THAY ĐỔI: Thay vì điều hướng, hãy kiểm tra vai trò và khởi động lại MainActivity
                            checkUserRoleAndNavigate(task.getResult().getUser());
                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "Đăng nhập thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        btnRegister.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }

    private void checkUserRoleAndNavigate(FirebaseUser user) {
        if (user == null || getActivity() == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = "customer"; // Mặc định là customer
                    if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                        role = documentSnapshot.getString("role");
                    }
                    // Khởi động lại MainActivity với vai trò chính xác
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("USER_ROLE", role);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish(); // Đóng Activity hiện tại để người dùng không thể quay lại
                })
                .addOnFailureListener(e -> {
                    // Nếu có lỗi, vẫn cho vào vai trò customer để đảm bảo app không bị treo
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("USER_ROLE", "customer");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                });
    }
}
