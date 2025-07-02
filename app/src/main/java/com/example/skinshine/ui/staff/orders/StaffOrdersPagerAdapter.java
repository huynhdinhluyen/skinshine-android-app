package com.example.skinshine.ui.staff.orders;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Arrays;
import java.util.List;

public class StaffOrdersPagerAdapter extends FragmentStateAdapter {

    private final List<String> statusList = Arrays.asList("PROCESSING", "DELIVERING", "DELIVERED", "CANCELLED");

    public StaffOrdersPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OrderStatusListFragment.newInstance(statusList.get(position));
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public String getPageTitle(int position) {
        switch (statusList.get(position)) {
            case "PROCESSING":
                return "Đang xử lý";
            case "DELIVERING":
                return "Đang giao";
            case "DELIVERED":
                return "Đã giao";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return "";
        }
    }
}
