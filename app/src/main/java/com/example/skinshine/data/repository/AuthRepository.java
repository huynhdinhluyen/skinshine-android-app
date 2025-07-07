package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Result;

public interface AuthRepository {
    LiveData<Result<String>> login(String email, String password);
    LiveData<Result<String>> register(String email, String password, String fullName, String phone);
    void logout();
    LiveData<Result<String>> getCurrentUserRole();
}
