package com.example.skinshine.ui.staff.customers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;

import java.util.ArrayList;

public class StaffCustomersFragment extends Fragment {

    private StaffCustomersViewModel viewModel;
    private StaffCustomersAdapter adapter;
    private EditText searchEditText;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_customers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StaffCustomersViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        setupSearchView(view);
        observeViewModel();
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        searchEditText = view.findViewById(R.id.searchEditText);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerCustomers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StaffCustomersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnCustomerClickListener(customer -> {
            Bundle bundle = new Bundle();
            bundle.putString("customerId", customer.getId());
            Navigation.findNavController(view).navigate(R.id.action_global_customerOrdersFragment, bundle);
        });
    }

    private void setupSearchView(View view) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCustomers().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                showLoading(true);
            } else if (result.isSuccess()) {
                showLoading(false);
                adapter.updateCustomers(result.getData());
            } else if (result.isError()) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}