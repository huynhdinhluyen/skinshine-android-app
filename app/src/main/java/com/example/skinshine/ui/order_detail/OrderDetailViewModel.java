package com.example.skinshine.ui.order_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;

public class OrderDetailViewModel extends ViewModel {
    private final OrderRepository orderRepository;
    private final MutableLiveData<String> _orderId = new MutableLiveData<>();

    public LiveData<Result<Order>> order;

    public OrderDetailViewModel() {
        this.orderRepository = new OrderRepositoryImpl();

        order = Transformations.switchMap(_orderId, orderRepository::getOrderById);
    }

    public void loadOrder(String orderId) {
        if (orderId != null && !orderId.equals(_orderId.getValue())) {
            _orderId.setValue(orderId);
        }
    }
}
