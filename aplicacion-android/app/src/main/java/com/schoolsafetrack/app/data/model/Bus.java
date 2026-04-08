package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Bus {
    @SerializedName("id")
    private long id;

    @SerializedName("matricula")
    private String matricula;

    @SerializedName("marca")
    private String marca;

    @SerializedName("modelo")
    private String modelo;

    @SerializedName("estado")
    private String estado;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lon")
    private Double lon;

    @SerializedName("route_nombre")
    private String routeNombre;

    @SerializedName("horario_inicio")
    private String horarioInicio;

    @SerializedName("horario_fin")
    private String horarioFin;

    public long getId() { return id; }
    public String getMatricula() { return matricula; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getEstado() { return estado; }
    public Double getLat() { return lat; }
    public Double getLon() { return lon; }
    public String getRouteNombre() { return routeNombre; }
    public String getHorarioInicio() { return horarioInicio; }
    public String getHorarioFin() { return horarioFin; }
}
