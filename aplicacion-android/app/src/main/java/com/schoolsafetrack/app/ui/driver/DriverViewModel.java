package com.schoolsafetrack.app.ui.driver;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.schoolsafetrack.app.data.model.ApiResponse;
import com.schoolsafetrack.app.data.model.TodayRouteResponse;
import com.schoolsafetrack.app.data.repository.DriverRepository;

public class DriverViewModel extends ViewModel {

    private final DriverRepository repository = new DriverRepository();

    public LiveData<TodayRouteResponse> getTodayRoute() { return repository.getTodayRoute(); }
    public LiveData<ApiResponse> getActionResult() { return repository.getActionResult(); }
    public LiveData<String> getErrorMessage() { return repository.getErrorMessage(); }

    public void loadTodayRoute(long driverId) {
        repository.loadTodayRoute(driverId);
    }

    public void checkIn(long routeAssignmentId, long stopId, long driverId,
                        String action, String notes) {
        repository.checkIn(routeAssignmentId, stopId, driverId, action, notes);
    }

    public void reportIncident(long routeAssignmentId, long driverId,
                               String tipo, String descripcion,
                               Double lat, Double lon) {
        repository.reportIncident(routeAssignmentId, driverId, tipo, descripcion, lat, lon);
    }

    public void finishRoute(long routeAssignmentId, long driverId, String summary) {
        repository.finishRoute(routeAssignmentId, driverId, summary);
    }

    public void updateLocation(long busId, double lat, double lon) {
        repository.updateLocation(busId, lat, lon);
    }
}
