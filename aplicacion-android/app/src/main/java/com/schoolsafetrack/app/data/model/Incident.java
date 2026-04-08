package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Incident {
    @SerializedName("id")
    private long id;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("latitud")
    private Double latitud;

    @SerializedName("longitud")
    private Double longitud;

    @SerializedName("resuelto")
    private int resuelto;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public boolean isResuelto() { return resuelto == 1; }
    public String getCreatedAt() { return createdAt; }
}
