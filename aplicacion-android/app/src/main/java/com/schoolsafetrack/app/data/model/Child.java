package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Child {
    @SerializedName("id")
    private long id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellidos")
    private String apellidos;

    @SerializedName("curso")
    private String curso;

    @SerializedName("stop_nombre")
    private String stopNombre;

    @SerializedName("stop_direccion")
    private String stopDireccion;

    @SerializedName("latitud")
    private Double latitud;

    @SerializedName("longitud")
    private Double longitud;

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getCurso() { return curso; }
    public String getStopNombre() { return stopNombre; }
    public String getStopDireccion() { return stopDireccion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }

    public String getFullName() {
        if (apellidos != null && !apellidos.isEmpty()) {
            return nombre + " " + apellidos;
        }
        return nombre;
    }
}
