package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.Result;

public interface PaymentRepository {
    LiveData<Result<String>> createZaloPayOrder(String amount);
}
