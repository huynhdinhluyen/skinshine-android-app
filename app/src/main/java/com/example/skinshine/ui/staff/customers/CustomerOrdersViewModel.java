package com.example.skinshine.ui.staff.customers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;

import java.util.List;

public class CustomerOrdersViewModel extends ViewModel {
    private final OrderRepository orderRepository;
    private final MutableLiveData<String> customerIdLiveData;

    public CustomerOrdersViewModel() {
        this.orderRepository = new OrderRepositoryImpl();
        this.customerIdLiveData = new MutableLiveData<>();
    }

    public LiveData<Result<List<Order>>> getCustomerOrders() {
        return Transformations.switchMap(customerIdLiveData, customerId -> {
            if (customerId == null || customerId.isEmpty()) {
                MutableLiveData<Result<List<Order>>> errorResult = new MutableLiveData<>();
                errorResult.setValue(Result.error("Customer ID không hợp lệ"));
                return errorResult;
            }
            return orderRepository.getOrdersByUserId(customerId);
        });
    }

    public void setCustomerId(String customerId) {
        customerIdLiveData.setValue(customerId);
    }
}