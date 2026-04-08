package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
