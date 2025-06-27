package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;
import com.example.skinshine.data.model.User;

public interface UserRepository {
    LiveData<User> getCurrentUser();
    void updateUserProfile(String name, String phone, UserCallback callback);
    void getUserRole(String userId, RoleCallback callback);

    interface UserCallback {
        void onSuccess();
        void onError(String error);
    }

    interface RoleCallback {
        void onSuccess(String role, Long points);
        void onError(String error);
    }
}
