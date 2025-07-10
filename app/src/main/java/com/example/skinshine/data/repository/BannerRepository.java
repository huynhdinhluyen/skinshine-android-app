package com.example.skinshine.data.repository;

import androidx.lifecycle.LiveData;

import com.example.skinshine.data.model.BannerItem;
import com.example.skinshine.data.model.Result;

import java.util.List;

public interface BannerRepository {
    LiveData<Result<List<BannerItem>>> getBanners();
}
