package com.example.skinshine.data.model;

import com.google.firebase.firestore.PropertyName;

public class BannerItem {
    @PropertyName("image_url")
    private String imageUrl;

    public BannerItem() {
    }

    public BannerItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
