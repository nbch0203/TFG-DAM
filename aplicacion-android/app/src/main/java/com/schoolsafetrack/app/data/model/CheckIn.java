package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckIn {
    @SerializedName("id")
    private long id;

    @SerializedName("stop_id")
    private long stopId;

    @SerializedName("action")
    private String action;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() { return id; }
    public long getStopId() { return stopId; }
    public String getAction() { return action; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
}
