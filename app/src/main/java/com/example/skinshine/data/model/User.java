package com.example.skinshine.data.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Long point;

    public User() {
    }

    public User(String id, String name, String email, String phone, String role, Long point) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.point = point;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getPoint() {
        return point;
    }

    public void setPoint(Long point) {
        this.point = point;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : "Người dùng";
    }
}
