package com.schoolsafetrack.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.UserProfile;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityProfileBinding;
import com.schoolsafetrack.app.ui.login.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private SessionManager session;

    // Keeps the last loaded profile to pre-fill edit dialogs
    private UserProfile currentProfile;

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
        String cachedEmail = session.getEmail();
        if (!cachedEmail.isEmpty()) {
            binding.tvEmailValue.setText(cachedEmail);
            binding.tvAvatarInitial.setText(String.valueOf(cachedEmail.charAt(0)).toUpperCase());
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
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                setEditButtonsEnabled(false);
            }
            binding.btnLogout.setEnabled(!isLoading);
        });

        viewModel.getProfile().observe(this, profile -> {
            populateUi(profile);
            setEditButtonsEnabled(profile != null);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.profile_saved_ok, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEditButtonsEnabled(boolean enabled) {
        binding.ibEditNombre.setEnabled(enabled);
        binding.ibEditApellidos.setEnabled(enabled);
        binding.ibEditEmail.setEnabled(enabled);
        binding.btnChangePassword.setEnabled(enabled);
    }

    private void populateUi(UserProfile profile) {
        if (profile == null) return;
        currentProfile = profile;

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

        binding.tvNombreValue.setText(profile.getNombre() != null ? profile.getNombre() : "");
        binding.tvApellidosValue.setText(profile.getApellidos() != null ? profile.getApellidos() : "");
        binding.tvEmailValue.setText(profile.getEmail() != null ? profile.getEmail() : "");
    }

    private void setupButtons() {
        binding.ibEditNombre.setOnClickListener(v -> {
            String current = currentProfile != null && currentProfile.getNombre() != null
                    ? currentProfile.getNombre() : "";
            showEditDialog(getString(R.string.profile_nombre), current,
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    newValue -> {
                        if (!newValue.isEmpty()) {
                            String apellidos = currentProfile != null && currentProfile.getApellidos() != null
                                    ? currentProfile.getApellidos() : "";
                            String email = currentProfile != null && currentProfile.getEmail() != null
                                    ? currentProfile.getEmail() : "";
                            viewModel.saveProfile(session.getUserId(), newValue, apellidos, email);
                        }
                    });
        });

        binding.ibEditApellidos.setOnClickListener(v -> {
            String current = currentProfile != null && currentProfile.getApellidos() != null
                    ? currentProfile.getApellidos() : "";
            showEditDialog(getString(R.string.profile_apellidos), current,
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    newValue -> {
                        String nombre = currentProfile != null && currentProfile.getNombre() != null
                                ? currentProfile.getNombre() : "";
                        String email = currentProfile != null && currentProfile.getEmail() != null
                                ? currentProfile.getEmail() : "";
                        viewModel.saveProfile(session.getUserId(), nombre, newValue, email);
                    });
        });

        binding.ibEditEmail.setOnClickListener(v -> {
            String current = currentProfile != null && currentProfile.getEmail() != null
                    ? currentProfile.getEmail() : "";
            showEditDialog(getString(R.string.email), current,
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    newValue -> {
                        if (newValue.isEmpty()) {
                            Toast.makeText(this, R.string.profile_email_required, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String nombre = currentProfile != null && currentProfile.getNombre() != null
                                ? currentProfile.getNombre() : "";
                        String apellidos = currentProfile != null && currentProfile.getApellidos() != null
                                ? currentProfile.getApellidos() : "";
                        viewModel.saveProfile(session.getUserId(), nombre, apellidos, newValue);
                    });
        });

        binding.btnChangePassword.setOnClickListener(v -> showPasswordDialog());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
    }

    /** Shows a single-field edit dialog and calls onSave with the trimmed new value. */
    private void showEditDialog(String label, String currentValue, int inputType,
                                java.util.function.Consumer<String> onSave) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_field, null);
        TextInputLayout til = dialogView.findViewById(R.id.tilDialogField);
        TextInputEditText et = dialogView.findViewById(R.id.etDialogField);

        til.setHint(label);
        et.setInputType(inputType);
        et.setText(currentValue);
        if (et.getText() != null) et.setSelection(et.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(label)
                .setView(dialogView)
                .setPositiveButton(R.string.profile_save_field, (d, w) -> {
                    String newValue = et.getText() != null ? et.getText().toString().trim() : "";
                    onSave.accept(newValue);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_password, null);
        TextInputLayout tilNew = dialogView.findViewById(R.id.tilDialogNewPassword);
        TextInputLayout tilConfirm = dialogView.findViewById(R.id.tilDialogConfirmPassword);
        TextInputEditText etNew = dialogView.findViewById(R.id.etDialogNewPassword);
        TextInputEditText etConfirm = dialogView.findViewById(R.id.etDialogConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.profile_change_password)
                .setView(dialogView)
                .setPositiveButton(R.string.profile_save_field, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Override positive button to validate before closing
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newPass = etNew.getText() != null ? etNew.getText().toString() : "";
            String confirmPass = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

            tilNew.setError(null);
            tilConfirm.setError(null);

            if (newPass.isEmpty()) {
                tilNew.setError(getString(R.string.profile_password_required));
                return;
            }
            if (newPass.length() < 6) {
                tilNew.setError(getString(R.string.profile_password_min_length));
                return;
            }
            if (!newPass.equals(confirmPass)) {
                tilConfirm.setError(getString(R.string.profile_passwords_dont_match));
                return;
            }

            viewModel.changePassword(session.getUserId(), newPass);
            dialog.dismiss();
        }));

        dialog.show();
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
}

