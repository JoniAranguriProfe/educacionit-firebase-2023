package com.example.username.myapplication;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.perf.metrics.Trace;

public class MainActivity extends AppCompatActivity {

    private static final String USUARIO = "USUARIO";
    private LinearLayout container;
    private Button btnIniciarSesion;
    private Button btnCrearUsuario;
    private TextView etUsuario;
    private TextView etPassword;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getPreferences(MODE_PRIVATE);
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etContraseña);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnCrearUsuario = findViewById(R.id.btnCrearUsuario);
        container = findViewById(R.id.container);

        btnCrearUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInProgress();
            }
        });

        btnIniciarSesion.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String password = etPassword.getText().toString();
            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Completar datos", Toast.LENGTH_SHORT).show();
            } else {
                final Bundle userParams = new Bundle();
                userParams.putString("username", usuario);

                FirebaseAnalytics.getInstance(MainActivity.this).logEvent(
                        "USER_SIGN_IN", userParams
                );
                guardarSharedPref(usuario);

                final Trace homeIntentTrace = FirebasePerformance.getInstance().newTrace("HOME_ACTIVITY_INTENT");

                homeIntentTrace.start();
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("USUARIO", usuario);
                intent.putExtra("PASSWORD", password);
                homeIntentTrace.stop();

                startActivity(intent);
                finish();
            }
        });

        cargarSharedPref();
        askForNotificationsPermissions();
    }

    private void askForNotificationsPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void mostrarInProgress() {
        Snackbar.make(container, "En progreso", Snackbar.LENGTH_LONG).show();
    }

    @AddTrace(name = "SAVE_NAME")
    private void guardarSharedPref(String usuario) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USUARIO, usuario);
        editor.apply();
    }

    private void cargarSharedPref() {
        String usuario = pref.getString(USUARIO, "");
        etUsuario.setText(usuario);
    }
}
