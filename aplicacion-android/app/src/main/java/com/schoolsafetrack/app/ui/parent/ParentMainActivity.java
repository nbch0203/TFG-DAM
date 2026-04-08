package com.schoolsafetrack.app.ui.parent;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.util.List;

public class ParentMainActivity extends AppCompatActivity implements ChildrenAdapter.OnChildClickListener {

    private static final int TAB_CHILDREN = 0;
    private static final int TAB_MAP = 1;

    private ActivityParentMainBinding binding;
    private ParentViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private SessionManager session;
    private ChildrenAdapter childrenAdapter;
    private TextView tvToolbarAvatar;
    private TextView tvToolbarUserName;

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

        // Botón refrescar mapa
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
                } else {
                    binding.layoutChildren.setVisibility(View.GONE);
                    binding.layoutMap.setVisibility(View.VISIBLE);
                    binding.tvEmptyChildren.setVisibility(View.GONE);
                    binding.mapView.invalidate();
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
        map.getController().setZoom(14.0);
        map.getController().setCenter(new GeoPoint(40.416775, -3.703790));
    }

    private void observeViewModel() {
        viewModel.getChildren().observe(this, children -> {
            childrenAdapter.setChildren(children);
            int count = children != null ? children.size() : 0;
            binding.tvChildrenCount.setText(count + (count == 1 ? " alumno" : " alumnos"));
            binding.tvEmptyChildren.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        });

        viewModel.getBuses().observe(this, buses -> updateMapWithBuses(buses));

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

        // Icono de bus personalizado
        Drawable busIcon = ContextCompat.getDrawable(this, R.drawable.ic_bus_marker);

        for (Bus bus : buses) {
            if (bus.getLat() == null || bus.getLon() == null) continue;

            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(bus.getLat(), bus.getLon()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marker.setTitle(bus.getMatricula());
            String snippet = (bus.getRouteNombre() != null ? bus.getRouteNombre() : "")
                    + (bus.getEstado() != null ? " · " + bus.getEstado() : "");
            marker.setSnippet(snippet);
            if (busIcon != null) {
                marker.setIcon(busIcon);
            }
            map.getOverlays().add(marker);
        }

        // Centrar en el primer bus con ubicación
        buses.stream()
                .filter(b -> b.getLat() != null && b.getLon() != null)
                .findFirst()
                .ifPresent(b -> {
                    map.getController().animateTo(new GeoPoint(b.getLat(), b.getLon()));
                    map.getController().setZoom(15.0);
                });

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }
}
