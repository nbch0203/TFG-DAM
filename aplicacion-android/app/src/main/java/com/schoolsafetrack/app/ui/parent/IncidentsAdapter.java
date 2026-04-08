package com.schoolsafetrack.app.ui.parent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Incident;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentsAdapter extends RecyclerView.Adapter<IncidentsAdapter.ViewHolder> {

    private List<Incident> incidents = new ArrayList<>();

    public void setIncidents(List<Incident> list) {
        this.incidents = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(incidents.get(position));
    }

    @Override
    public int getItemCount() { return incidents.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTipo;
        final TextView tvEstado;
        final TextView tvDescripcion;
        final TextView tvRuta;
        final TextView tvFecha;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_incident_icon);
            tvTipo = itemView.findViewById(R.id.tv_incident_tipo);
            tvEstado = itemView.findViewById(R.id.tv_incident_estado);
            tvDescripcion = itemView.findViewById(R.id.tv_incident_descripcion);
            tvRuta = itemView.findViewById(R.id.tv_incident_ruta);
            tvFecha = itemView.findViewById(R.id.tv_incident_fecha);
        }

        void bind(Incident incident) {
            Context ctx = itemView.getContext();

            // Tipo legible
            tvTipo.setText(tipoLegible(ctx, incident.getTipo()));

            // Estado
            if (incident.isResuelto()) {
                tvEstado.setText(ctx.getString(R.string.incident_resolved));
                tvEstado.setBackgroundResource(R.drawable.bg_status_pill_green);
            } else {
                tvEstado.setText(ctx.getString(R.string.incident_pending));
                tvEstado.setBackgroundResource(R.drawable.bg_status_pill);
            }

            // Descripción
            tvDescripcion.setText(incident.getDescripcion());

            // Ruta y matrícula
            String ruta = "";
            if (incident.getRouteNombre() != null) ruta = incident.getRouteNombre();
            if (incident.getMatricula() != null) ruta += (ruta.isEmpty() ? "" : " · ") + incident.getMatricula();
            tvRuta.setText(ruta);

            // Fecha formateada
            tvFecha.setText(formatDate(incident.getCreatedAt()));

            // Color del icono según tipo
            int bgColor = incidentBgColor(ctx, incident.getTipo());
            ivIcon.getBackground().setTint(bgColor);
        }

        private String tipoLegible(Context ctx, String tipo) {
            if (tipo == null) return "";
            switch (tipo) {
                case "RETRASO":    return ctx.getString(R.string.incident_type_retraso);
                case "MECANICO":   return ctx.getString(R.string.incident_type_mecanico);
                case "ACCIDENTE":  return ctx.getString(R.string.incident_type_accidente);
                case "CLIMA":      return ctx.getString(R.string.incident_type_clima);
                case "OTRO":       return ctx.getString(R.string.incident_type_otro);
                default:           return tipo;
            }
        }

        private int incidentBgColor(Context ctx, String tipo) {
            if (tipo == null) return ContextCompat.getColor(ctx, R.color.incident_otro);
            switch (tipo) {
                case "RETRASO":    return ContextCompat.getColor(ctx, R.color.incident_retraso);
                case "MECANICO":   return ContextCompat.getColor(ctx, R.color.incident_mecanico);
                case "ACCIDENTE":  return ContextCompat.getColor(ctx, R.color.incident_accidente);
                case "CLIMA":      return ContextCompat.getColor(ctx, R.color.incident_clima);
                default:           return ContextCompat.getColor(ctx, R.color.incident_otro);
            }
        }

        private String formatDate(String raw) {
            if (raw == null || raw.isEmpty()) return "";
            try {
                // El backend devuelve ISO 8601 o formato MySQL
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = input.parse(raw);
                return date != null ? output.format(date) : raw;
            } catch (Exception e) {
                // Intentar con formato MySQL
                try {
                    SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    Date date = input2.parse(raw);
                    return date != null ? output.format(date) : raw;
                } catch (Exception ex) {
                    return raw;
                }
            }
        }
    }
}
