package com.example.skinshine.data.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Product {
    private String id;
    private String name;
    private DocumentReference brand;
    private String description;
    private String ingredients;
    private Long price;
    @PropertyName("image_url")
    private String imageUrl;
    private DocumentReference category;
    private int rating;
    @PropertyName("suitable_skin_types")
    private List<String> suitableSkinTypes;
    @PropertyName("is_active")
    private boolean isActive;

    public Product() {
    }

    public Product(String id, String name, DocumentReference brand, String description, String ingredients, Long price, String imageUrl, DocumentReference category, int rating, List<String> suitableSkinTypes, boolean isActive) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.ingredients = ingredients;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.rating = rating;
        this.suitableSkinTypes = suitableSkinTypes;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentReference getBrand() {
        return brand;
    }

    public void setBrand(DocumentReference brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public DocumentReference getCategory() {
        return category;
    }

    public void setCategory(DocumentReference category) {
        this.category = category;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
    @PropertyName("suitable_skin_types")
    public List<String> getSuitableSkinTypes() {
        return suitableSkinTypes;
    }

    @PropertyName("suitable_skin_types")
    public void setSuitableSkinTypes(List<String> suitableSkinTypes) {
        this.suitableSkinTypes = suitableSkinTypes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
