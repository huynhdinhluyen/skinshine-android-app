package com.example.skinshine.ui.staff.order_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.User;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.UserRepository;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;
import com.example.skinshine.data.repository.impl.UserRepositoryImpl;

public class StaffOrderDetailViewModel extends ViewModel {

    public final LiveData<Result<Order>> order;
    public final LiveData<Result<User>> customer;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<String> _orderId = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> _updateStatusResult = new MutableLiveData<>();

    public StaffOrderDetailViewModel() {
        this.orderRepository = new OrderRepositoryImpl();
        this.userRepository = new UserRepositoryImpl();

        order = Transformations.switchMap(_orderId, orderRepository::getOrderById);
        customer = Transformations.switchMap(order, orderResult -> {
            if (orderResult.isSuccess() && orderResult.getData() != null) {
                return userRepository.getUserById(orderResult.getData().getUserId());
            }
            return new MutableLiveData<>(Result.error("Không có ID người dùng"));
        });
    }

    public LiveData<Result<Void>> getUpdateStatusResult() {
        return _updateStatusResult;
    }

    public void loadOrder(String orderId) {
        if (orderId != null && !orderId.equals(_orderId.getValue())) {
            _orderId.setValue(orderId);
        }
    }

    public void updateOrderStatus(String newStatus) {
        String currentOrderId = _orderId.getValue();
        if (currentOrderId != null && !newStatus.isEmpty()) {
            orderRepository.updateOrderStatus(currentOrderId, newStatus)
                    .observeForever(_updateStatusResult::setValue);
        }
    }
}
