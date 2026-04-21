package com.schoolsafetrack.app.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // URL por defecto: 10.0.2.2 sólo funciona en el emulador Android.
    // En dispositivo físico hay que usar la IP real del PC en la red local.
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:3000/api/";

    private static String currentBaseUrl = DEFAULT_BASE_URL;
    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient(currentBaseUrl);
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    /** Cambia la URL base en runtime (p.ej. al guardar la IP del servidor). */
    public static synchronized void resetWithBaseUrl(String baseUrl) {
        currentBaseUrl = baseUrl;
        instance = null;
    }

    public static String getCurrentBaseUrl() {
        return currentBaseUrl;
    }
}
