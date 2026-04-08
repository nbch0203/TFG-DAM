package com.schoolsafetrack.app.ui.parent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.schoolsafetrack.app.data.model.Incident;
import com.schoolsafetrack.app.data.repository.ParentRepository;

import java.util.List;

public class ChildDetailViewModel extends ViewModel {

    private final ParentRepository repository = new ParentRepository();

    private final MutableLiveData<List<Incident>> incidents = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<Incident>> getIncidents() { return incidents; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void loadIncidents(long parentId, long childId) {
        loading.setValue(true);
        repository.loadChildIncidents(parentId, childId, incidents, errorMessage);
        // Ocultar loader cuando llega respuesta
        incidents.observeForever(i -> loading.setValue(false));
        errorMessage.observeForever(e -> loading.setValue(false));
    }
}
