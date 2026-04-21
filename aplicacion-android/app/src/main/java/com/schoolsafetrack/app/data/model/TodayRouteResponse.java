package com.schoolsafetrack.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TodayRouteResponse {
    @SerializedName("route")
    private RouteInfo route;

    @SerializedName("stops")
    private List<Stop> stops;

    @SerializedName("checkins")
    private List<CheckIn> checkins;

    public RouteInfo getRoute() { return route; }
    public List<Stop> getStops() { return stops; }
    public List<CheckIn> getCheckins() { return checkins; }
}
