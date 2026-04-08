package com.schoolsafetrack.app.data.repository;

import com.schoolsafetrack.app.data.model.UpdateProfileResponse;
import com.schoolsafetrack.app.data.model.UserProfile;
import com.schoolsafetrack.app.data.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
    }

    public void loadProfile(long userId, ProfileCallback callback) {
        RetrofitClient.getInstance().getApiService()
                .getUserProfile(userId)
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Error al cargar el perfil (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable t) {
                        callback.onError("Sin conexión: " + t.getMessage());
                    }
                });
    }

    public void updateProfile(long userId,
                              Map<String, String> fields,
                              ProfileCallback callback) {
        RetrofitClient.getInstance().getApiService()
                .updateProfile(userId, fields)
                .enqueue(new Callback<UpdateProfileResponse>() {
                    @Override
                    public void onResponse(Call<UpdateProfileResponse> call,
                                           Response<UpdateProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UpdateProfileResponse body = response.body();
                            if (body.isSuccess()) {
                                callback.onSuccess(body.getUser());
                            } else {
                                callback.onError(body.getError() != null
                                        ? body.getError() : "Error al actualizar el perfil");
                            }
                        } else {
                            callback.onError("Error al actualizar (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                        callback.onError("Sin conexión: " + t.getMessage());
                    }
                });
    }
}
