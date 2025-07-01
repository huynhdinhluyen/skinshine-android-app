package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;

import java.util.List;

public interface OrderRepository {
    LiveData<Result<String>> createOrder(Order order);

    LiveData<Result<Void>> updateOrderStatus(String orderId, String status);

    LiveData<Result<List<Order>>> getOrdersForCurrentUser();

    LiveData<Result<List<Order>>> getAllOrders();

    LiveData<Result<Order>> getOrderById(String orderId);
}
