package com.example.username.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvLibros;
    private AirplaneStateReceiver airplaneStateReceiver = new AirplaneStateReceiver();
    private LibrosAdapter adapter;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabAgregarLibro;
    final static String LIBRO = "LIBRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        saludarUsuario();
        setupToolbar();
        configurarNavigationView();
        setupAdapter();
        setupUI();

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.getString("Nombre", "");
        prefs.getInt("Edad", 0);

        initializeSyncService();

        createSyncAlarm();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mis libros");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void configurarNavigationView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuAboutMe:
                        irAAboutMe();
                        break;
                    case R.id.menuCerrarSesion:
                        cerrarSesion();
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void irAAboutMe() {
        Intent intent = new Intent(this, AboutMeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void cerrarSesion() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToAgregarLibro() {
        Intent intent = new Intent(HomeActivity.this, AgregarLibroActivity.class);
        startActivity(intent);
    }

    private void setupUI() {
        fabAgregarLibro = findViewById(R.id.fabAgregarLibro);
        fabAgregarLibro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAgregarLibro();
            }
        });
    }

    private void setupAdapter() {
        rvLibros = findViewById(R.id.rvLibros);
        adapter = new LibrosAdapter(getLibros(), new LibrosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Libro libro, TextView txtNombre, TextView txtAutor) {
                goToDetalleLibro(libro, txtNombre, txtAutor);
            }
        });
        rvLibros.setAdapter(adapter);
    }

    private void goToDetalleLibro(Libro libro, TextView txtNombre, TextView txtAutor) {
        Intent intent = new Intent(this, DetalleLibroActivity.class);
        intent.putExtra(LIBRO, libro);

        Pair<View, String> p1 = Pair.create((View) txtNombre, getString(R.string.nombre_libro));
        Pair<View, String> p2 = Pair.create((View) txtAutor, getString(R.string.nombre_autor));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                p1, p2);
        startActivity(intent, options.toBundle());
    }

    private void initializeSyncService() {
        Intent intent = new Intent(this, SyncService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setLibros(getLibros());
        adapter.notifyDataSetChanged();

        registerReceiver(airplaneStateReceiver, new IntentFilter(ACTION_AIRPLANE_MODE_CHANGED));
    }

    private List<Libro> getLibros() {
        try {
            return LibroManager.getInstance(this).getLibros();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saludarUsuario() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String usuario = bundle.getString("USUARIO");
            Toast.makeText(HomeActivity.this, "Bienvenido " + usuario, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, SyncService.class));
        unregisterReceiver(airplaneStateReceiver);
        super.onDestroy();
    }

    private void createSyncAlarm() {
        Intent intent = new Intent(this, SyncDataReceiver.class);
        boolean alarmExists = PendingIntent.getBroadcast(this, 0, intent, FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;

        if (!alarmExists) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);

            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 86400000, pendingIntent);
        }
    }
}
