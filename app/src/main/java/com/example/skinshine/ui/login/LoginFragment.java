package com.example.skinshine.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.skinshine.MainActivity;
import com.example.skinshine.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private LoginViewModel viewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private Button btnRegister; // Đổi từ TextView thành Button
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        mAuth = FirebaseAuth.getInstance();

        initViews(view);
        setupClickListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        // SỬ DỤNG ĐÚNG ID TỪ activity_login.xml
        etEmail = view.findViewById(R.id.edtUsername); // Không phải etEmail
        etPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLoginConfirm); // Không phải btnLogin
        btnRegister = view.findViewById(R.id.btnRegister); // Button, không phải TextView
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(email, password);
        });

        // SỬA LẠI BUTTON REGISTER
        btnRegister.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment));
    }

    private void observeViewModel() {
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                btnLogin.setClickable(false);
                btnLogin.setText("Đang đăng nhập...");
            } else {
                btnLogin.setClickable(true);
                btnLogin.setText("ĐĂNG NHẬP");

                if (result.isSuccess()) {
                    String userRole = result.getData();
                    navigateToMain(userRole);
                } else if (result.isError()) {
                    Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToMain(String userRole) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("USER_ROLE", userRole);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}