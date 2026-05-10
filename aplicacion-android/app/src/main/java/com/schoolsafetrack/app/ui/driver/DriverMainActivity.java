package com.schoolsafetrack.app.ui.driver;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.ImageView;

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
import com.schoolsafetrack.app.data.repository.ProfilePhotoStore;
import com.schoolsafetrack.app.data.repository.ProfileImageUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DriverMainActivity extends AppCompatActivity implements StopsAdapter.StopActionListener {

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String NAV_PREFS = "driver_nav_prefs";
    private static final String NAV_DEFAULT_PACKAGE = "default_maps_package";

    private static class MapAppOption {
        final String packageName;
        final String label;

        MapAppOption(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }

    private ActivityDriverMainBinding binding;
    private DriverViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private SessionManager session;
    private StopsAdapter stopsAdapter;
    private TextView tvToolbarAvatar;
    private ImageView ivToolbarAvatarPhoto;
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

            // Try to load toolbar photo from internal store. If image is available show it,
            // otherwise keep the initial letter. If menu hasn't been created yet, ask to
            // recreate it so toolbar view is bound in onCreateOptionsMenu.
            try {
                ProfilePhotoStore store = new ProfilePhotoStore(this);
                java.io.File photo = store.getUserPhoto(profile.getId());
                if (photo != null && ivToolbarAvatarPhoto != null
                        && ProfileImageUtils.loadIntoImageView(this, photo, ivToolbarAvatarPhoto)) {
                    ivToolbarAvatarPhoto.setVisibility(View.VISIBLE);
                    if (tvToolbarAvatar != null) tvToolbarAvatar.setVisibility(View.GONE);
                } else {
                    if (ivToolbarAvatarPhoto != null) ivToolbarAvatarPhoto.setVisibility(View.GONE);
                    if (tvToolbarAvatar != null) tvToolbarAvatar.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {
                // ignore and keep initials
            }
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
        List<Intent> candidateIntents = buildMapIntents(destination);
        List<MapAppOption> mapApps = queryMapApps(candidateIntents);
        if (mapApps.isEmpty()) {
            Toast.makeText(this, R.string.maps_app_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        String defaultPackage = getNavPrefs().getString(NAV_DEFAULT_PACKAGE, null);
        if (!TextUtils.isEmpty(defaultPackage) && isPackageInOptions(defaultPackage, mapApps)) {
            if (!launchWithPackage(candidateIntents, defaultPackage)) {
                getNavPrefs().edit().remove(NAV_DEFAULT_PACKAGE).apply();
            }
            return;
        }

        showMapsAppPicker(candidateIntents, mapApps);
    }

    private Intent buildGeoIntent(String destination) {
        Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(destination));
        return new Intent(Intent.ACTION_VIEW, geoUri);
    }

    private Intent buildNavigationIntent(String destination) {
        Uri navUri = Uri.parse("google.navigation:q=" + Uri.encode(destination) + "&mode=d");
        return new Intent(Intent.ACTION_VIEW, navUri);
    }

    private Intent buildWebMapsIntent(String destination) {
        Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                + Uri.encode(destination) + "&travelmode=driving");
        return new Intent(Intent.ACTION_VIEW, webUri);
    }

    private List<Intent> buildMapIntents(String destination) {
        List<Intent> intents = new ArrayList<>();
        intents.add(buildNavigationIntent(destination));
        intents.add(buildGeoIntent(destination));
        intents.add(buildWebMapsIntent(destination));
        return intents;
    }

    private List<MapAppOption> queryMapApps(List<Intent> candidateIntents) {
        Map<String, String> uniqueApps = new LinkedHashMap<>();
        for (Intent intent : candidateIntents) {
            List<ResolveInfo> resolvers = getPackageManager().queryIntentActivities(intent, 0);
            if (resolvers == null) continue;
            for (ResolveInfo info : resolvers) {
                if (info == null || info.activityInfo == null) continue;
                String packageName = info.activityInfo.packageName;
                if (!uniqueApps.containsKey(packageName)) {
                    String label = String.valueOf(info.loadLabel(getPackageManager()));
                    uniqueApps.put(packageName, label);
                }
            }
        }

        List<MapAppOption> apps = new ArrayList<>();
        for (Map.Entry<String, String> entry : uniqueApps.entrySet()) {
            apps.add(new MapAppOption(entry.getKey(), entry.getValue()));
        }
        return apps;
    }

    private SharedPreferences getNavPrefs() {
        return getSharedPreferences(NAV_PREFS, MODE_PRIVATE);
    }

    private boolean isPackageInOptions(String packageName, List<MapAppOption> options) {
        for (MapAppOption option : options) {
            if (option != null && packageName.equals(option.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void showMapsAppPicker(List<Intent> candidateIntents, List<MapAppOption> mapApps) {
        String[] appNames = new String[mapApps.size()];
        for (int i = 0; i < mapApps.size(); i++) {
            appNames[i] = mapApps.get(i).label;
        }

        final int[] selectedIndex = {0};
        CheckBox alwaysDefaultCheckbox = new CheckBox(this);
        alwaysDefaultCheckbox.setText(R.string.maps_use_default_always);

        new AlertDialog.Builder(this)
                .setTitle(R.string.maps_choose_app_title)
                .setSingleChoiceItems(appNames, 0, (dialog, which) -> selectedIndex[0] = which)
                .setView(alwaysDefaultCheckbox)
                .setPositiveButton(R.string.open_maps, (dialog, which) -> {
                    String packageName = mapApps.get(selectedIndex[0]).packageName;

                    if (alwaysDefaultCheckbox.isChecked()) {
                        getNavPrefs().edit().putString(NAV_DEFAULT_PACKAGE, packageName).apply();
                    }

                    launchWithPackage(candidateIntents, packageName);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean launchWithPackage(List<Intent> candidateIntents, String packageName) {
        for (Intent baseIntent : candidateIntents) {
            Intent intent = new Intent(baseIntent);
            intent.setPackage(packageName);
            try {
                startActivity(intent);
                return true;
            } catch (ActivityNotFoundException ignored) {
                // Prueba siguiente intent soportado por el mismo paquete.
            }
        }
        Toast.makeText(this, R.string.maps_app_not_found, Toast.LENGTH_SHORT).show();
        return false;
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
                ivToolbarAvatarPhoto = actionView.findViewById(R.id.ivToolbarAvatarPhoto);
                tvToolbarUserName = actionView.findViewById(R.id.tvToolbarUserName);
                String email = session.getEmail();
                if (tvToolbarAvatar != null && !email.isEmpty()) {
                    tvToolbarAvatar.setText(String.valueOf(email.charAt(0)).toUpperCase());
                }
                if (tvToolbarUserName != null && !email.isEmpty()) {
                    tvToolbarUserName.setText(email);
                }

                // Attempt to load saved profile photo for the driver and show it in toolbar
                try {
                    ProfilePhotoStore store = new ProfilePhotoStore(this);
                    java.io.File photo = store.getUserPhoto(session.getUserId());
                    if (photo != null && ivToolbarAvatarPhoto != null
                            && ProfileImageUtils.loadIntoImageView(this, photo, ivToolbarAvatarPhoto)) {
                        ivToolbarAvatarPhoto.setVisibility(View.VISIBLE);
                        if (tvToolbarAvatar != null) tvToolbarAvatar.setVisibility(View.GONE);
                    } else {
                        if (ivToolbarAvatarPhoto != null) ivToolbarAvatarPhoto.setVisibility(View.GONE);
                        if (tvToolbarAvatar != null) tvToolbarAvatar.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ignored) {
                    // keep initials
                }

                actionView.setOnClickListener(v ->
                        startActivity(new Intent(this, ProfileActivity.class)));
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure toolbar action view is refreshed after returning from profile editor
        invalidateOptionsMenu();
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
