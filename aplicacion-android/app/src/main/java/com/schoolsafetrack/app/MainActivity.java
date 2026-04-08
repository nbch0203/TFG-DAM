package com.schoolsafetrack.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.schoolsafetrack.app.data.repository.SessionManager;
import com.schoolsafetrack.app.ui.driver.DriverMainActivity;
import com.schoolsafetrack.app.ui.login.LoginActivity;
import com.schoolsafetrack.app.ui.parent.ParentMainActivity;

/**
 * Activity de arranque. Comprueba si hay sesión activa y redirige
 * al panel correspondiente o a la pantalla de login.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        Intent intent;

        if (session.isLoggedIn()) {
            String role = session.getRole();
            if ("DRIVER".equalsIgnoreCase(role)) {
                intent = new Intent(this, DriverMainActivity.class);
            } else if ("PARENT".equalsIgnoreCase(role)) {
                intent = new Intent(this, ParentMainActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
