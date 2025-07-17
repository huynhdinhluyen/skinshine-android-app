package com.example.skinshine.data.repository.impl;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.PaymentRepository;
import com.example.skinshine.data.service.payment.Api.CreateOrder;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentRepositoryImpl implements PaymentRepository {
    private static final String TAG = "PaymentRepositoryImpl";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public LiveData<Result<String>> createZaloPayOrder(String amount) {
        MutableLiveData<Result<String>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Result.loading());

        executor.execute(() -> {
            try {
                CreateOrder orderApi = new CreateOrder();
                JSONObject data = orderApi.createOrder(amount);
                String returnCode = data.getString("return_code");

                if ("1".equals(returnCode)) {
                    String zpTransToken = data.getString("zp_trans_token");
                    resultLiveData.postValue(Result.success(zpTransToken));
                } else {
                    String returnMessage = data.getString("return_message");
                    resultLiveData.postValue(Result.error(returnMessage));
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo đơn hàng ZaloPay", e);
                resultLiveData.postValue(Result.error("Lỗi không xác định: " + e.getMessage()));
            }
        });

        return resultLiveData;
    }
}