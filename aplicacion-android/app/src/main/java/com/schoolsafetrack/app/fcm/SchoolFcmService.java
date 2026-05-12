package com.schoolsafetrack.app.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.schoolsafetrack.app.MainActivity;
import com.schoolsafetrack.app.R;

import java.util.Map;

public class SchoolFcmService extends FirebaseMessagingService {

    private static final String TAG = "SchoolFcmService";
    private static final String CHANNEL_ID = "schoolsafetrack_push";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FcmTokenSyncManager.syncToken(this, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        String title = resolveTitle(message, data);
        String body = resolveBody(message, data);

        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        intent.putExtra("notification_type", data.get("type"));
        intent.putExtra("notification_child_id", data.get("childId"));
        intent.putExtra("notification_child_name", data.get("childName"));
        intent.putExtra("notification_route_assignment_id", data.get("routeAssignmentId"));
        intent.putExtra("notification_incident_id", data.get("incidentId"));
        intent.putExtra("notification_stop_name", data.get("stopName"));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        try {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                NotificationManagerCompat.from(this)
                        .notify((int) System.currentTimeMillis(), builder.build());
            } else {
                Log.d(TAG, "Las notificaciones están desactivadas en el dispositivo");
            }
        } catch (SecurityException e) {
            Log.w(TAG, "No se pudo mostrar la notificación FCM", e);
        }
    }

    private String resolveTitle(RemoteMessage message, Map<String, String> data) {
        if (message.getNotification() != null && message.getNotification().getTitle() != null) {
            return message.getNotification().getTitle();
        }

        String type = data != null ? data.get("type") : null;
        if (type == null) return getString(R.string.app_name);

        switch (type.toUpperCase()) {
            case "ARRIVAL":
            case "LLEGADA":
                return "Llegada a parada";
            case "DEPARTURE":
            case "SALIDA":
                return "Salida de parada";
            case "INCIDENT":
            case "INCIDENCE":
            case "INCIDENCIA":
                return "Incidencia en la ruta";
            default:
                return getString(R.string.app_name);
        }
    }

    private String resolveBody(RemoteMessage message, Map<String, String> data) {
        if (message.getNotification() != null && message.getNotification().getBody() != null) {
            return message.getNotification().getBody();
        }

        if (data == null) {
            return getString(R.string.app_name);
        }

        String childName = data.get("childName");
        String stopName = data.get("stopName");
        String incidentDescription = data.get("description");
        String type = data.get("type");

        if (type != null && childName != null && stopName != null) {
            if ("ARRIVAL".equalsIgnoreCase(type) || "LLEGADA".equalsIgnoreCase(type)) {
                return childName + " ha llegado a " + stopName + ".";
            }
            if ("DEPARTURE".equalsIgnoreCase(type) || "SALIDA".equalsIgnoreCase(type)) {
                return childName + " ha salido de " + stopName + ".";
            }
        }

        if (childName != null && incidentDescription != null) {
            return childName + ": " + incidentDescription;
        }

        if (incidentDescription != null) {
            return incidentDescription;
        }

        return getString(R.string.app_name);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.push_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(getString(R.string.push_channel_description));

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
