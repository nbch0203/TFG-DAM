package com.schoolsafetrack.app.data.network;

import com.schoolsafetrack.app.data.model.ApiResponse;
import com.schoolsafetrack.app.data.model.Bus;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.model.LoginResponse;
import com.schoolsafetrack.app.data.model.TodayRouteResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("login")
    Call<LoginResponse> login(@Body Map<String, String> credentials);

    // ── Padre ─────────────────────────────────────────────────────────────────
    @GET("parent/{parentId}/children")
    Call<List<Child>> getChildren(@Path("parentId") long parentId);

    @GET("parent/{parentId}/buses")
    Call<List<Bus>> getParentBuses(@Path("parentId") long parentId);

    // ── Conductor ─────────────────────────────────────────────────────────────
    @GET("driver/{driverId}/today-route")
    Call<TodayRouteResponse> getTodayRoute(@Path("driverId") long driverId);

    @POST("driver/checkins")
    Call<ApiResponse> postCheckIn(@Body Map<String, Object> body);

    @POST("driver/incidents")
    Call<ApiResponse> postIncident(@Body Map<String, Object> body);

    @POST("driver/finish-route")
    Call<ApiResponse> finishRoute(@Body Map<String, Object> body);

    @PATCH("buses/{busId}/location")
    Call<ApiResponse> updateBusLocation(
            @Path("busId") long busId,
            @Body Map<String, Double> location);
}
