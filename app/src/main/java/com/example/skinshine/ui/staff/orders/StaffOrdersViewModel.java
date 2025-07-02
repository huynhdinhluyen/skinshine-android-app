package com.example.skinshine.ui.staff.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class StaffOrdersViewModel extends ViewModel {
    private final OrderRepository orderRepository;
    private LiveData<Result<List<Order>>> allOrders;

    public StaffOrdersViewModel() {
        this.orderRepository = new OrderRepositoryImpl();
        this.allOrders = orderRepository.getAllOrders();
    }

    public LiveData<Result<List<Order>>> getAllOrders() {
        return allOrders;
    }

    public LiveData<List<Order>> getOrdersByStatus(String status) {
        return Transformations.map(allOrders, result -> {
            List<Order> filteredList = new ArrayList<>();
            if (result.isSuccess() && result.getData() != null) {
                for (Order order : result.getData()) {
                    if (status.equalsIgnoreCase(order.getStatus())) {
                        filteredList.add(order);
                    }
                }
            }
            return filteredList;
        });
    }
}