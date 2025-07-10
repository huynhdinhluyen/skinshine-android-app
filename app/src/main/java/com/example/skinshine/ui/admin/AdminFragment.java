package com.example.skinshine.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.skinshine.databinding.FragmentAdminBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminFragment extends Fragment {
    private FragmentAdminBinding binding;
    private AdminViewModel viewModel;
    private List<String> monthLabels;
    private List<String> userLabels;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        setupCharts();
        setupObservers();
        viewModel.loadDashboardData();
    }

    private void setupCharts() {
        // Configure revenue chart
        setupBarChart(binding.revenueChart, "Doanh thu (VNĐ)", false);

        // Configure orders chart
        setupBarChart(binding.ordersChart, "Số lượng đơn hàng", true);

        // Configure users chart as BarChart
        setupBarChart(binding.usersChart, "Phân bố người dùng", true);
    }

    private void setupBarChart(BarChart chart, String description, boolean isIntegerOnly) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(12f);
        xAxis.setYOffset(25f);
        xAxis.setXOffset(0f);

        chart.getAxisLeft().setDrawGridLines(false);
        if (isIntegerOnly) {
            chart.getAxisLeft().setGranularity(1f);
            chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
        }
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setExtraBottomOffset(35f);
    }

    private void setupObservers() {
        viewModel.getMonthLabels().observe(getViewLifecycleOwner(), labels -> {
            this.monthLabels = labels;
            // Update both charts when month labels are available
            updateChartLabels();
        });

        viewModel.getUserLabels().observe(getViewLifecycleOwner(), labels -> {
            this.userLabels = labels;
            updateUserChartLabels();
        });

        viewModel.getRevenueData().observe(getViewLifecycleOwner(), this::updateRevenueChart);
        viewModel.getOrdersData().observe(getViewLifecycleOwner(), this::updateOrdersChart);
        viewModel.getUsersData().observe(getViewLifecycleOwner(), this::updateUsersChart);
    }

    private void updateChartLabels() {
        if (monthLabels != null && !monthLabels.isEmpty()) {
            // Update revenue chart labels
            if (binding.revenueChart.getData() != null) {
                XAxis revenueXAxis = binding.revenueChart.getXAxis();
                revenueXAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                revenueXAxis.setLabelCount(monthLabels.size());
                revenueXAxis.setLabelRotationAngle(-45f);
                binding.revenueChart.invalidate();
            }

            // Update orders chart labels
            if (binding.ordersChart.getData() != null) {
                XAxis ordersXAxis = binding.ordersChart.getXAxis();
                ordersXAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                ordersXAxis.setLabelCount(monthLabels.size());
                ordersXAxis.setLabelRotationAngle(-45f);
                binding.ordersChart.invalidate();
            }
        }
    }

    private void updateUserChartLabels() {
        if (userLabels != null && !userLabels.isEmpty()) {
            if (binding.usersChart.getData() != null) {
                XAxis xAxis = binding.usersChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(userLabels));
                xAxis.setLabelCount(userLabels.size());
                xAxis.setLabelRotationAngle(-15f);
                binding.usersChart.invalidate();
            }
        }
    }

    private void updateRevenueChart(List<BarEntry> data) {
        if (data.isEmpty()) {
            binding.revenueChart.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(data, "Doanh thu theo tháng");
        dataSet.setColor(Color.rgb(76, 175, 80));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        // Format values as currency
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format("%.1ftr", value / 1000000);
                } else if (value >= 1000) {
                    return String.format("%.0fn", value / 1000);
                } else {
                    return String.format("%.0f", value);
                }
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        binding.revenueChart.setData(barData);

        // Wait for monthLabels to be available, then set x-axis labels
        viewModel.getMonthLabels().observe(getViewLifecycleOwner(), labels -> {
            if (labels != null && !labels.isEmpty()) {
                XAxis xAxis = binding.revenueChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setLabelCount(labels.size());
                xAxis.setLabelRotationAngle(-45f);
                binding.revenueChart.invalidate();
            }
        });

        binding.revenueChart.invalidate();
    }

    private void updateOrdersChart(List<BarEntry> data) {
        if (data.isEmpty()) {
            binding.ordersChart.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(data, "Đơn hàng theo tháng");
        dataSet.setColor(Color.rgb(33, 150, 243));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        binding.ordersChart.setData(barData);

        // Set custom x-axis labels
        if (monthLabels != null && !monthLabels.isEmpty()) {
            XAxis xAxis = binding.ordersChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
            xAxis.setLabelCount(monthLabels.size());
            xAxis.setLabelRotationAngle(-45f);
        }

        binding.ordersChart.invalidate();
    }

    private void updateUsersChart(List<BarEntry> data) {
        if (data.isEmpty()) {
            binding.usersChart.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(data, "Phân bố người dùng");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        binding.usersChart.setData(barData);

        // Set custom x-axis labels
        if (userLabels != null && !userLabels.isEmpty()) {
            XAxis xAxis = binding.usersChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(userLabels));
            xAxis.setLabelCount(userLabels.size());
            xAxis.setLabelRotationAngle(-15f);
        }

        binding.usersChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
