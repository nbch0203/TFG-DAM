package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class RouteInfo {
    @SerializedName("assignment_id")
    private long assignmentId;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("estado")
    private String estado;

    @SerializedName("hora_inicio_real")
    private String horaInicioReal;

    @SerializedName("hora_fin_real")
    private String horaFinReal;

    @SerializedName("retraso_minutos")
    private Integer retrasoMinutos;

    @SerializedName("route_id")
    private long routeId;

    @SerializedName("route_nombre")
    private String routeNombre;

    @SerializedName("horario_inicio")
    private String horarioInicio;

    @SerializedName("horario_fin")
    private String horarioFin;

    @SerializedName("bus_id")
    private long busId;

    @SerializedName("matricula")
    private String matricula;

    @SerializedName("marca")
    private String marca;

    @SerializedName("modelo")
    private String modelo;

    @SerializedName("bus_estado")
    private String busEstado;

    public long getAssignmentId() { return assignmentId; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getHoraInicioReal() { return horaInicioReal; }
    public String getHoraFinReal() { return horaFinReal; }
    public Integer getRetrasoMinutos() { return retrasoMinutos; }
    public long getRouteId() { return routeId; }
    public String getRouteNombre() { return routeNombre; }
    public String getHorarioInicio() { return horarioInicio; }
    public String getHorarioFin() { return horarioFin; }
    public long getBusId() { return busId; }
    public String getMatricula() { return matricula; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getBusEstado() { return busEstado; }
}
