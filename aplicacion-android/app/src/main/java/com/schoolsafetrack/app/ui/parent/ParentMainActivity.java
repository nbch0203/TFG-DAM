package com.schoolsafetrack.app.ui.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.model.Bus;
import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.databinding.ActivityParentMainBinding;
import com.schoolsafetrack.app.ui.login.LoginActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class ParentMainActivity extends AppCompatActivity {

    private static final int TAB_CHILDREN = 0;
    private static final int TAB_MAP = 1;

    private ActivityParentMainBinding binding;
    private ParentViewModel viewModel;
    private SessionManager session;
    private ChildrenAdapter childrenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar osmdroid con el contexto de la app
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        binding = ActivityParentMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(ParentViewModel.class);

        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupMap();
        observeViewModel();

        long parentId = session.getUserId();
        viewModel.loadChildren(parentId);
        viewModel.loadBuses(parentId);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.parent_panel);
        }
        binding.toolbar.inflateMenu(R.menu.menu_main);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_children));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_map));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == TAB_CHILDREN) {
                    binding.rvChildren.setVisibility(View.VISIBLE);
                    binding.mapView.setVisibility(View.GONE);
                } else {
                    binding.rvChildren.setVisibility(View.GONE);
                    binding.mapView.setVisibility(View.VISIBLE);
                    binding.mapView.invalidate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        childrenAdapter = new ChildrenAdapter();
        binding.rvChildren.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChildren.setAdapter(childrenAdapter);
    }

    private void setupMap() {
        MapView map = binding.mapView;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.0);
        // Posición inicial: Madrid
        map.getController().setCenter(new GeoPoint(40.416775, -3.703790));
    }

    private void observeViewModel() {
        viewModel.getChildren().observe(this, children -> {
            childrenAdapter.setChildren(children);
            binding.tvEmptyChildren.setVisibility(
                    children == null || children.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getBuses().observe(this, buses -> {
            updateMapWithBuses(buses);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateMapWithBuses(List<Bus> buses) {
        MapView map = binding.mapView;
        map.getOverlays().clear();

        if (buses == null || buses.isEmpty()) return;

        for (Bus bus : buses) {
            if (bus.getLat() == null || bus.getLon() == null) continue;

            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(bus.getLat(), bus.getLon()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(bus.getMatricula());
            String snippet = (bus.getRouteNombre() != null ? bus.getRouteNombre() : "")
                    + (bus.getEstado() != null ? " · " + bus.getEstado() : "");
            marker.setSnippet(snippet);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }
}
