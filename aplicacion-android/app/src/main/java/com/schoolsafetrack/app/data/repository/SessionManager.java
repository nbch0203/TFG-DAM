package com.schoolsafetrack.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.schoolsafetrack.app.data.model.User;

/**
 * Gestiona la sesión del usuario usando SharedPreferences.
 * Almacena id, email y rol del usuario autenticado.
 */
public class SessionManager {

    private static final String PREF_NAME = "schoolsafetrack_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_SERVER_URL = "server_url";

    private static final String DEFAULT_SERVER_URL = "http://10.0.2.2:3000/api/";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(User user) {
        prefs.edit()
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_ROLE, user.getRole())
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_EMAIL)
                .remove(KEY_ROLE)
                .putBoolean(KEY_LOGGED_IN, false)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public String getServerUrl() {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL);
    }

    public void saveServerUrl(String url) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply();
    }
}
