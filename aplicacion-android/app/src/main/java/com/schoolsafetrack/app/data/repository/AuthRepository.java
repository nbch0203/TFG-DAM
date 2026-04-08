package com.schoolsafetrack.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schoolsafetrack.app.data.model.LoginResponse;
import com.schoolsafetrack.app.data.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<LoginResponse> getLoginResult() { return loginResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void login(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        RetrofitClient.getInstance().getApiService().login(body)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            loginResult.postValue(response.body());
                        } else {
                            errorMessage.postValue("Credenciales incorrectas");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        errorMessage.postValue("No se pudo conectar con el servidor: " + t.getMessage());
                    }
                });
    }
}
