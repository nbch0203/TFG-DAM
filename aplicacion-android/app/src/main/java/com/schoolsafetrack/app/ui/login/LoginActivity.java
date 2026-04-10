package com.schoolsafetrack.app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityLoginBinding;
import com.schoolsafetrack.app.ui.driver.DriverMainActivity;
import com.schoolsafetrack.app.ui.parent.ParentMainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Pre-fill server URL with the previously saved value (or the default)
        binding.etServerUrl.setText(session.getServerUrl());

        observeViewModel();
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String serverUrl = binding.etServerUrl.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        // Validate server URL
        if (serverUrl.isEmpty() || !serverUrl.startsWith("http")) {
            binding.tilServerUrl.setError(getString(R.string.server_url_error));
            return;
        }
        binding.tilServerUrl.setError(null);
        // Ensure trailing slash
        if (!serverUrl.endsWith("/")) {
            serverUrl = serverUrl + "/";
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Introduce tu correo");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Introduce tu contraseña");
            return;
        }
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Persist & apply the server URL before the network call
        session.saveServerUrl(serverUrl);
        com.schoolsafetrack.app.data.network.RetrofitClient.resetWithBaseUrl(serverUrl);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        viewModel.login(email, password);
    }

    private void observeViewModel() {
        viewModel.getLoginResult().observe(this, response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);

            if (response != null && response.isSuccess()) {
                session.saveSession(response.getUser());
                redirectByRole(response.getUser().getRole());
            } else if (response != null) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(response.getError() != null
                        ? response.getError()
                        : "Error al iniciar sesión");
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.tvError.setVisibility(View.VISIBLE);
            binding.tvError.setText(msg);
        });
    }

    private void redirectByRole(String role) {
        Intent intent;
        if ("DRIVER".equalsIgnoreCase(role)) {
            intent = new Intent(this, DriverMainActivity.class);
        } else if ("PARENT".equalsIgnoreCase(role)) {
            intent = new Intent(this, ParentMainActivity.class);
        } else {
            binding.tvError.setVisibility(View.VISIBLE);
            binding.tvError.setText("Rol no soportado en esta app: " + role);
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
