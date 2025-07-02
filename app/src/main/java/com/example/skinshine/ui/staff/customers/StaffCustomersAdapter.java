package com.example.skinshine.ui.staff.customers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skinshine.R;
import com.example.skinshine.data.model.User;

import java.util.List;

public class StaffCustomersAdapter extends RecyclerView.Adapter<StaffCustomersAdapter.CustomerViewHolder> {

    private List<User> customers;
    private OnCustomerClickListener listener;

    public StaffCustomersAdapter(List<User> customers) {
        this.customers = customers;
    }

    public void setOnCustomerClickListener(OnCustomerClickListener listener) {
        this.listener = listener;
    }

    public void updateCustomers(List<User> newCustomers) {
        this.customers = newCustomers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(customers.get(position));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public interface OnCustomerClickListener {
        void onCustomerClick(User customer);
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, phoneTextView, totalSpentTextView;
        ImageView arrowImageView;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            totalSpentTextView = itemView.findViewById(R.id.totalSpentTextView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCustomerClick(customers.get(getAdapterPosition()));
                }
            });
        }

        void bind(User customer) {
            nameTextView.setText(customer.getName() != null ? customer.getName() : "Chưa có tên");
            emailTextView.setText(customer.getEmail() != null ? customer.getEmail() : "Chưa có email");
            phoneTextView.setText(customer.getPhone() != null ? customer.getPhone() : "Chưa có SĐT");
        }
    }
}