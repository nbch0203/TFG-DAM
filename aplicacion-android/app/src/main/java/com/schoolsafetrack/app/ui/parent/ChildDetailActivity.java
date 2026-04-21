package com.schoolsafetrack.app.ui.parent;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityChildDetailBinding;

/**
 * Pantalla de detalle de un alumno:
 * – Información personal (nombre, fecha nacimiento, curso, colegio)
 * – Parada asignada
 * – Lista de incidencias registradas en su trayecto
 *
 * Recibe el objeto {@link Child} serializado en el Intent con la clave
 * {@link #EXTRA_CHILD_ID} y {@link #EXTRA_CHILD_NOMBRE}.
 */
public class ChildDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CHILD_ID = "extra_child_id";
    public static final String EXTRA_CHILD_NOMBRE = "extra_child_nombre";
    public static final String EXTRA_CHILD_APELLIDOS = "extra_child_apellidos";
    public static final String EXTRA_CHILD_CURSO = "extra_child_curso";
    public static final String EXTRA_CHILD_FECHA_NAC = "extra_child_fecha_nac";
    public static final String EXTRA_CHILD_SCHOOL = "extra_child_school";
    public static final String EXTRA_CHILD_STOP_NOMBRE = "extra_child_stop_nombre";
    public static final String EXTRA_CHILD_STOP_DIR = "extra_child_stop_dir";
    public static final String EXTRA_CHILD_AVATAR_COLOR = "extra_child_avatar_color";

    private ActivityChildDetailBinding binding;
    private ChildDetailViewModel viewModel;
    private IncidentsAdapter incidentsAdapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChildDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(ChildDetailViewModel.class);

        // Recuperar datos del Intent
        long childId = getIntent().getLongExtra(EXTRA_CHILD_ID, -1);
        String nombre = getIntent().getStringExtra(EXTRA_CHILD_NOMBRE);
        String apellidos = getIntent().getStringExtra(EXTRA_CHILD_APELLIDOS);
        String curso = getIntent().getStringExtra(EXTRA_CHILD_CURSO);
        String fechaNac = getIntent().getStringExtra(EXTRA_CHILD_FECHA_NAC);
        String school = getIntent().getStringExtra(EXTRA_CHILD_SCHOOL);
        String stopNombre = getIntent().getStringExtra(EXTRA_CHILD_STOP_NOMBRE);
        String stopDir = getIntent().getStringExtra(EXTRA_CHILD_STOP_DIR);
        int avatarColor = getIntent().getIntExtra(EXTRA_CHILD_AVATAR_COLOR, getColor(R.color.avatar_1));

        setupToolbar(nombre, apellidos);
        bindChildData(nombre, apellidos, curso, fechaNac, school, stopNombre, stopDir, avatarColor);
        setupRecyclerView();
        observeViewModel();

        if (childId > 0) {
            viewModel.loadIncidents(session.getUserId(), childId);
        }
    }

    private void setupToolbar(String nombre, String apellidos) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.child_detail);
        }
    }

    private void bindChildData(String nombre, String apellidos, String curso,
                                String fechaNac, String school,
                                String stopNombre, String stopDir, int avatarColor) {
        String fullName = (apellidos != null && !apellidos.isEmpty())
                ? nombre + " " + apellidos
                : nombre;

        // Cabecera
        String initial = (nombre != null && !nombre.isEmpty())
                ? String.valueOf(nombre.charAt(0)).toUpperCase()
                : "?";
        binding.tvHeaderAvatar.setText(initial);
        binding.tvHeaderAvatar.setBackground(makeAvatarBg(avatarColor));
        binding.tvHeaderName.setText(fullName);
        binding.tvHeaderCurso.setText(curso != null ? curso : getString(R.string.not_available));

        // Tarjeta de datos personales
        binding.tvFullName.setText(fullName);
        binding.tvBirthDate.setText(fechaNac != null ? formatDate(fechaNac) : getString(R.string.not_available));
        binding.tvCurso.setText(curso != null ? curso : getString(R.string.not_available));
        binding.tvSchool.setText(school != null ? school : getString(R.string.not_available));

        // Parada
        binding.tvStopName.setText(stopNombre != null ? stopNombre : getString(R.string.stop_not_assigned));
        binding.tvStopAddress.setText(stopDir != null ? stopDir : getString(R.string.not_available));
    }

    private android.graphics.drawable.GradientDrawable makeAvatarBg(int color) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        shape.setColor(color);
        return shape;
    }

    private void setupRecyclerView() {
        incidentsAdapter = new IncidentsAdapter();
        binding.rvIncidents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvIncidents.setAdapter(incidentsAdapter);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, loading -> {
            binding.progressIncidents.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });

        viewModel.getIncidents().observe(this, incidents -> {
            incidentsAdapter.setIncidents(incidents);
            binding.tvNoIncidents.setVisibility(
                    incidents == null || incidents.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        // Simplificado: devolver primeros 10 caracteres (yyyy-MM-dd)
        try {
            String dateOnly = raw.substring(0, 10);
            String[] parts = dateOnly.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch (Exception ignored) {}
        return raw;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
