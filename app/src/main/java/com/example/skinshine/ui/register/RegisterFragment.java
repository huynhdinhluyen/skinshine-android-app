package com.example.skinshine.ui.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.skinshine.R;

public class RegisterFragment extends Fragment {
    private RegisterViewModel viewModel;
    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        initViews(view);
        setupClickListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        // SỬ DỤNG ĐÚNG ID TỪ activity_register.xml
        etFullName = view.findViewById(R.id.edtFullName);
        etEmail = view.findViewById(R.id.edtUsername); // Không phải etEmail
        etPhone = view.findViewById(R.id.edtPhone);
        etPassword = view.findViewById(R.id.edtPassword);
        etConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegisterConfirm); // Không phải btnRegister
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                    password.isEmpty() || confirm.isEmpty()) {
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

            viewModel.register(email, password, fullName, phone);
        });

        tvGoToLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment));
    }

    private void observeViewModel() {
        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                btnRegister.setClickable(false);
                btnRegister.setText("Đang đăng ký...");
            } else {
                btnRegister.setClickable(true);
                btnRegister.setText("ĐĂNG KÝ");

                if (result.isSuccess()) {
                    Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_registerFragment_to_loginFragment);
                } else if (result.isError()) {
                    Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^[0-9]{10,11}$");
    }
}