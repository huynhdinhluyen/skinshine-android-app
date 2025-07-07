package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.User;

public interface UserRepository {
    LiveData<Result<User>> getCurrentUser();

    LiveData<Result<User>> getUserById(String userId);

    void updateUserProfile(String name, String phone, UserCallback callback);

    void refreshCurrentUser();

    interface UserCallback {
        void onSuccess();

        void onError(String error);
    }
}