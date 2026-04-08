package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("id")
    private long id;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellidos")
    private String apellidos;

    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }

    public String getFullName() {
        if (nombre == null && apellidos == null) return email;
        StringBuilder sb = new StringBuilder();
        if (nombre != null) sb.append(nombre);
        if (apellidos != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(apellidos);
        }
        return sb.toString();
    }

    public String getRoleLabel() {
        if (role == null) return "";
        switch (role) {
            case "PARENT":   return "Padre / Madre";
            case "DRIVER":   return "Conductor";
            case "ADMIN":    return "Administrador";
            case "PROFESOR": return "Profesor";
            default:         return role;
        }
    }
}
