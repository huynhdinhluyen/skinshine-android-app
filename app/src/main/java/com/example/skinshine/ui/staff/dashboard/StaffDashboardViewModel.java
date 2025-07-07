package com.example.skinshine.ui.staff.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StaffDashboardViewModel extends ViewModel {
    private final OrderRepository orderRepository;

    private final MutableLiveData<Integer> _processingOrdersCount = new MutableLiveData<>(0);
    public final LiveData<Integer> processingOrdersCount = _processingOrdersCount;

    private final MutableLiveData<Long> _todayRevenue = new MutableLiveData<>(0L);
    public final LiveData<Long> todayRevenue = _todayRevenue;
    private final MutableLiveData<List<Order>> _processingOrders = new MutableLiveData<>();
    public final LiveData<List<Order>> processingOrders = _processingOrders;

    public StaffDashboardViewModel() {
        this.orderRepository = new OrderRepositoryImpl();
        loadDashboardData();
    }

    public void loadDashboardData() {
        orderRepository.getAllOrders().observeForever(this::processOrdersData);
    }

    private void processOrdersData(Result<List<Order>> result) {
        if (result.isSuccess() && result.getData() != null) {
            int processingCount = 0;
            long revenue = 0;
            Calendar today = Calendar.getInstance();
            List<Order> processingList = new ArrayList<>();
            for (Order order : result.getData()) {
                // Đếm đơn hàng cần xử lý
                if ("PROCESSING".equalsIgnoreCase(order.getStatus())) {
                    processingCount++;
                    processingList.add(order);
                }

                // Tính doanh thu hôm nay (chỉ tính đơn đã xử lý hoặc đã giao)
                if (("PROCESSING".equalsIgnoreCase(order.getStatus()) || "DELIVERED".equalsIgnoreCase(order.getStatus())) && order.getCreatedAt() != null) {
                    Calendar orderDate = Calendar.getInstance();
                    orderDate.setTime(order.getCreatedAt());
                    if (today.get(Calendar.YEAR) == orderDate.get(Calendar.YEAR) &&
                            today.get(Calendar.DAY_OF_YEAR) == orderDate.get(Calendar.DAY_OF_YEAR)) {
                        revenue += order.getTotalAmount();
                    }
                }
            }
            _processingOrders.postValue(processingList);
            _processingOrdersCount.postValue(processingCount);
            _todayRevenue.postValue(revenue);
        }
    }
}
