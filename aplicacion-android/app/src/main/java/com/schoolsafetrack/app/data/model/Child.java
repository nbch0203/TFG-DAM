package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Child {
    @SerializedName("id")
    private long id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellidos")
    private String apellidos;

    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;

    @SerializedName("curso")
    private String curso;

    @SerializedName("stop_id")
    private Long stopId;

    @SerializedName("stop_nombre")
    private String stopNombre;

    @SerializedName("stop_direccion")
    private String stopDireccion;

    @SerializedName("latitud")
    private Double latitud;

    @SerializedName("longitud")
    private Double longitud;

    @SerializedName("school_nombre")
    private String schoolNombre;

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getCurso() { return curso; }
    public Long getStopId() { return stopId; }
    public String getStopNombre() { return stopNombre; }
    public String getStopDireccion() { return stopDireccion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getSchoolNombre() { return schoolNombre; }

    public String getFullName() {
        if (apellidos != null && !apellidos.isEmpty()) {
            return nombre + " " + apellidos;
        }
        return nombre;
    }

    /** Devuelve la inicial del nombre para el avatar. */
    public String getInitial() {
        return (nombre != null && !nombre.isEmpty())
                ? String.valueOf(nombre.charAt(0)).toUpperCase()
                : "?";
    }
}
