package com.example.skinshine.data.repository.impl;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryImpl implements OrderRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public OrderRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

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

        // SỬA LẠI QUERY ĐỂ TRÁNH LỖI INDEX
        // Thay vì dùng orderBy với whereEqualTo, ta sẽ sắp xếp trong code
        db.collection("orders")
                .whereEqualTo("userId", userId)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // XÓA DÒNG NÀY
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        ordersResult.postValue(Result.error(e.getMessage()));
                        return;
                    }
                    if (snapshots != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : snapshots) {
                            Order order = document.toObject(Order.class);
                            order.setId(document.getId());
                            orders.add(order);
                        }

                        // SẮP XẾP TRONG CODE THAY VÌ FIRESTORE
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            orders.sort((o1, o2) -> {
                                if (o1.getCreatedAt() == null && o2.getCreatedAt() == null)
                                    return 0;
                                if (o1.getCreatedAt() == null) return 1;
                                if (o2.getCreatedAt() == null) return -1;
                                return o2.getCreatedAt().compareTo(o1.getCreatedAt()); // DESC
                            });
                        }

                        ordersResult.postValue(Result.success(orders));
                    } else {
                        ordersResult.postValue(Result.success(new ArrayList<>()));
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
                        if (order != null) {
                            order.setId(snapshot.getId());
                        }
                        orderResult.postValue(Result.success(order));
                    } else {
                        orderResult.postValue(Result.error("Không tìm thấy đơn hàng"));
                    }
                });
        return orderResult;
    }

    @Override
    public LiveData<Result<List<Order>>> getOrdersByUserId(String userId) {
        MutableLiveData<Result<List<Order>>> ordersResult = new MutableLiveData<>();
        ordersResult.setValue(Result.loading());

        if (userId == null || userId.isEmpty()) {
            ordersResult.setValue(Result.error("User ID không hợp lệ"));
            return ordersResult;
        }

        // CŨNG SỬA TƯƠNG TỰ CHO METHOD NÀY
        db.collection("orders")
                .whereEqualTo("userId", userId)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // XÓA DÒNG NÀY
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        ordersResult.setValue(Result.error(e.getMessage()));
                        return;
                    }

                    if (snapshots != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : snapshots) {
                            Order order = document.toObject(Order.class);
                            order.setId(document.getId());
                            orders.add(order);
                        }

                        // SẮP XẾP TRONG CODE
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            orders.sort((o1, o2) -> {
                                if (o1.getCreatedAt() == null && o2.getCreatedAt() == null)
                                    return 0;
                                if (o1.getCreatedAt() == null) return 1;
                                if (o2.getCreatedAt() == null) return -1;
                                return o2.getCreatedAt().compareTo(o1.getCreatedAt()); // DESC
                            });
                        }

                        ordersResult.setValue(Result.success(orders));
                    } else {
                        ordersResult.setValue(Result.success(new ArrayList<>()));
                    }
                });
        return ordersResult;
    }

    @Override
    public LiveData<Result<List<Order>>> getAllOrders() {
        MutableLiveData<Result<List<Order>>> ordersResult = new MutableLiveData<>();
        ordersResult.setValue(Result.loading());

        // CHỈ LẤY TẤT CẢ ĐƠN HÀNG KHÔNG CẦN SẮP XẾP PHỨC TẠP
        db.collection("orders")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        ordersResult.setValue(Result.error(e.getMessage()));
                        return;
                    }

                    if (snapshots != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : snapshots) {
                            Order order = document.toObject(Order.class);
                            order.setId(document.getId());
                            orders.add(order);
                        }

                        // SẮP XẾP TRONG CODE
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            orders.sort((o1, o2) -> {
                                if (o1.getCreatedAt() == null && o2.getCreatedAt() == null)
                                    return 0;
                                if (o1.getCreatedAt() == null) return 1;
                                if (o2.getCreatedAt() == null) return -1;
                                return o2.getCreatedAt().compareTo(o1.getCreatedAt()); // DESC
                            });
                        }

                        ordersResult.setValue(Result.success(orders));
                    } else {
                        ordersResult.setValue(Result.success(new ArrayList<>()));
                    }
                });

        return ordersResult;
    }
}