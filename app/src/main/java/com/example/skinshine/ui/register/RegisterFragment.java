package com.example.skinshine.ui.register;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.skinshine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {
    private FirebaseAuth mAuth;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    public boolean isValidPhoneNumber(String phone) {
        return phone.matches("^0[3|5|7|8|9][0-9]{8}$");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        EditText edtFullName = view.findViewById(R.id.edtFullName);
        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtPhone = view.findViewById(R.id.edtPhone);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        Button btnRegisterConfirm = view.findViewById(R.id.btnRegisterConfirm);
        TextView tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        btnRegisterConfirm.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtUsername.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(getContext(), "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(getContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.updateProfile(new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build())
                                        .addOnCompleteListener(profileTask -> {
                                            if (profileTask.isSuccessful()) {
                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("name", fullName);
                                                userData.put("email", email);
                                                userData.put("phone", phone);
                                                userData.put("role", "admin");

                                                db.collection("users").document(user.getUid())
                                                        .set(userData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                            Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(getContext(), "Lỗi khi lưu dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Toast.makeText(getContext(), "Lỗi khi cập nhật tên người dùng", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvGoToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }
}
