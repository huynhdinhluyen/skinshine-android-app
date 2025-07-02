package com.example.skinshine.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.AuthRepository;
import com.example.skinshine.data.repository.impl.AuthRepositoryImpl;

public class RegisterViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Result<String>> registerResult;

    public RegisterViewModel() {
        this.authRepository = new AuthRepositoryImpl();
        this.registerResult = new MutableLiveData<>();
    }

    public LiveData<Result<String>> getRegisterResult() {
        return registerResult;
    }

    public void register(String email, String password, String fullName, String phone) {
        registerResult.setValue(Result.loading());

        authRepository.register(email, password, fullName, phone).observeForever(result -> {
            registerResult.setValue(result);
        });
    }
}
