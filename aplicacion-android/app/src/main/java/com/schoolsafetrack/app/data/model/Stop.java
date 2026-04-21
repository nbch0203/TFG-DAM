package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Stop {
    @SerializedName("id")
    private long id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("latitud")
    private Double latitud;

    @SerializedName("longitud")
    private Double longitud;

    @SerializedName("orden")
    private int orden;

    @SerializedName("hora_estimada")
    private String horaEstimada;

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public int getOrden() { return orden; }
    public String getHoraEstimada() { return horaEstimada; }
}
