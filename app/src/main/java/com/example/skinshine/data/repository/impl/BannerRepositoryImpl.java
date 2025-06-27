package com.example.skinshine.data.repository.impl;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Result;
import com.example.skinshine.data.repository.BannerRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BannerRepositoryImpl implements BannerRepository {
    private static final String TAG = "BannerRepository";
    private final FirebaseFirestore db;
    private final MutableLiveData<Result<List<BannerItem>>> bannersLiveData;
    private ListenerRegistration bannersListener;

    public BannerRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
        this.bannersLiveData = new MutableLiveData<>();
        attachBannersListener();
    }

    @Override
    public LiveData<Result<List<BannerItem>>> getBanners() {
        return bannersLiveData;
    }

    private void attachBannersListener() {
        if (bannersListener != null) {
            bannersListener.remove();
        }

        bannersLiveData.setValue(Result.loading());

        bannersListener = db.collection("banner_items")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshots != null) {
                        List<BannerItem> banners = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                BannerItem banner = doc.toObject(BannerItem.class);
                                banners.add(banner);
                                Log.d(TAG, "Loaded banner: " + banner.getImageUrl());
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing banner: " + e.getMessage());
                            }
                        }
                            bannersLiveData.setValue(Result.success(banners));
                    }
                });
    }

    public void detachListener() {
        if (bannersListener != null) {
            bannersListener.remove();
            bannersListener = null;
        }
    }
}