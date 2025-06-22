package com.example.skinshine.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skinshine.R;
import com.example.skinshine.ui.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        EditText edtFullName = findViewById(R.id.edtFullName);
        EditText edtUsername = findViewById(R.id.edtUsername);
        EditText edtPhone = findViewById(R.id.edtPhone);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);
        TextInputEditText edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        Button btnRegisterConfirm = findViewById(R.id.btnRegisterConfirm);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegisterConfirm.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtUsername.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
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
                                                userData.put("role", "customer");

                                                db.collection("users").document(user.getUid())
                                                        .set(userData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "Lỗi khi lưu dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
