package com.schoolsafetrack.app.ui.parent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.schoolsafetrack.app.data.model.Bus;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.repository.ParentRepository;

import java.util.List;

public class ParentViewModel extends ViewModel {

    private final ParentRepository repository = new ParentRepository();

    public LiveData<List<Child>> getChildren() { return repository.getChildren(); }
    public LiveData<List<Bus>> getBuses() { return repository.getBuses(); }
    public LiveData<String> getErrorMessage() { return repository.getErrorMessage(); }

    public void loadChildren(long parentId) {
        repository.loadChildren(parentId);
    }

    public void loadBuses(long parentId) {
        repository.loadBuses(parentId);
    }
}
