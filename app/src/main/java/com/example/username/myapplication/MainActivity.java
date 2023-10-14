package com.example.username.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String USUARIO = "USUARIO";
    private SharedPreferences pref;
    final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.PhoneBuilder().build());
    private FirebaseAuth mFirebaseAuth;

    private FirebaseAuth.AuthStateListener mAuthStateListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getPreferences(MODE_PRIVATE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListner = firebaseAuth -> {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                launchLoginScreen();
                return;
            }
            guardarSharedPref(user.getDisplayName());
            final Intent i = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        };


        launchLoginScreen();
    }

    private void launchLoginScreen() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.splash_screen).setTheme(R.style.AppTheme)
                .build();

        registerForActivityResult(new FirebaseAuthUIActivityResultContract(), (result) -> {
        }).launch(signInIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListner);

    }

    private void guardarSharedPref(String usuario) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USUARIO, usuario);
        editor.apply();
    }
}
