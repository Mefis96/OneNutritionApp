package com.example.onenutrition;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.firestore.Source;
import com.example.onenutrition.auth.ActividadProtegida;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.onenutrition.administrador.Usuario;
import com.example.onenutrition.administrador.AdministradorActivity;
import com.example.onenutrition.auth.MainActivity;
import com.example.onenutrition.comprador.CompradorActivity;
import com.example.onenutrition.comprador.perfil.PerfilManager;
import com.example.onenutrition.vendedor.VendedorActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends ActividadProtegida {

    private static final int SPLASH_TIME = 2000;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_splash);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        new Handler(Looper.getMainLooper()).postDelayed(this::comprobarSesion, SPLASH_TIME);
    }

    private void comprobarSesion() {

        FirebaseUser usuarioFirebase = firebaseAuth.getCurrentUser();

        if (usuarioFirebase == null) {
            abrirLogin();
            return;
        }

        // Actualizar el estado del usuario en Firebase
        usuarioFirebase.reload().addOnCompleteListener(task -> {

            FirebaseUser usuarioActual = firebaseAuth.getCurrentUser();

            if (!task.isSuccessful() || usuarioActual == null) {
                abrirLogin();
                return;
            }

            if (!usuarioActual.isEmailVerified()) {
                firebaseAuth.signOut();
                PerfilManager.setUsuarioActual(null);
                abrirLogin();
                return;
            }

            String uid = usuarioActual.getUid();

            // Consultar el perfil real en Firestore
            firestore.collection("usuarios").document(uid).get(Source.SERVER).addOnSuccessListener(documento -> {

                if (!documento.exists()) {
                    abrirLogin();
                    return;
                }

                String rol = documento.getString("rol");
                Usuario usuario = new Usuario(documento.getString("nombre"), documento.getString("correo"), documento.getString("telefono"), "", rol);
                usuario.setUid(uid);
                PerfilManager.setUsuarioActual(usuario);

                if ("Comprador".equals(rol)) {

                    abrirPantalla(CompradorActivity.class);

                } else if ("Vendedor".equals(rol)) {

                    abrirPantalla(VendedorActivity.class);

                } else if ("Administrador".equals(rol)) {

                    abrirPantalla(AdministradorActivity.class);

                } else {

                    PerfilManager.setUsuarioActual(null);
                    abrirLogin();
                }

            }).addOnFailureListener(e -> abrirLogin());
        });
    }

    private void abrirPantalla(Class<?> pantalla) {

        Intent intent = new Intent(SplashActivity.this, pantalla);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void abrirLogin() {

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
