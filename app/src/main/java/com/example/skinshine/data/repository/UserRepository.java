package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.User;

import java.util.List;

public interface UserRepository {
    LiveData<Result<User>> getCurrentUser();

    LiveData<Result<User>> getUserById(String userId);

    void updateUserProfile(String name, String phone, UserCallback callback);

    void refreshCurrentUser();

    LiveData<Result<List<User>>> getAllCustomers();

    LiveData<Result<List<User>>> searchCustomers(String query);

    interface UserCallback {
        void onSuccess();

        void onError(String error);
    }
}