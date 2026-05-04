package com.schoolsafetrack.app.ui.parent;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Bus;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.repository.ProfileImageUtils;
import com.schoolsafetrack.app.data.repository.ProfilePhotoStore;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityParentMainBinding;
import com.schoolsafetrack.app.ui.login.LoginActivity;
import com.schoolsafetrack.app.ui.profile.ProfileActivity;
import com.schoolsafetrack.app.ui.profile.ProfileViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParentMainActivity extends AppCompatActivity implements ChildrenAdapter.OnChildClickListener {

    private static final int TAB_CHILDREN = 0;
    private static final int TAB_MAP = 1;
    private static final long POLL_INTERVAL_MS = 1000L;

    private ActivityParentMainBinding binding;
    private ParentViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private SessionManager session;
    private ProfilePhotoStore photoStore;
    private ChildrenAdapter childrenAdapter;
    private ImageView ivToolbarAvatarPhoto;
    private TextView tvToolbarAvatar;
    private TextView tvToolbarUserName;

    // Auto-refresh
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;
    private boolean pollingActive = false;
    private boolean hasCenteredOnBus = false;
    private boolean suppressSpinnerCallback = false;
    private boolean forceCenterOnNextUpdate = false;

    // Child-follow: child id that is currently selected (null = show all)
    private Long followedChildId = null;
    private List<Child> latestChildren = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        binding = ActivityParentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        photoStore = new ProfilePhotoStore(this);
        viewModel = new ViewModelProvider(this).get(ParentViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupMap();
        setupChildSpinner();
        observeViewModel();

        long parentId = session.getUserId();
        viewModel.loadChildren(parentId);
        viewModel.loadBuses(parentId);
        profileViewModel.loadProfile(parentId);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.parent_panel);
        }

        // Manual refresh button still works alongside auto-refresh
        binding.btnRefreshMap.setOnClickListener(v -> {
            // When user presses manual refresh, reload buses and if following a specific child
            // force the map to recenter on that bus when the new data arrives. If in "show all"
            // mode (followedChildId == null) do not change the current map center.
            if (followedChildId != null) {
                forceCenterOnNextUpdate = true;
                // reset hasCenteredOnBus so we guarantee recenter even if previously centered
                hasCenteredOnBus = false;
            }
            viewModel.loadBuses(session.getUserId());
        });
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_children));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_map));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == TAB_CHILDREN) {
                    binding.layoutChildren.setVisibility(View.VISIBLE);
                    binding.layoutMap.setVisibility(View.GONE);
                    binding.tvEmptyChildren.setVisibility(
                            childrenAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    stopPolling();
                } else {
                    binding.layoutChildren.setVisibility(View.GONE);
                    binding.layoutMap.setVisibility(View.VISIBLE);
                    binding.tvEmptyChildren.setVisibility(View.GONE);
                    // Al volver a la pestaña mapa, recéntrate una vez en la última ubicación recibida.
                    hasCenteredOnBus = false;
                    List<Bus> currentBuses = viewModel.getBuses().getValue();
                    if (currentBuses != null) updateMapWithBuses(currentBuses);
                    binding.mapView.invalidate();
                    startPolling();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        childrenAdapter = new ChildrenAdapter();
        childrenAdapter.setListener(this);
        binding.rvChildren.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChildren.setAdapter(childrenAdapter);
    }

    private void setupMap() {
        MapView map = binding.mapView;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        // Initial zoom
        map.getController().setZoom(12.0);
        // Prevent excessive zooming out (evita repetición de mosaicos al alejar mucho)
        // Ajusta estos valores según convenga: minZoom = lejania máxima (más pequeño -> más alejado)
        try {
            map.setMinZoomLevel(5.0);
            map.setMaxZoomLevel(20.0);
        } catch (NoSuchMethodError e) {
            // Algunas versiones antiguas de osmdroid no exponen setMin/MaxZoomLevel; en ese caso
            // no hacemos nada y confiamos en setZoom inicial.
        } catch (Throwable ignored) {
            // Otros errores inesperados: ignorar para no romper la app en dispositivos
        }
    }

     private void setupChildSpinner() {
         // Initial empty adapter; populated once children are loaded
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                 android.R.layout.simple_spinner_item, new ArrayList<>());
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         binding.spinnerFollowChild.setAdapter(adapter);

         binding.spinnerFollowChild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 if (suppressSpinnerCallback) return;
                 
                 if (position == 0) {
                     // "— Todos —" selected
                     followedChildId = null;
                 } else if (position > 0 && position - 1 < latestChildren.size()) {
                     Child selected = latestChildren.get(position - 1);
                     followedChildId = Long.valueOf(selected.getId());
                 } else {
                     followedChildId = null;
                 }
                 
                 hasCenteredOnBus = false;
                 // Re-render map immediately with current data
                 List<Bus> currentBuses = viewModel.getBuses().getValue();
                 if (currentBuses != null) updateMapWithBuses(currentBuses);
             }

             @Override
             public void onNothingSelected(AdapterView<?> parent) {
                 followedChildId = null;
             }
         });
     }

    /** Rebuild spinner entries whenever the children list changes. */
    private void refreshChildSpinner(List<Child> children) {
        latestChildren = children != null ? children : new ArrayList<>();

        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.follow_child_all));
        for (Child c : latestChildren) {
            labels.add(c.getFullName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int selectedIndex = 0;
        if (followedChildId != null) {
            for (int i = 0; i < latestChildren.size(); i++) {
                if (latestChildren.get(i).getId() == followedChildId) {
                    selectedIndex = i + 1;
                    break;
                }
            }
        }

        suppressSpinnerCallback = true;
        binding.spinnerFollowChild.setAdapter(adapter);
        binding.spinnerFollowChild.setSelection(selectedIndex, false);
        suppressSpinnerCallback = false;
    }

    // ── Auto-refresh ──────────────────────────────────────────────────────────

    private void startPolling() {
        if (pollingActive) return;
        pollingActive = true;
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!pollingActive) return;
                viewModel.loadBuses(session.getUserId());
                pollHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };
        pollHandler.post(pollRunnable);
    }

    private void stopPolling() {
        pollingActive = false;
        if (pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    // ── ViewModel observation ─────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getChildren().observe(this, children -> {
            childrenAdapter.setChildren(children);
            int count = children != null ? children.size() : 0;
            binding.tvChildrenCount.setText(count + (count == 1 ? " alumno" : " alumnos"));
            binding.tvEmptyChildren.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
            refreshChildSpinner(children);
        });

        viewModel.getBuses().observe(this, this::updateMapWithBuses);

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
            updateToolbarAvatarPhoto(profile.getId(), initial);
            if (tvToolbarUserName != null) tvToolbarUserName.setText(name);
            invalidateOptionsMenu();
        });
    }

    /** Callback del ChildrenAdapter — navega al detalle del hijo. */
    @Override
    public void onChildClick(Child child, int avatarColor) {
        Intent intent = new Intent(this, ChildDetailActivity.class);
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_ID, child.getId());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_NOMBRE, child.getNombre());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_APELLIDOS, child.getApellidos());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_CURSO, child.getCurso());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_FECHA_NAC, child.getFechaNacimiento());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_SCHOOL, child.getSchoolNombre());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_STOP_NOMBRE, child.getStopNombre());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_STOP_DIR, child.getStopDireccion());
        intent.putExtra(ChildDetailActivity.EXTRA_CHILD_AVATAR_COLOR, avatarColor);
        startActivity(intent);
    }

    private void updateMapWithBuses(List<Bus> buses) {
        MapView map = binding.mapView;
        map.getOverlays().clear();

        if (buses == null || buses.isEmpty()) {
            map.invalidate();
            return;
        }

        // Filter to followed bus if a child is selected
        Long followedBusId = getFollowedBusId();
        List<Bus> visibleBuses = new ArrayList<>();
        
        if (followedBusId == null) {
            // Show all buses
            visibleBuses.addAll(buses);
        } else {
            // Show only the bus of the followed child
            for (Bus bus : buses) {
                if (bus.getId() == followedBusId.longValue()) {
                    visibleBuses.add(bus);
                    break;
                }
            }
        }

        // Icono de bus personalizado
        Drawable busIcon = ContextCompat.getDrawable(this, R.drawable.ic_bus_marker);

        Marker firstMarker = null;
        Double minLat = null, maxLat = null, minLon = null, maxLon = null;
        for (Bus bus : visibleBuses) {
            if (bus.getLat() == null || bus.getLon() == null) continue;

            GeoPoint pos = new GeoPoint(bus.getLat(), bus.getLon());
            Marker marker = new Marker(map);
            marker.setPosition(pos);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marker.setTitle(bus.getMatricula());
            String snippet = (bus.getRouteNombre() != null ? bus.getRouteNombre() : "")
                    + (bus.getEstado() != null ? " · " + bus.getEstado() : "");
            marker.setSnippet(snippet);
            if (busIcon != null) {
                marker.setIcon(busIcon);
            }
            map.getOverlays().add(marker);
            if (firstMarker == null) firstMarker = marker;
            // Accumulate bounds
            if (minLat == null) {
                minLat = bus.getLat(); maxLat = bus.getLat(); minLon = bus.getLon(); maxLon = bus.getLon();
            } else {
                if (bus.getLat() < minLat) minLat = bus.getLat();
                if (bus.getLat() > maxLat) maxLat = bus.getLat();
                if (bus.getLon() < minLon) minLon = bus.getLon();
                if (bus.getLon() > maxLon) maxLon = bus.getLon();
            }
        }
        // Establecer límite de scroll para que el usuario no pueda desplazar el mapa
        // fuera de la zona donde hay buses (con un pequeño margen).
        try {
            if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
                double paddingLat = (maxLat - minLat) * 0.25 + 0.001; // 25% padding
                double paddingLon = (maxLon - minLon) * 0.25 + 0.001;
                double north = Math.min(90.0, maxLat + paddingLat);
                double south = Math.max(-90.0, minLat - paddingLat);
                double east = Math.min(180.0, maxLon + paddingLon);
                double west = Math.max(-180.0, minLon - paddingLon);
                BoundingBox limit = new BoundingBox(north, east, south, west);
                // Intentar usar el método disponible para establecer el límite de desplazamiento
                try {
                    map.setScrollableAreaLimitDouble(limit);
                } catch (NoSuchMethodError e) {
                    // Si no existe setScrollableAreaLimitDouble en esta versión de osmdroid,
                    // no intentamos otra variante para evitar errores de compilación.
                }
            }
        } catch (Throwable ignored) {
            // Ignorar si el método no está disponible o hay algún error inesperado
        }

        // Centrar automáticamente cuando se sigue a un hijo concreto. Si el usuario ha
        // forzado una recarga manual (forceCenterOnNextUpdate) o aún no se ha centrado,
        // entonces animamos la cámara hacia el bus seguido. No centramos si estamos en
        // el modo "— Todos —" (followedBusId == null).
        if (followedBusId != null && firstMarker != null && (!hasCenteredOnBus || forceCenterOnNextUpdate)) {
            map.getController().animateTo(firstMarker.getPosition());
            map.getController().setZoom(15.0);
            hasCenteredOnBus = true;
            forceCenterOnNextUpdate = false;
        }

        map.invalidate();
    }

    private Long getFollowedBusId() {
        if (followedChildId == null) return null;
        for (Child child : latestChildren) {
            if (child.getId() == followedChildId.longValue()) {
                Long busId = child.getBusId();
                return (busId != null && busId > 0) ? busId : null;
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        bindToolbarAvatar(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        bindToolbarAvatar(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void bindToolbarAvatar(Menu menu) {
        MenuItem profileItem = menu.findItem(R.id.action_profile);
        if (profileItem == null) return;

        View actionView = profileItem.getActionView();
        if (actionView == null) return;

        ivToolbarAvatarPhoto = actionView.findViewById(R.id.ivToolbarAvatarPhoto);
        tvToolbarAvatar = actionView.findViewById(R.id.tvToolbarAvatarInitial);
        tvToolbarUserName = actionView.findViewById(R.id.tvToolbarUserName);

        String email = session.getEmail();
        String fallbackInitial = !email.isEmpty() ? String.valueOf(email.charAt(0)).toUpperCase() : "?";
        if (tvToolbarAvatar != null) tvToolbarAvatar.setText(fallbackInitial);
        if (tvToolbarUserName != null && !email.isEmpty()) tvToolbarUserName.setText(email);
        updateToolbarAvatarPhoto(session.getUserId(), fallbackInitial);

        actionView.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateToolbarAvatarPhoto(long userId, String fallbackInitial) {
        if (ivToolbarAvatarPhoto == null) return;
        File photoFile = photoStore.getUserPhoto(userId);
        if (photoFile != null && ProfileImageUtils.loadIntoImageView(this, photoFile, ivToolbarAvatarPhoto)) {
            ivToolbarAvatarPhoto.setVisibility(View.VISIBLE);
            if (tvToolbarAvatar != null) tvToolbarAvatar.setVisibility(View.GONE);
        } else {
            ivToolbarAvatarPhoto.setImageDrawable(null);
            ivToolbarAvatarPhoto.setVisibility(View.GONE);
            if (tvToolbarAvatar != null) {
                tvToolbarAvatar.setVisibility(View.VISIBLE);
                tvToolbarAvatar.setText(fallbackInitial);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
        profileViewModel.loadProfile(session.getUserId());
        invalidateOptionsMenu();
        // Resume polling only if map tab is visible
        if (binding.layoutMap.getVisibility() == View.VISIBLE) {
            startPolling();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
        stopPolling();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }
}
