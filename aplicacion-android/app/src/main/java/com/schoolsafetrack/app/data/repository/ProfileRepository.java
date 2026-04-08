package com.schoolsafetrack.app.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.schoolsafetrack.app.data.model.UpdateProfileResponse;
import com.schoolsafetrack.app.data.model.UserProfile;
import com.schoolsafetrack.app.data.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    public void loadProfile(long userId,
                            MutableLiveData<UserProfile> result,
                            MutableLiveData<String> error) {
        RetrofitClient.getInstance().getApiService()
                .getUserProfile(userId)
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.postValue(response.body());
                        } else {
                            error.postValue("Error al cargar el perfil (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable t) {
                        error.postValue("Sin conexión: " + t.getMessage());
                    }
                });
    }

    public void updateProfile(long userId,
                              Map<String, String> fields,
                              MutableLiveData<UserProfile> result,
                              MutableLiveData<String> error) {
        RetrofitClient.getInstance().getApiService()
                .updateProfile(userId, fields)
                .enqueue(new Callback<UpdateProfileResponse>() {
                    @Override
                    public void onResponse(Call<UpdateProfileResponse> call,
                                           Response<UpdateProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UpdateProfileResponse body = response.body();
                            if (body.isSuccess()) {
                                result.postValue(body.getUser());
                            } else {
                                error.postValue(body.getError() != null
                                        ? body.getError() : "Error al actualizar el perfil");
                            }
                        } else {
                            error.postValue("Error al actualizar (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                        error.postValue("Sin conexión: " + t.getMessage());
                    }
                });
    }
}
