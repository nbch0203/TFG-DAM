package com.schoolsafetrack.app.ui.parent;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityParentMainBinding;
import com.schoolsafetrack.app.ui.login.LoginActivity;
import com.schoolsafetrack.app.ui.profile.ProfileActivity;
import com.schoolsafetrack.app.ui.profile.ProfileViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

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
    private ChildrenAdapter childrenAdapter;
    private TextView tvToolbarAvatar;
    private TextView tvToolbarUserName;

    // Auto-refresh
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;
    private boolean pollingActive = false;
    private boolean hasCenteredOnBus = false;

    // Child-follow: child id that is currently selected (null = show all)
    private Long followedBusId = null;
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
        binding.btnRefreshMap.setOnClickListener(v ->
                viewModel.loadBuses(session.getUserId()));
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
        map.getController().setZoom(12.0);
    }

    /** Build the "Seguir hijo" spinner with the latest children list. */
    private void setupChildSpinner() {
        // Initial empty adapter; populated once children are loaded
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFollowChild.setAdapter(adapter);

        binding.spinnerFollowChild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "— Todos —" selected
                    followedBusId = null;
                } else {
                    Child selected = latestChildren.get(position - 1);
                    followedBusId = selected.getBusId();
                }
                hasCenteredOnBus = false;
                // Re-render map immediately with current data
                List<Bus> currentBuses = viewModel.getBuses().getValue();
                if (currentBuses != null) updateMapWithBuses(currentBuses);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                followedBusId = null;
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
        binding.spinnerFollowChild.setAdapter(adapter);
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
                viewModel.loadChildren(session.getUserId());
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
            if (tvToolbarUserName != null) tvToolbarUserName.setText(name);
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
        List<Bus> visibleBuses = new ArrayList<>();
        for (Bus bus : buses) {
            if (followedBusId == null || followedBusId == bus.getId()) {
                visibleBuses.add(bus);
            }
        }

        // Icono de bus personalizado
        Drawable busIcon = ContextCompat.getDrawable(this, R.drawable.ic_bus_marker);

        Marker firstMarker = null;
        for (Bus bus : visibleBuses) {
            if (bus.getLat() == null || bus.getLon() == null) continue;

            GeoPoint pos = new GeoPoint(bus.getLat(), bus.getLon());
            // Reuse existing marker if present, otherwise create new one
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
        }

        // If a child is followed, center on that bus on first load (don't re-center on every poll)
        if (firstMarker != null && !hasCenteredOnBus) {
            map.getController().animateTo(firstMarker.getPosition());
            map.getController().setZoom(15.0);
            hasCenteredOnBus = true;
        }

        map.invalidate();
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

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
        profileViewModel.loadProfile(session.getUserId());
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
