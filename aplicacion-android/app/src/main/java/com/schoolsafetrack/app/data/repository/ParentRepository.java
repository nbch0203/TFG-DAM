package com.schoolsafetrack.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schoolsafetrack.app.data.model.Bus;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.model.Incident;
import com.schoolsafetrack.app.data.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentRepository {

    private final MutableLiveData<List<Child>> children = new MutableLiveData<>();
    private final MutableLiveData<List<Bus>> buses = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Child>> getChildren() { return children; }
    public LiveData<List<Bus>> getBuses() { return buses; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadChildren(long parentId) {
        RetrofitClient.getInstance().getApiService().getChildren(parentId)
                .enqueue(new Callback<List<Child>>() {
                    @Override
                    public void onResponse(Call<List<Child>> call, Response<List<Child>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            children.postValue(response.body());
                        } else {
                            errorMessage.postValue("Error al cargar hijos");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Child>> call, Throwable t) {
                        errorMessage.postValue("Error de red: " + t.getMessage());
                    }
                });
    }

    public void loadBuses(long parentId) {
        RetrofitClient.getInstance().getApiService().getParentBuses(parentId)
                .enqueue(new Callback<List<Bus>>() {
                    @Override
                    public void onResponse(Call<List<Bus>> call, Response<List<Bus>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            buses.postValue(response.body());
                        } else {
                            errorMessage.postValue("Error al cargar buses");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Bus>> call, Throwable t) {
                        errorMessage.postValue("Error de red: " + t.getMessage());
                    }
                });
    }

    /** Carga las incidencias de la ruta de un hijo concreto. */
    public void loadChildIncidents(long parentId, long childId,
                                   MutableLiveData<List<Incident>> result,
                                   MutableLiveData<String> error) {
        RetrofitClient.getInstance().getApiService()
                .getChildIncidents(parentId, childId)
                .enqueue(new Callback<List<Incident>>() {
                    @Override
                    public void onResponse(Call<List<Incident>> call, Response<List<Incident>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.postValue(response.body());
                        } else {
                            error.postValue("Error al cargar incidencias");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Incident>> call, Throwable t) {
                        error.postValue("Error de red: " + t.getMessage());
                    }
                });
    }
}
