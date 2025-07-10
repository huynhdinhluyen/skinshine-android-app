package com.example.skinshine.ui.checkout;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.CartItem;
import com.example.skinshine.data.model.Order;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.CartRepository;
import com.example.skinshine.data.repository.OrderRepository;
import com.example.skinshine.data.repository.PaymentRepository;
import com.example.skinshine.data.repository.impl.CartRepositoryImpl;
import com.example.skinshine.data.repository.impl.OrderRepositoryImpl;
import com.example.skinshine.data.repository.impl.PaymentRepositoryImpl;

import java.util.List;
import java.util.stream.Collectors;

public class CheckoutViewModel extends ViewModel {
    private static final String ORDER_ID_KEY = "currentOrderId";

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final SavedStateHandle savedStateHandle;

    private final MutableLiveData<Result<String>> _zaloPayTokenResult = new MutableLiveData<>();
    public final LiveData<Result<String>> zaloPayTokenResult = _zaloPayTokenResult;

    private final MutableLiveData<Result<Void>> _postPaymentResult = new MutableLiveData<>();
    public final LiveData<Result<Void>> postPaymentResult = _postPaymentResult;

    private final MutableLiveData<String> shippingAddress = new MutableLiveData<>();

    public void setShippingAddress(String address) {
        shippingAddress.setValue(address);
    }

    public LiveData<String> getShippingAddress() {
        return shippingAddress;
    }

    public CheckoutViewModel(SavedStateHandle savedStateHandle) {
        this.paymentRepository = new PaymentRepositoryImpl();
        this.orderRepository = new OrderRepositoryImpl();
        this.cartRepository = new CartRepositoryImpl();
        this.savedStateHandle = savedStateHandle;
    }

    public void placeOrderAndRequestPayment(List<CartItem> items, String paymentMethod, String address) {
        _zaloPayTokenResult.setValue(Result.loading());
        long totalAmount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalAmount = (long) items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        } else {
            totalAmount = 0;
        }

        Order newOrder = new Order();
        newOrder.setItems(items);
        newOrder.setTotalAmount(totalAmount);
        newOrder.setPaymentMethod(paymentMethod);
        newOrder.setStatus("PENDING_PAYMENT");

        orderRepository.createOrder(newOrder).observeForever(orderResult -> {
            if (orderResult.isSuccess()) {
                String orderId = orderResult.getData();
                savedStateHandle.set(ORDER_ID_KEY, orderId);
                paymentRepository.createZaloPayOrder(String.valueOf(totalAmount))
                        .observeForever(_zaloPayTokenResult::setValue);
            } else {
                _zaloPayTokenResult.setValue(Result.error("Lỗi tạo đơn hàng: " + orderResult.getMessage()));
            }
        });
    }

    public void finalizeSuccessfulPayment(List<CartItem> paidItems) {
        _postPaymentResult.setValue(Result.loading());
        String orderId = savedStateHandle.get(ORDER_ID_KEY);

        if (orderId == null) {
            _postPaymentResult.setValue(Result.error("Không tìm thấy mã đơn hàng để cập nhật."));
            return;
        }

        orderRepository.updateOrderStatus(orderId, "PROCESSING").observeForever(updateResult -> {
            if (updateResult.isSuccess()) {
                List<String> productIdsToRemove = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    productIdsToRemove = paidItems.stream()
                            .map(CartItem::getProductId)
                            .collect(Collectors.toList());
                }

                cartRepository.removeSelectedItems(productIdsToRemove, new CartRepository.CartCallback() {
                    @Override
                    public void onSuccess() {
                        _postPaymentResult.postValue(Result.success(null));
                    }

                    @Override
                    public void onError(String error) {
                        _postPaymentResult.postValue(Result.success(null));
                    }
                });
            } else {
                _postPaymentResult.setValue(Result.error("Lỗi khi cập nhật trạng thái đơn hàng: " + updateResult.getMessage()));
            }
        });
    }
}