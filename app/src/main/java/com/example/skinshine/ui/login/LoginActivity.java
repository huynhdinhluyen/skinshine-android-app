package com.example.skinshine.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skinshine.MainActivity;
import com.example.skinshine.R;
import com.example.skinshine.ui.register.RegisterActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        TextInputEditText edtUsername = findViewById(R.id.edtUsername);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);
        Button btnLoginConfirm = findViewById(R.id.btnLoginConfirm);
        Button btnRegister = findViewById(R.id.btnRegister);
        LinearLayout backContainer = findViewById(R.id.backContainer);

        if (backContainer != null) {
            backContainer.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnLoginConfirm != null && edtUsername != null && edtPassword != null) {
            btnLoginConfirm.setOnClickListener(v -> {
                String email = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnLoginConfirm.setClickable(false);
                btnLoginConfirm.setText("Đang đăng nhập...");

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            btnLoginConfirm.setClickable(true);
                            btnLoginConfirm.setText("ĐĂNG NHẬP");

                            if (task.isSuccessful()) {
                                checkUserRoleAndNavigate(task.getResult().getUser());
                            } else {
                                Toast.makeText(this,
                                        "Đăng nhập thất bại: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void checkUserRoleAndNavigate(FirebaseUser user) {
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = "customer"; // Mặc định là customer
                    if (documentSnapshot.exists()) {
                        role = documentSnapshot.getString("role");
                    }
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("USER_ROLE", role); // Gửi role qua Intent
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi, vẫn cho vào vai trò customer
                    Toast.makeText(this, "Đăng nhập thành công (không thể lấy vai trò)!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("USER_ROLE", "customer");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}