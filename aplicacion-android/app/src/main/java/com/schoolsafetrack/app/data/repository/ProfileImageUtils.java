package com.schoolsafetrack.app.data.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;

/**
 * Utilidades para cargar una imagen desde un archivo interno en un ImageView.
 */
public final class ProfileImageUtils {

    private ProfileImageUtils() {}

    public static boolean loadIntoImageView(Context context, File file, ImageView imageView) {
        if (context == null || file == null || imageView == null || !file.exists()) return false;
        try (FileInputStream input = new FileInputStream(file)) {
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (bitmap == null) return false;
            imageView.setImageBitmap(bitmap);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
