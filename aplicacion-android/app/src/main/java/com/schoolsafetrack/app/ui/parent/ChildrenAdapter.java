package com.schoolsafetrack.app.ui.parent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Child;

import java.util.ArrayList;
import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ViewHolder> {

    private List<Child> children = new ArrayList<>();

    public void setChildren(List<Child> list) {
        this.children = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(children.get(position));
    }

    @Override
    public int getItemCount() { return children.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCurso;
        final TextView tvStop;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvCurso = itemView.findViewById(R.id.tv_child_curso);
            tvStop = itemView.findViewById(R.id.tv_child_stop);
        }

        void bind(Child child) {
            tvName.setText(child.getFullName());
            tvCurso.setText(child.getCurso() != null ? child.getCurso() : "");
            String stop = child.getStopNombre() != null
                    ? child.getStopNombre()
                    : itemView.getContext().getString(R.string.stop_not_assigned);
            tvStop.setText(stop);
        }
    }
}
