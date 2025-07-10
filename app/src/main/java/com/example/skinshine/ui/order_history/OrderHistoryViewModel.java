package com.example.skinshine.ui.order_history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;

import java.util.List;

public class OrderHistoryViewModel extends ViewModel {
    private final OrderRepository orderRepository;

    public OrderHistoryViewModel() {
        this.orderRepository = new OrderRepositoryImpl();
    }

    public LiveData<Result<List<Order>>> getOrderHistory() {
        return orderRepository.getOrdersForCurrentUser();
    }
}
