package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("user")
    private UserProfile user;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() { return success; }
    public UserProfile getUser() { return user; }
    public String getError() { return error; }
}
