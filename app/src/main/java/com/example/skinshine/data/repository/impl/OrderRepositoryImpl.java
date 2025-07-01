package com.example.skinshine.data.repository.impl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    public LiveData<Result<String>> createOrder(Order order) {
        MutableLiveData<Result<String>> orderIdResult = new MutableLiveData<>();
        if (auth.getCurrentUser() == null) {
            orderIdResult.setValue(Result.error("Người dùng chưa đăng nhập"));
            return orderIdResult;
        }
        order.setUserId(auth.getCurrentUser().getUid());

        db.collection("orders").add(order)
                .addOnSuccessListener(docRef -> orderIdResult.postValue(Result.success(docRef.getId())))
                .addOnFailureListener(e -> orderIdResult.postValue(Result.error(e.getMessage())));
        return orderIdResult;
    }

    @Override
    public LiveData<Result<Void>> updateOrderStatus(String orderId, String status) {
        MutableLiveData<Result<Void>> updateResult = new MutableLiveData<>();
        db.collection("orders").document(orderId).update("status", status)
                .addOnSuccessListener(aVoid -> updateResult.postValue(Result.success(null)))
                .addOnFailureListener(e -> updateResult.postValue(Result.error(e.getMessage())));
        return updateResult;
    }

    @Override
    public LiveData<Result<List<Order>>> getOrdersForCurrentUser() {
        MutableLiveData<Result<List<Order>>> ordersResult = new MutableLiveData<>();
        if (auth.getCurrentUser() == null) {
            ordersResult.setValue(Result.error("Người dùng chưa đăng nhập"));
            return ordersResult;
        }
        String userId = auth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        ordersResult.postValue(Result.error(e.getMessage()));
                        return;
                    }
                    if (snapshots != null) {
                        List<Order> orders = snapshots.toObjects(Order.class);
                        ordersResult.postValue(Result.success(orders));
                    }
                });
        return ordersResult;
    }

    @Override
    public LiveData<Result<List<Order>>> getAllOrders() {
        MutableLiveData<Result<List<Order>>> ordersResult = new MutableLiveData<>();
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        ordersResult.postValue(Result.error(e.getMessage()));
                        return;
                    }
                    if (snapshots != null) {
                        List<Order> orders = snapshots.toObjects(Order.class);
                        ordersResult.postValue(Result.success(orders));
                    }
                });
        return ordersResult;
    }

    @Override
    public LiveData<Result<Order>> getOrderById(String orderId) {
        MutableLiveData<Result<Order>> orderResult = new MutableLiveData<>();
        if (orderId == null || orderId.isEmpty()) {
            orderResult.postValue(Result.error("Order ID không hợp lệ"));
            return orderResult;
        }
        db.collection("orders").document(orderId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        orderResult.postValue(Result.error(e.getMessage()));
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Order order = snapshot.toObject(Order.class);
                        orderResult.postValue(Result.success(order));
                    } else {
                        orderResult.postValue(Result.error("Không tìm thấy đơn hàng"));
                    }
                });
        return orderResult;
    }
}