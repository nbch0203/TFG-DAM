package com.schoolsafetrack.app.ui.driver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Stop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.ViewHolder> {

    public interface StopActionListener {
        void onArrival(Stop stop);
        void onDeparture(Stop stop);
    }

    private List<Stop> stops = new ArrayList<>();
    private Set<Long> completedStopIds;
    private StopActionListener listener;

    public void setStops(List<Stop> stops, Set<Long> completedStopIds) {
        this.stops = stops != null ? stops : new ArrayList<>();
        this.completedStopIds = completedStopIds;
        notifyDataSetChanged();
    }

    public void setListener(StopActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(stops.get(position));
    }

    @Override
    public int getItemCount() { return stops.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrden;
        final TextView tvNombre;
        final TextView tvDireccion;
        final TextView tvHora;
        final Button btnArrival;
        final Button btnDeparture;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrden = itemView.findViewById(R.id.tv_stop_orden);
            tvNombre = itemView.findViewById(R.id.tv_stop_nombre);
            tvDireccion = itemView.findViewById(R.id.tv_stop_direccion);
            tvHora = itemView.findViewById(R.id.tv_stop_hora);
            btnArrival = itemView.findViewById(R.id.btn_arrival);
            btnDeparture = itemView.findViewById(R.id.btn_departure);
        }

        void bind(Stop stop) {
            tvOrden.setText(String.valueOf(stop.getOrden()));
            tvNombre.setText(stop.getNombre());
            tvDireccion.setText(stop.getDireccion() != null ? stop.getDireccion() : "");
            tvHora.setText(stop.getHoraEstimada() != null ? stop.getHoraEstimada() : "");

            boolean completed = completedStopIds != null && completedStopIds.contains(stop.getId());
            btnArrival.setEnabled(!completed);
            btnDeparture.setEnabled(!completed);

            if (completed) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }

            btnArrival.setOnClickListener(v -> {
                if (listener != null) listener.onArrival(stop);
            });
            btnDeparture.setOnClickListener(v -> {
                if (listener != null) listener.onDeparture(stop);
            });
        }
    }
}
