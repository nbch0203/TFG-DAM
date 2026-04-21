package com.schoolsafetrack.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schoolsafetrack.app.data.model.ApiResponse;
import com.schoolsafetrack.app.data.model.TodayRouteResponse;
import com.schoolsafetrack.app.data.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRepository {

    private final MutableLiveData<TodayRouteResponse> todayRoute = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse> actionResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<TodayRouteResponse> getTodayRoute() { return todayRoute; }
    public LiveData<ApiResponse> getActionResult() { return actionResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadTodayRoute(long driverId) {
        RetrofitClient.getInstance().getApiService().getTodayRoute(driverId)
                .enqueue(new Callback<TodayRouteResponse>() {
                    @Override
                    public void onResponse(Call<TodayRouteResponse> call, Response<TodayRouteResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            todayRoute.postValue(response.body());
                        } else {
                            errorMessage.postValue("Error al cargar la ruta");
                        }
                    }

                    @Override
                    public void onFailure(Call<TodayRouteResponse> call, Throwable t) {
                        errorMessage.postValue("Error de red: " + t.getMessage());
                    }
                });
    }

    public void checkIn(long routeAssignmentId, long stopId, long driverId, String action, String notes) {
        Map<String, Object> body = new HashMap<>();
        body.put("routeAssignmentId", routeAssignmentId);
        body.put("stopId", stopId);
        body.put("driverId", driverId);
        body.put("action", action);
        if (notes != null && !notes.isEmpty()) {
            body.put("notes", notes);
        }

        RetrofitClient.getInstance().getApiService().postCheckIn(body)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        actionResult.postValue(
                                response.isSuccessful() && response.body() != null
                                        ? response.body()
                                        : buildError("Error al registrar check-in"));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        errorMessage.postValue("Error de red: " + t.getMessage());
                    }
                });
    }

    public void reportIncident(long routeAssignmentId, long driverId,
                               String tipo, String descripcion,
                               Double lat, Double lon) {
        Map<String, Object> body = new HashMap<>();
        body.put("routeAssignmentId", routeAssignmentId);
        body.put("driverId", driverId);
        body.put("tipo", tipo);
        body.put("descripcion", descripcion);
        if (lat != null) body.put("latitud", lat);
        if (lon != null) body.put("longitud", lon);

        RetrofitClient.getInstance().getApiService().postIncident(body)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        actionResult.postValue(
                                response.isSuccessful() && response.body() != null
                                        ? response.body()
                                        : buildError("Error al reportar incidencia"));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        errorMessage.postValue("Error de red: " + t.getMessage());
                    }
                });
    }

    public void finishRoute(long routeAssignmentId, long driverId, String summary) {
        Map<String, Object> body = new HashMap<>();
        body.put("routeAssignmentId", routeAssignmentId);
        body.put("driverId", driverId);
        if (summary != null && !summary.isEmpty()) {
            body.put("summary", summary);
        }

        RetrofitClient.getInstance().getApiService().finishRoute(body)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        actionResult.postValue(
                                response.isSuccessful() && response.body() != null
                                        ? response.body()
                                        : buildError("Error al finalizar ruta"));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        errorMessage.postValue("Error de red: " + t.getMessage());
                    }
                });
    }

    public void updateLocation(long busId, double lat, double lon) {
        Map<String, Double> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lon", lon);

        RetrofitClient.getInstance().getApiService().updateBusLocation(busId, location)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        // Actualización silenciosa de ubicación
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        // Log silencioso en background
                    }
                });
    }

    private ApiResponse buildError(String message) {
        // Crea una respuesta de error genérica cuando el servidor devuelve fallo
        return new ApiResponse();
    }
}
