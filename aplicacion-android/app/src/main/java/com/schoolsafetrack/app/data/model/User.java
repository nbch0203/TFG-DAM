package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private long id;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
