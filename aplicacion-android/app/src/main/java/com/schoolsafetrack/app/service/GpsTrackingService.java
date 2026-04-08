package com.schoolsafetrack.app.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.schoolsafetrack.app.R;
import com.schoolsafetrack.app.data.repository.DriverRepository;

/**
 * Servicio en primer plano que envía la ubicación GPS del conductor al backend
 * cada {@link #INTERVAL_MS} milisegundos mientras la ruta está activa.
 *
 * NOTA: requiere la dependencia play-services-location en build.gradle.kts.
 * Si no se dispone de Google Play Services, sustituir FusedLocationProviderClient
 * por LocationManager del SDK de Android.
 */
public class GpsTrackingService extends Service {

    public static final String EXTRA_BUS_ID = "extra_bus_id";
    public static final String EXTRA_DRIVER_ID = "extra_driver_id";

    private static final String CHANNEL_ID = "gps_tracking_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long INTERVAL_MS = 15_000;    // 15 segundos
    private static final long FASTEST_INTERVAL_MS = 5_000;

    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private DriverRepository driverRepository;

    private long busId = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        driverRepository = new DriverRepository();
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            busId = intent.getLongExtra(EXTRA_BUS_ID, -1);
        }

        startForeground(NOTIFICATION_ID, buildNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || busId < 0) return;
                Location last = result.getLastLocation();
                if (last != null) {
                    driverRepository.updateLocation(busId, last.getLatitude(), last.getLongitude());
                }
            }
        };

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.gps_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.gps_channel_description));
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.gps_notification_title))
                .setContentText(getString(R.string.gps_notification_text))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();
    }
}
