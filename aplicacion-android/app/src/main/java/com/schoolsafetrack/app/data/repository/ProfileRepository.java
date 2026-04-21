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
                        } else if (response.code() == 404) {
                            callback.onError("No se encontró tu perfil. Por favor, cierra sesión y vuelve a entrar.");
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
                            // Intentar leer el mensaje de error del servidor
                            String errorMsg = "Error al actualizar (" + response.code() + ")";
                            try {
                                if (response.errorBody() != null) {
                                    String rawError = response.errorBody().string();
                                    // Extraer el campo "error" del JSON simple {"error":"..."}
                                    int start = rawError.indexOf("\"error\":\"");
                                    if (start >= 0) {
                                        start += 9;
                                        int end = rawError.indexOf("\"", start);
                                        if (end > start) errorMsg = rawError.substring(start, end);
                                    }
                                }
                            } catch (Exception ignored) { }
                            callback.onError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                        callback.onError("Sin conexión: " + t.getMessage());
                    }
                });
    }
}
