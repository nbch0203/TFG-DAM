package com.schoolsafetrack.app.fcm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.schoolsafetrack.app.data.model.ApiResponse;
import com.schoolsafetrack.app.data.network.RetrofitClient;
import com.schoolsafetrack.app.data.repository.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class FcmTokenSyncManager {

    private static final String TAG = "FcmTokenSyncManager";

    private FcmTokenSyncManager() {
        // Utility class
    }

    public static void syncCurrentToken(Context context) {
        if (context == null) return;
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> syncToken(context, token))
                .addOnFailureListener(e -> Log.w(TAG, "No se pudo obtener el token FCM", e));
    }

    public static void syncToken(Context context, String token) {
        if (context == null || token == null || token.trim().isEmpty()) return;

        Context appContext = context.getApplicationContext();
        SessionManager session = new SessionManager(appContext);
        if (!session.isLoggedIn() || session.getUserId() <= 0) {
            Log.d(TAG, "Sesión no iniciada; token FCM no enviado al backend");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userId", session.getUserId());
        body.put("email", session.getEmail());
        body.put("role", session.getRole());
        body.put("token", token);
        body.put("platform", "android");

        RetrofitClient.getInstance().getApiService().registerDeviceToken(body)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call,
                                           @NonNull Response<ApiResponse> response) {
                        if (!response.isSuccessful()) {
                            Log.w(TAG, "El backend rechazó el token FCM: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                        Log.w(TAG, "Error enviando token FCM al backend", t);
                    }
                });
    }
}


