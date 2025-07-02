package com.example.skinshine.ui.staff.customers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.model.User;
import com.example.skinshine.data.repository.UserRepository;
import com.example.skinshine.data.repository.impl.UserRepositoryImpl;

import java.util.List;

public class StaffCustomersViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<String> searchQueryLiveData;
    private final MediatorLiveData<Result<List<User>>> customersResult;

    public StaffCustomersViewModel() {
        this.userRepository = new UserRepositoryImpl();
        this.searchQueryLiveData = new MutableLiveData<>("");
        this.customersResult = new MediatorLiveData<>();

        setupCustomersObserver();
    }

    public LiveData<Result<List<User>>> getCustomers() {
        return customersResult;
    }

    public void searchCustomers(String query) {
        searchQueryLiveData.setValue(query);
    }

    private void setupCustomersObserver() {
        customersResult.addSource(searchQueryLiveData, query -> {
            LiveData<Result<List<User>>> source;

            if (query == null || query.trim().isEmpty()) {
                source = userRepository.getAllCustomers();
            } else {
                source = userRepository.searchCustomers(query);
            }

            customersResult.addSource(source, customersResult::setValue);
        });
    }
}
