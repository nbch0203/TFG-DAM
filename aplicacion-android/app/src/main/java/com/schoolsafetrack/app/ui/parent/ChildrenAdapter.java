package com.schoolsafetrack.app.ui.parent;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.repository.ProfileImageUtils;
import com.schoolsafetrack.app.data.repository.ProfilePhotoStore;

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
        final ImageView ivAvatarPhoto;
        final TextView tvName;
        final TextView tvCurso;
        final TextView tvStop;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            ivAvatarPhoto = itemView.findViewById(R.id.iv_avatar_photo);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvCurso = itemView.findViewById(R.id.tv_child_curso);
            tvStop = itemView.findViewById(R.id.tv_child_stop);
        }

        void bind(Child child, int avatarColor) {
            Context ctx = itemView.getContext();

            // Try to load child photo from internal store. If available show image,
            // otherwise show initial with colored background.
            try {
                ProfilePhotoStore store = new ProfilePhotoStore(ctx);
                java.io.File photo = store.getChildPhoto(child.getId());
                if (photo != null && ProfileImageUtils.loadIntoImageView(ctx, photo, ivAvatarPhoto)) {
                    ivAvatarPhoto.setVisibility(View.VISIBLE);
                    tvAvatar.setVisibility(View.GONE);
                } else {
                    ivAvatarPhoto.setImageDrawable(null);
                    ivAvatarPhoto.setVisibility(View.GONE);
                    // Avatar con inicial y color
                    tvAvatar.setText(child.getInitial());
                    GradientDrawable circle = new GradientDrawable();
                    circle.setShape(GradientDrawable.OVAL);
                    circle.setColor(avatarColor);
                    tvAvatar.setBackground(circle);
                    tvAvatar.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {
                // On any failure fallback to initial avatar
                ivAvatarPhoto.setImageDrawable(null);
                ivAvatarPhoto.setVisibility(View.GONE);
                tvAvatar.setText(child.getInitial());
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(avatarColor);
                tvAvatar.setBackground(circle);
                tvAvatar.setVisibility(View.VISIBLE);
            }

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
