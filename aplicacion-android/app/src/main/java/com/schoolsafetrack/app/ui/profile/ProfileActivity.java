package com.schoolsafetrack.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.UserProfile;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityProfileBinding;
import com.schoolsafetrack.app.ui.login.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupToolbar();
        observeViewModel();
        setupButtons();

        // Pre-fill email from session immediately as a fallback
        if (binding.etEmail.getText() != null && binding.etEmail.getText().length() == 0) {
            String cachedEmail = session.getEmail();
            if (!cachedEmail.isEmpty()) {
                binding.etEmail.setText(cachedEmail);
                binding.tvAvatarInitial.setText(String.valueOf(cachedEmail.charAt(0)).toUpperCase());
            }
        }

        viewModel.loadProfile(session.getUserId());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.profile_title);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            binding.btnSaveProfile.setEnabled(!Boolean.TRUE.equals(loading));
            binding.btnChangePassword.setEnabled(!Boolean.TRUE.equals(loading));
            binding.btnLogout.setEnabled(!Boolean.TRUE.equals(loading));
        });

        viewModel.getProfile().observe(this, this::populateUi);

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.profile_saved_ok, Toast.LENGTH_SHORT).show();
                // Limpiar campos de contraseña tras guardarla
                binding.etNewPassword.setText("");
                binding.etConfirmPassword.setText("");
            }
        });
    }

    private void populateUi(UserProfile profile) {
        if (profile == null) return;

        // Avatar: primera letra del nombre o del email
        String initial = "?";
        if (profile.getNombre() != null && !profile.getNombre().isEmpty()) {
            initial = String.valueOf(profile.getNombre().charAt(0)).toUpperCase();
        } else if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            initial = String.valueOf(profile.getEmail().charAt(0)).toUpperCase();
        }
        binding.tvAvatarInitial.setText(initial);
        binding.tvFullName.setText(profile.getFullName());
        binding.tvRoleLabel.setText(profile.getRoleLabel());
        binding.tvRole.setText(profile.getRoleLabel());

        // Rellenar campos editables (solo si no los ha modificado el usuario todavía)
        if (binding.etNombre.getText() != null && binding.etNombre.getText().length() == 0) {
            binding.etNombre.setText(profile.getNombre() != null ? profile.getNombre() : "");
        }
        if (binding.etApellidos.getText() != null && binding.etApellidos.getText().length() == 0) {
            binding.etApellidos.setText(profile.getApellidos() != null ? profile.getApellidos() : "");
        }
        if (binding.etEmail.getText() != null && binding.etEmail.getText().length() == 0) {
            binding.etEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
        }
    }

    private void setupButtons() {
        binding.btnSaveProfile.setOnClickListener(v -> savePersonalData());
        binding.btnChangePassword.setOnClickListener(v -> changePassword());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.profile_logout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void savePersonalData() {
        String nombre    = getText(binding.etNombre);
        String apellidos = getText(binding.etApellidos);
        String email     = getText(binding.etEmail);

        // Validación mínima de email
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.profile_email_required));
            return;
        }
        binding.tilEmail.setError(null);

        viewModel.saveProfile(session.getUserId(), nombre, apellidos, email);
    }

    private void changePassword() {
        String newPass     = getText(binding.etNewPassword);
        String confirmPass = getText(binding.etConfirmPassword);

        if (newPass.isEmpty()) {
            binding.tilNewPassword.setError(getString(R.string.profile_password_required));
            return;
        }
        binding.tilNewPassword.setError(null);

        if (newPass.length() < 6) {
            binding.tilNewPassword.setError(getString(R.string.profile_password_min_length));
            return;
        }

        if (!newPass.equals(confirmPass)) {
            binding.tilConfirmPassword.setError(getString(R.string.profile_passwords_dont_match));
            return;
        }
        binding.tilConfirmPassword.setError(null);

        viewModel.changePassword(session.getUserId(), newPass);
    }

    /** Devuelve el texto de un EditText recortado, nunca null. */
    private String getText(android.widget.EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
