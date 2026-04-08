package com.schoolsafetrack.app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.schoolsafetrack.app.data.model.UserProfile;
import com.schoolsafetrack.app.data.repository.ProfileRepository;

import java.util.HashMap;
import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository repository = new ProfileRepository();

    private final MutableLiveData<UserProfile> profile = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public LiveData<UserProfile> getProfile() { return profile; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }

    public void loadProfile(long userId) {
        loading.setValue(true);
        repository.loadProfile(userId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                profile.postValue(p);
                loading.postValue(false);
            }

            @Override
            public void onError(String msg) {
                errorMessage.postValue(msg);
                loading.postValue(false);
            }
        });
    }

    public void saveProfile(long userId, String nombre, String apellidos, String email) {
        loading.setValue(true);
        Map<String, String> fields = new HashMap<>();
        if (nombre != null)    fields.put("nombre",    nombre);
        if (apellidos != null) fields.put("apellidos", apellidos);
        if (email != null)     fields.put("email",     email);

        repository.updateProfile(userId, fields, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                profile.postValue(p);
                loading.postValue(false);
                saveSuccess.postValue(true);
            }

            @Override
            public void onError(String msg) {
                errorMessage.postValue(msg);
                loading.postValue(false);
            }
        });
    }

    public void changePassword(long userId, String newPassword) {
        loading.setValue(true);
        Map<String, String> fields = new HashMap<>();
        fields.put("password", newPassword);

        repository.updateProfile(userId, fields, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile p) {
                loading.postValue(false);
                saveSuccess.postValue(true);
            }

            @Override
            public void onError(String msg) {
                errorMessage.postValue(msg);
                loading.postValue(false);
            }
        });
    }
}
