package com.example.skinshine.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminViewModel extends ViewModel {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MutableLiveData<List<BarEntry>> revenueData = new MutableLiveData<>();
    private MutableLiveData<List<BarEntry>> ordersData = new MutableLiveData<>();
    private MutableLiveData<List<BarEntry>> usersData = new MutableLiveData<>();
    private MutableLiveData<List<String>> userLabels = new MutableLiveData<>();

    // Store month labels for x-axis
    private MutableLiveData<List<String>> monthLabels = new MutableLiveData<>();

    public LiveData<List<BarEntry>> getRevenueData() {
        return revenueData;
    }

    public LiveData<List<BarEntry>> getOrdersData() {
        return ordersData;
    }

    public LiveData<List<BarEntry>> getUsersData() {
        return usersData;
    }

    public LiveData<List<String>> getMonthLabels() {
        return monthLabels;
    }

    public LiveData<List<String>> getUserLabels() {
        return userLabels;
    }

    public void loadDashboardData() {
        loadRevenueData();
        loadOrdersData();
        loadUsersData();
    }

    private void loadRevenueData() {
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Float> monthlyRevenue = new HashMap<>();
                    Map<String, Date> monthDates = new HashMap<>();
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", new Locale("vi", "VN"));

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp createdAt = document.getTimestamp("createdAt");
                        Double totalAmount = document.getDouble("totalAmount");

                        if (createdAt != null && totalAmount != null) {
                            Date date = createdAt.toDate();
                            String month = monthFormat.format(date);

                            monthlyRevenue.put(month,
                                    monthlyRevenue.getOrDefault(month, 0f) + totalAmount.floatValue());
                            monthDates.put(month, date);
                        }
                    }

                    // Sort months chronologically
                    List<Map.Entry<String, Float>> sortedEntries = new ArrayList<>();
                    for (Map.Entry<String, Float> entry : monthlyRevenue.entrySet()) {
                        sortedEntries.add(entry);
                    }

                    sortedEntries.sort((a, b) -> {
                        Date dateA = monthDates.get(a.getKey());
                        Date dateB = monthDates.get(b.getKey());
                        return dateA.compareTo(dateB);
                    });

                    List<BarEntry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    int index = 0;
                    for (Map.Entry<String, Float> entry : sortedEntries) {
                        entries.add(new BarEntry(index++, entry.getValue()));
                        labels.add(entry.getKey());
                    }

                    revenueData.setValue(entries);
                    monthLabels.setValue(labels);
                })
                .addOnFailureListener(e -> {
                    revenueData.setValue(new ArrayList<>());
                    monthLabels.setValue(new ArrayList<>());
                });
    }

    private void loadOrdersData() {
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> monthlyOrders = new HashMap<>();
                    Map<String, Date> monthDates = new HashMap<>();
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", new Locale("vi", "VN"));

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp createdAt = document.getTimestamp("createdAt");

                        if (createdAt != null) {
                            Date date = createdAt.toDate();
                            String month = monthFormat.format(date);

                            // Replace getOrDefault with manual check
                            Integer currentValue = monthlyOrders.get(month);
                            monthlyOrders.put(month, (currentValue != null ? currentValue : 0) + 1);
                            monthDates.put(month, date);
                        }
                    }

                    // Sort months chronologically
                    List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : monthlyOrders.entrySet()) {
                        sortedEntries.add(entry);
                    }

                    // Replace List.sort with Collections.sort
                    java.util.Collections.sort(sortedEntries, new java.util.Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                            Date dateA = monthDates.get(a.getKey());
                            Date dateB = monthDates.get(b.getKey());
                            return dateA.compareTo(dateB);
                        }
                    });

                    List<BarEntry> entries = new ArrayList<>();
                    int index = 0;
                    for (Map.Entry<String, Integer> entry : sortedEntries) {
                        entries.add(new BarEntry(index++, entry.getValue()));
                    }

                    ordersData.setValue(entries);
                })
                .addOnFailureListener(e -> {
                    ordersData.setValue(new ArrayList<>());
                });
    }


    private void loadUsersData() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> userTypes = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userType = document.getString("userType");
                        String role = document.getString("role");

                        String category = userType != null ? userType :
                                (role != null ? role : "Regular");

                        // Replace getOrDefault with manual check
                        Integer currentValue = userTypes.get(category);
                        userTypes.put(category, (currentValue != null ? currentValue : 0) + 1);
                    }

                    List<BarEntry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    int index = 0;
                    for (Map.Entry<String, Integer> entry : userTypes.entrySet()) {
                        entries.add(new BarEntry(index++, entry.getValue()));
                        labels.add(entry.getKey());
                    }

                    usersData.setValue(entries);
                    userLabels.setValue(labels);
                })
                .addOnFailureListener(e -> {
                    usersData.setValue(new ArrayList<>());
                    userLabels.setValue(new ArrayList<>());
                });
    }
}
