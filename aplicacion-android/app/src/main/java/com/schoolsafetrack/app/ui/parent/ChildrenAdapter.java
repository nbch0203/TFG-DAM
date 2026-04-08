package com.schoolsafetrack.app.ui.parent;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Child;

import java.util.ArrayList;
import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ViewHolder> {

    /** Interfaz de callback al hacer click en un hijo. */
    public interface OnChildClickListener {
        void onChildClick(Child child, int avatarColor);
    }

    private static final int[] AVATAR_COLORS = {
            R.color.avatar_1, R.color.avatar_2, R.color.avatar_3,
            R.color.avatar_4, R.color.avatar_5, R.color.avatar_6
    };

    private List<Child> children = new ArrayList<>();
    private OnChildClickListener listener;

    public void setChildren(List<Child> list) {
        this.children = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setListener(OnChildClickListener listener) {
        this.listener = listener;
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
        Child child = children.get(position);
        int colorResId = AVATAR_COLORS[position % AVATAR_COLORS.length];
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorResId);
        holder.bind(child, color);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChildClick(child, color);
        });
    }

    @Override
    public int getItemCount() { return children.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvName;
        final TextView tvCurso;
        final TextView tvStop;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvCurso = itemView.findViewById(R.id.tv_child_curso);
            tvStop = itemView.findViewById(R.id.tv_child_stop);
        }

        void bind(Child child, int avatarColor) {
            Context ctx = itemView.getContext();

            // Avatar con inicial y color
            tvAvatar.setText(child.getInitial());
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(avatarColor);
            tvAvatar.setBackground(circle);

            tvName.setText(child.getFullName());
            tvCurso.setText(child.getCurso() != null && !child.getCurso().isEmpty()
                    ? child.getCurso() : "");
            String stop = child.getStopNombre() != null
                    ? child.getStopNombre()
                    : ctx.getString(R.string.stop_not_assigned);
            tvStop.setText(stop);
        }
    }
}
