package com.example.skinshine.data.repository.impl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepositoryImpl implements AuthRepository {
    private static final String TAG = "AuthRepository";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepositoryImpl() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public LiveData<Result<String>> login(String email, String password) {
        MutableLiveData<Result<String>> loginResult = new MutableLiveData<>();
        loginResult.setValue(Result.loading());

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            getUserRole(user.getUid(), loginResult);
                        } else {
                            loginResult.setValue(Result.error("Không thể lấy thông tin người dùng"));
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Đăng nhập thất bại";
                        loginResult.setValue(Result.error(errorMessage));
                    }
                });

        return loginResult;
    }

    @Override
    public LiveData<Result<String>> register(String email, String password, String fullName, String phone) {
        MutableLiveData<Result<String>> registerResult = new MutableLiveData<>();
        registerResult.setValue(Result.loading());

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Update profile
                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                            .setDisplayName(fullName)
                                            .build())
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Save user data to Firestore
                                            saveUserToFirestore(user.getUid(), fullName, email, phone, registerResult);
                                        } else {
                                            registerResult.setValue(Result.error("Lỗi khi cập nhật profile"));
                                        }
                                    });
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Đăng ký thất bại";
                        registerResult.setValue(Result.error(errorMessage));
                    }
                });

        return registerResult;
    }

    @Override
    public void logout() {
        auth.signOut();
    }

    @Override
    public LiveData<Result<String>> getCurrentUserRole() {
        MutableLiveData<Result<String>> roleResult = new MutableLiveData<>();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            roleResult.setValue(Result.success("customer"));
            return roleResult;
        }

        roleResult.setValue(Result.loading());
        getUserRole(currentUser.getUid(), roleResult);
        return roleResult;
    }

    private void getUserRole(String userId, MutableLiveData<Result<String>> resultLiveData) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = "customer"; // Default role
                    if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                        role = documentSnapshot.getString("role");
                    }
                    resultLiveData.setValue(Result.success(role));
                })
                .addOnFailureListener(e -> {
                    resultLiveData.setValue(Result.success("customer"));
                });
    }

    private void saveUserToFirestore(String userId, String fullName, String email, String phone,
                                     MutableLiveData<Result<String>> resultLiveData) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", "customer");
        userData.put("point", 0L);

        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    resultLiveData.setValue(Result.success("customer"));
                })
                .addOnFailureListener(e -> {
                    resultLiveData.setValue(Result.error("Lỗi khi lưu dữ liệu người dùng"));
                });
    }
}
