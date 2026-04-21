package com.schoolsafetrack.app.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.schoolsafetrack.app.data.model.LoginResponse;
import com.schoolsafetrack.app.data.repository.AuthRepository;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    public LiveData<LoginResponse> getLoginResult() {
        return repository.getLoginResult();
    }

    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }

    public void login(String email, String password) {
        repository.login(email, password);
    }
}
