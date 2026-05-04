package com.schoolsafetrack.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * Guarda una copia interna de la foto de perfil del usuario o del hijo.
 * Evita depender de permisos persistentes del proveedor de archivos.
 */
public class ProfilePhotoStore {

    private static final String PREF_NAME = "schoolsafetrack_profile_photos";
    private static final String KEY_USER_PREFIX = "user_photo_";
    private static final String KEY_CHILD_PREFIX = "child_photo_";
    private static final String PHOTO_DIR_NAME = "profile_photos";

    private final Context appContext;
    private final SharedPreferences prefs;

    public ProfilePhotoStore(Context context) {
        appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public File saveUserPhoto(long userId, Uri uri) {
        return saveUriToInternalFile(KEY_USER_PREFIX + userId, uri);
    }

    public File getUserPhoto(long userId) {
        return getPhotoFile(KEY_USER_PREFIX + userId);
    }

    public File saveChildPhoto(long childId, Uri uri) {
        return saveUriToInternalFile(KEY_CHILD_PREFIX + childId, uri);
    }

    public File getChildPhoto(long childId) {
        return getPhotoFile(KEY_CHILD_PREFIX + childId);
    }

    private File saveUriToInternalFile(String key, Uri uri) {
        if (uri == null) return null;

        File dir = getPhotoDir();
        if (!dir.exists() && !dir.mkdirs()) return null;

        File outFile = new File(dir, safeFileName(key) + ".jpg");
        try (InputStream input = appContext.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(outFile, false)) {
            if (input == null) return null;

            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            prefs.edit().putString(key, outFile.getAbsolutePath()).apply();
            return outFile;
        } catch (Exception ignored) {
            return null;
        }
    }

    private File getPhotoFile(String key) {
        String value = prefs.getString(key, null);
        if (value == null || value.trim().isEmpty()) return null;

        File file = new File(value);
        if (file.exists()) {
            return file;
        }

        // Legacy fallback: si antes se guardó una URI, intentar importarla una sola vez.
        try {
            Uri legacyUri = Uri.parse(value);
            if (legacyUri != null && legacyUri.getScheme() != null) {
                File imported = saveUriToInternalFile(key, legacyUri);
                if (imported != null && imported.exists()) {
                    return imported;
                }
            }
        } catch (Exception ignored) {
            // Ignorar y devolver null.
        }
        return null;
    }

    private File getPhotoDir() {
        return new File(appContext.getFilesDir(), PHOTO_DIR_NAME);
    }

    private String safeFileName(String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
    }
}
