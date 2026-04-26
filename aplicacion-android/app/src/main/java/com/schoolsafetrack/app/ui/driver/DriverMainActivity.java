package com.schoolsafetrack.app.ui.driver;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.CheckIn;
import com.schoolsafetrack.app.data.model.RouteInfo;
import com.schoolsafetrack.app.data.model.Stop;
import com.schoolsafetrack.app.data.model.TodayRouteResponse;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityDriverMainBinding;
import com.schoolsafetrack.app.databinding.DialogIncidentBinding;
import com.schoolsafetrack.app.service.GpsTrackingService;
import com.schoolsafetrack.app.ui.login.LoginActivity;

import com.schoolsafetrack.app.ui.profile.ProfileActivity;
import com.schoolsafetrack.app.ui.profile.ProfileViewModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DriverMainActivity extends AppCompatActivity implements StopsAdapter.StopActionListener {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private ActivityDriverMainBinding binding;
    private DriverViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private SessionManager session;
    private StopsAdapter stopsAdapter;
    private TextView tvToolbarAvatar;
    private TextView tvToolbarUserName;

    private RouteInfo currentRoute;
    private long busId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(DriverViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeViewModel();
        requestLocationPermission();

        long driverId = session.getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadTodayRoute(driverId);
        profileViewModel.loadProfile(driverId);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.driver_panel);
        }

        binding.btnFinishRoute.setOnClickListener(v -> confirmFinishRoute());
        binding.btnReportIncident.setOnClickListener(v -> showIncidentDialog());
    }

    private void setupRecyclerView() {
        stopsAdapter = new StopsAdapter();
        stopsAdapter.setListener(this);
        binding.rvStops.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStops.setAdapter(stopsAdapter);
    }

    private void observeViewModel() {
        viewModel.getTodayRoute().observe(this, response -> {
            binding.progressBar.setVisibility(View.GONE);
            if (response == null) return;

            currentRoute = response.getRoute();
            if (currentRoute == null) {
                binding.scrollContent.setVisibility(View.GONE);
                binding.tvNoRoute.setVisibility(View.VISIBLE);
                binding.btnFinishRoute.setEnabled(false);
                binding.btnReportIncident.setEnabled(false);
                return;
            }

            binding.scrollContent.setVisibility(View.VISIBLE);
            binding.tvNoRoute.setVisibility(View.GONE);
            binding.tvRouteName.setText(currentRoute.getRouteNombre());
            binding.tvRouteStatus.setText(currentRoute.getEstado());
            binding.tvBusInfo.setText(
                    currentRoute.getMatricula() + " · " + currentRoute.getMarca()
                            + " " + currentRoute.getModelo());
            binding.tvHorario.setText(
                    currentRoute.getHorarioInicio() + " – " + currentRoute.getHorarioFin());

            busId = currentRoute.getBusId();

            boolean routeActive = "EN_CURSO".equals(currentRoute.getEstado())
                    || "PROGRAMADA".equals(currentRoute.getEstado());
            binding.btnFinishRoute.setEnabled(routeActive);
            binding.btnReportIncident.setEnabled(routeActive);

            Set<Long> completedIds = buildCompletedStopIds(response.getCheckins());
            stopsAdapter.setStops(response.getStops(), completedIds);

            if (routeActive && busId > 0) {
                startGpsService(busId);
            }
        });

        viewModel.getActionResult().observe(this, result -> {
            if (result != null) {
                String msg = result.isSuccess()
                        ? getString(R.string.action_success)
                        : getString(R.string.action_error);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                // Recargar ruta para reflejar cambios
                viewModel.loadTodayRoute(session.getUserId());
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            binding.progressBar.setVisibility(View.GONE);
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        profileViewModel.getProfile().observe(this, profile -> {
            if (profile == null) return;
            String initial;
            String name;
            if (profile.getNombre() != null && !profile.getNombre().isEmpty()) {
                initial = String.valueOf(profile.getNombre().charAt(0)).toUpperCase();
                name = profile.getNombre();
                if (profile.getApellidos() != null && !profile.getApellidos().isEmpty()) {
                    name = name + " " + profile.getApellidos();
                }
            } else if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
                initial = String.valueOf(profile.getEmail().charAt(0)).toUpperCase();
                name = profile.getEmail();
            } else {
                return;
            }
            if (tvToolbarAvatar != null) tvToolbarAvatar.setText(initial);
            if (tvToolbarUserName != null) tvToolbarUserName.setText(name);
        });
    }

    private Set<Long> buildCompletedStopIds(List<CheckIn> checkins) {
        Set<Long> ids = new HashSet<>();
        if (checkins != null) {
            for (CheckIn c : checkins) {
                if ("DEPARTURE".equals(c.getAction())) {
                    ids.add(c.getStopId());
                }
            }
        }
        return ids;
    }

    @Override
    public void onArrival(Stop stop) {
        if (currentRoute == null) return;
        viewModel.checkIn(currentRoute.getAssignmentId(), stop.getId(),
                session.getUserId(), "ARRIVAL", null);
    }

    @Override
    public void onDeparture(Stop stop) {
        if (currentRoute == null) return;
        viewModel.checkIn(currentRoute.getAssignmentId(), stop.getId(),
                session.getUserId(), "DEPARTURE", null);
    }

    @Override
    public void onStopClick(Stop stop) {
        openNavigationToStop(stop);
    }

    private void openNavigationToStop(Stop stop) {
        if (stop == null || TextUtils.isEmpty(stop.getDireccion())) {
            Toast.makeText(this, R.string.stop_address_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        String destination = stop.getDireccion().trim();
        Uri navUri = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=d");
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, navUri);
        mapsIntent.setPackage("com.google.android.apps.maps");

        if (mapsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapsIntent);
            return;
        }

        Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                + Uri.encode(destination) + "&travelmode=driving");
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
        if (webIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(webIntent);
        } else {
            Toast.makeText(this, R.string.maps_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void showIncidentDialog() {
        if (currentRoute == null) return;

        DialogIncidentBinding dialogBinding = DialogIncidentBinding.inflate(getLayoutInflater());
        String[] tipos = {"RETRASO", "MECANICO", "ACCIDENTE", "CLIMA", "OTRO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerTipo.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.report_incident)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.send, (dialog, which) -> {
                    String tipo = tipos[dialogBinding.spinnerTipo.getSelectedItemPosition()];
                    String desc = dialogBinding.etDescription.getText().toString().trim();
                    if (desc.isEmpty()) {
                        Toast.makeText(this, R.string.description_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.reportIncident(
                            currentRoute.getAssignmentId(),
                            session.getUserId(),
                            tipo, desc, null, null);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmFinishRoute() {
        if (currentRoute == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.finish_route)
                .setMessage(R.string.finish_route_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    stopGpsService();
                    viewModel.finishRoute(currentRoute.getAssignmentId(),
                            session.getUserId(), null);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startGpsService(long busId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, GpsTrackingService.class);
            serviceIntent.putExtra(GpsTrackingService.EXTRA_BUS_ID, busId);
            serviceIntent.putExtra(GpsTrackingService.EXTRA_DRIVER_ID, session.getUserId());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private void stopGpsService() {
        stopService(new Intent(this, GpsTrackingService.class));
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (busId > 0) startGpsService(busId);
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem profileItem = menu.findItem(R.id.action_profile);
        if (profileItem != null) {
            View actionView = profileItem.getActionView();
            if (actionView != null) {
                tvToolbarAvatar = actionView.findViewById(R.id.tvToolbarAvatarInitial);
                tvToolbarUserName = actionView.findViewById(R.id.tvToolbarUserName);
                String email = session.getEmail();
                if (tvToolbarAvatar != null && !email.isEmpty()) {
                    tvToolbarAvatar.setText(String.valueOf(email.charAt(0)).toUpperCase());
                }
                if (tvToolbarUserName != null && !email.isEmpty()) {
                    tvToolbarUserName.setText(email);
                }
                actionView.setOnClickListener(v ->
                        startActivity(new Intent(this, ProfileActivity.class)));
            }
        }
        return true;
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
