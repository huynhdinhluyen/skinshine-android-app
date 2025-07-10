package com.example.skinshine.data.repository.impl;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.User;
import com.example.skinshine.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<Result<User>> currentUserLiveData;
    private ListenerRegistration userListener;

    public UserRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.currentUserLiveData = new MutableLiveData<>();
        attachCurrentUserListener();
    }

    @Override
    public LiveData<Result<User>> getCurrentUser() {
        return currentUserLiveData;
    }

    @Override
    public LiveData<Result<User>> getUserById(String userId) {
        MutableLiveData<Result<User>> userLiveData = new MutableLiveData<>();
        userLiveData.setValue(Result.loading());

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        try {
                            User user = parseUser(doc);
                            userLiveData.setValue(Result.success(user));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + e.getMessage());
                            userLiveData.setValue(Result.error("Error parsing user data"));
                        }
                    } else {
                        userLiveData.setValue(Result.error("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user: " + e.getMessage());
                    userLiveData.setValue(Result.error(e.getMessage()));
                });

        return userLiveData;
    }

    @Override
    public void updateUserProfile(String name, String phone, UserCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .update("name", name, "phone", phone)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user profile: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void refreshCurrentUser() {
        // Force refresh by detaching and reattaching listener
        if (userListener != null) {
            userListener.remove();
        }
        attachCurrentUserListener();
    }

    @Override
    public LiveData<Result<List<User>>> getAllCustomers() {
        MutableLiveData<Result<List<User>>> customersLiveData = new MutableLiveData<>();
        customersLiveData.setValue(Result.loading());

        db.collection("users")
                .whereEqualTo("role", "customer")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        customersLiveData.setValue(Result.error(e.getMessage()));
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<User> customers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            User customer = parseUser(document);
                            customers.add(customer);
                        }
                    }
                });

        return customersLiveData;
    }

    @Override
    public LiveData<Result<List<User>>> searchCustomers(String query) {
        MutableLiveData<Result<List<User>>> searchResult = new MutableLiveData<>();
        searchResult.setValue(Result.loading());

        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }

        String searchQuery = query.toLowerCase().trim();

        db.collection("users")
                .whereEqualTo("role", "customer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> filteredCustomers = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User customer = parseUser(document);

                        if (matchesSearchQuery(customer, searchQuery)) {
                            filteredCustomers.add(customer);
                        }
                    }
                })
                .addOnFailureListener(e -> searchResult.setValue(Result.error(e.getMessage())));

        return searchResult;
    }

    private void attachCurrentUserListener() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            currentUserLiveData.setValue(Result.error("User not authenticated"));
            return;
        }

        if (userListener != null) {
            userListener.remove();
        }

        currentUserLiveData.setValue(Result.loading());

        userListener = db.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to user changes", error);
                        currentUserLiveData.setValue(Result.error(error.getMessage()));
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        try {
                            User user = parseUser(snapshot);
                            currentUserLiveData.setValue(Result.success(user));
                            Log.d(TAG, "User data updated: " + user.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + e.getMessage());
                            currentUserLiveData.setValue(Result.error("Error parsing user data"));
                        }
                    } else {
                        Log.w(TAG, "User document not found");
                        currentUserLiveData.setValue(Result.error("User data not found"));
                    }
                });
    }

    private boolean matchesSearchQuery(User customer, String searchQuery) {
        return (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(searchQuery)) ||
                (customer.getPhone() != null && customer.getPhone().contains(searchQuery)) ||
                (customer.getName() != null && customer.getName().toLowerCase().contains(searchQuery));
    }

    private User parseUser(DocumentSnapshot doc) {
        User user = new User();
        user.setId(doc.getId());
        user.setName(doc.getString("name"));
        user.setEmail(doc.getString("email"));
        user.setPhone(doc.getString("phone"));
        user.setRole(doc.getString("role"));
        user.setPoint(doc.getLong("point"));
        return user;
    }

    public void detachListener() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }
}
