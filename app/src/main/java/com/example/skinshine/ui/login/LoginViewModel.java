package com.example.skinshine.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.AuthRepository;
import com.example.skinshine.data.repository.impl.AuthRepositoryImpl;

public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Result<String>> loginResult;

    public LoginViewModel() {
        this.authRepository = new AuthRepositoryImpl();
        this.loginResult = new MutableLiveData<>();
    }

    public LiveData<Result<String>> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        loginResult.setValue(Result.loading());

        authRepository.login(email, password).observeForever(result -> {
            loginResult.setValue(result);
        });
    }
}
