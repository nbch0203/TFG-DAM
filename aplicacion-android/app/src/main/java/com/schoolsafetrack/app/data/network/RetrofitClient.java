package com.schoolsafetrack.app.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // URL base del backend. Cambiar por la IP real en producción.
    // En emulador Android, 10.0.2.2 apunta al localhost del ordenador host.
    private static final String BASE_URL = "http://10.0.2.2:3000/api/";

    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    /** Permite cambiar la URL base en runtime (p.ej. desde ajustes). */
    public static void resetWithBaseUrl(String baseUrl) {
        instance = null;
    }
}
