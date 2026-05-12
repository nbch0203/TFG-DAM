package com.schoolsafetrack.app.data.network;

import com.schoolsafetrack.app.data.model.ApiResponse;
import com.schoolsafetrack.app.data.model.Bus;
import com.schoolsafetrack.app.data.model.Child;
import com.schoolsafetrack.app.data.model.Incident;
import com.schoolsafetrack.app.data.model.LoginResponse;
import com.schoolsafetrack.app.data.model.TodayRouteResponse;
import com.schoolsafetrack.app.data.model.UpdateProfileResponse;
import com.schoolsafetrack.app.data.model.UserProfile;

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

    // ── Perfil de usuario ─────────────────────────────────────────────────────
    @GET("users/{id}")
    Call<UserProfile> getUserProfile(@Path("id") long id);

    @PATCH("users/{id}/profile")
    Call<UpdateProfileResponse> updateProfile(
            @Path("id") long id,
            @Body Map<String, String> fields);

    @POST("users/device-token")
    Call<ApiResponse> registerDeviceToken(@Body Map<String, Object> body);

    // ── Padre ─────────────────────────────────────────────────────────────────
    @GET("parent/{parentId}/children")
    Call<List<Child>> getChildren(@Path("parentId") long parentId);

    @GET("parent/{parentId}/buses")
    Call<List<Bus>> getParentBuses(@Path("parentId") long parentId);

    @GET("parent/{parentId}/children/{childId}/incidents")
    Call<List<Incident>> getChildIncidents(
            @Path("parentId") long parentId,
            @Path("childId") long childId);

    // ── Alumno ────────────────────────────────────────────────────────────────
    @GET("students/{childId}")
    Call<Child> getStudentDetail(@Path("childId") long childId);

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
