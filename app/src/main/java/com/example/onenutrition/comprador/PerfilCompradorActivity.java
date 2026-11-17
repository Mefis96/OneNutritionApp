package com.example.onenutrition.comprador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.example.onenutrition.auth.BloqueoBiometricoManager;
import com.example.onenutrition.auth.MainActivity;
import com.google.android.material.button.MaterialButton;

import android.widget.TextView;

import com.example.onenutrition.comprador.perfil.PerfilManager;

import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.imageview.ShapeableImageView;
import com.example.onenutrition.vendedor.CloudinaryManager;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.Nullable;

public class PerfilCompradorActivity extends ActividadProtegida {

    private ImageButton btnVolverPerfil;
    private MaterialButton btnEditarPerfil;
    private MaterialButton btnCerrarSesion;
    private TextView tvNombrePerfil;
    private TextView tvCorreoPerfil;
    private TextView tvTelefonoPerfil;
    private ShapeableImageView imgFotoPerfil;
    private TextView tvCambiarFotoPerfil;
    private int versionFotoPerfil = 0;
    private boolean subiendoFoto = false;
    private TextView tvInicialesPerfil;
    private final ActivityResultLauncher<String> selectorFoto = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            mostrarFotoSeleccionada(uri);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_comprador);
        btnVolverPerfil = findViewById(R.id.btnVolverPerfil);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnVolverPerfil.setOnClickListener(v -> finish());
        btnEditarPerfil.setOnClickListener(v -> {

            Intent intent = new Intent(PerfilCompradorActivity.this, EditarPerfilActivity.class);
            startActivity(intent);
        });

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        tvNombrePerfil = findViewById(R.id.tvNombrePerfil);
        tvCorreoPerfil = findViewById(R.id.tvCorreoPerfil);
        tvTelefonoPerfil = findViewById(R.id.tvTelefonoPerfil);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        tvInicialesPerfil = findViewById(R.id.tvInicialesPerfil);
        tvCambiarFotoPerfil = findViewById(R.id.tvCambiarFotoPerfil);


        // Abrir la galería al tocar la fotografía.
        imgFotoPerfil.setOnClickListener(v -> abrirGaleria());

        // También permitir cambiarla desde el texto.
        tvCambiarFotoPerfil.setOnClickListener(v -> abrirGaleria());
    }


    private String obtenerIniciales(String nombreCompleto) {

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "?";
        }

        String[] partes = nombreCompleto.trim().split("\\s+");
        String primeraInicial = partes[0].substring(0, 1);

        if (partes.length == 1) {
            return primeraInicial.toUpperCase(java.util.Locale.ROOT);
        }

        String segundaInicial = partes[1].substring(0, 1);
        return (primeraInicial + segundaInicial).toUpperCase(java.util.Locale.ROOT);
    }


    private void cerrarSesion() {

        // 1. Finalizar cualquier autenticación biométrica en curso
        BloqueoBiometricoManager.setAutenticacionEnCurso(false);

        // 2. Limpiar el estado del bloqueo biométrico
        BloqueoBiometricoManager.limpiarBloqueo(this);

        // 3. Cerrar sesión en Firebase y limpiar datos locales
        PerfilManager.cerrarSesion();

        // 4. Regresar a la pantalla de inicio de sesión
        Intent intent = new Intent(PerfilCompradorActivity.this, MainActivity.class);

        // 5. Eliminar las pantallas anteriores del historial
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void cargarFotoPerfil(String fotoPerfilUrl) {

        if (fotoPerfilUrl == null || fotoPerfilUrl.trim().isEmpty()) {

            Glide.with(this).clear(imgFotoPerfil);
            imgFotoPerfil.setImageResource(R.drawable.bg_perfil_avatar);
            tvInicialesPerfil.setVisibility(View.VISIBLE);
            return;
        }

        // Mostrar las iniciales mientras carga la imagen.
        tvInicialesPerfil.setVisibility(View.VISIBLE);

        Glide.with(this).load(fotoPerfilUrl).circleCrop().listener(new RequestListener<Drawable>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                tvInicialesPerfil.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {

                tvInicialesPerfil.setVisibility(View.GONE);
                return false;
            }
        }).into(imgFotoPerfil);
    }


    private void cargarDatosPerfil() {

        // 1. Mostrar los datos personales del comprador.
        String nombre = PerfilManager.getNombre();
        tvNombrePerfil.setText(nombre);
        tvCorreoPerfil.setText(PerfilManager.getCorreo());
        tvTelefonoPerfil.setText(PerfilManager.getTelefono());
        // 2. Obtener y preparar las iniciales.
        String iniciales = obtenerIniciales(nombre);
        tvInicialesPerfil.setText(iniciales);
        // 3. Obtener el usuario autenticado.
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            cargarFotoPerfil(null);
            return;
        }

        // 4. Evitar interferir con una fotografía que se está subiendo.
        if (subiendoFoto) {
            return;
        }
        final int versionConsulta = ++versionFotoPerfil;
        // 5. Consultar la fotografía guardada en Firestore.
        FirebaseFirestore.getInstance().collection("usuarios").document(usuario.getUid()).get().addOnSuccessListener(documento -> {

            // No reemplazar la imagen si comenzó una subida.
            if (subiendoFoto || versionConsulta != versionFotoPerfil) {
                return;
            }

            // Si no existe el documento, mostrar iniciales.
            if (!documento.exists()) {
                cargarFotoPerfil(null);
                return;
            }

            // Obtener la URL guardada en Firestore.
            String fotoPerfilUrl = documento.getString("fotoPerfilUrl");

            // Mostrar fotografía o iniciales según corresponda.
            cargarFotoPerfil(fotoPerfilUrl);
        }).addOnFailureListener(error -> {

            if (subiendoFoto || versionConsulta != versionFotoPerfil) {
                return;
            }

            // Mantener las iniciales como alternativa.
            cargarFotoPerfil(null);

            Toast.makeText(PerfilCompradorActivity.this, "No se pudo cargar la fotografía: " + error.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    private void abrirGaleria() {

        // Permitir seleccionar una imagen del dispositivo.
        selectorFoto.launch("image/*");
    }

    private void mostrarFotoSeleccionada(Uri uri) {

        if (subiendoFoto) {
            Toast.makeText(this, "Espera a que termine la subida anterior", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null || !usuario.isEmailVerified()) {
            Toast.makeText(this, "Debes iniciar sesión para cambiar tu fotografía", Toast.LENGTH_LONG).show();
            return;
        }

        subiendoFoto = true;
        versionFotoPerfil++;
        imgFotoPerfil.setEnabled(false);
        tvCambiarFotoPerfil.setEnabled(false);
        tvCambiarFotoPerfil.setText("Subiendo fotografía...");

        CloudinaryManager.subirFotoPerfil(this, uri, new CloudinaryManager.SubidaCallback() {

            @Override
            public void onExito(String imagenUrl) {

                // Guardar la URL en el perfil del usuario.
                FirebaseFirestore.getInstance().collection("usuarios").document(usuario.getUid()).update("fotoPerfilUrl", imagenUrl).addOnSuccessListener(unused -> {

                    subiendoFoto = false;
                    imgFotoPerfil.setEnabled(true);
                    tvCambiarFotoPerfil.setEnabled(true);
                    tvCambiarFotoPerfil.setText("Cambiar fotografía");

                    // Mostrar la fotografía guardada.
                    com.bumptech.glide.Glide.with(PerfilCompradorActivity.this).load(imagenUrl).circleCrop().into(imgFotoPerfil);

                    Toast.makeText(PerfilCompradorActivity.this, "Fotografía guardada correctamente", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(error -> {
                    restaurarControlesFoto();
                    Toast.makeText(PerfilCompradorActivity.this, "No se pudo guardar la fotografía: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String mensaje) {

                restaurarControlesFoto();
                Toast.makeText(PerfilCompradorActivity.this, "Error al subir fotografía: " + mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void restaurarControlesFoto() {

        subiendoFoto = false;
        imgFotoPerfil.setEnabled(true);
        tvCambiarFotoPerfil.setEnabled(true);
        tvCambiarFotoPerfil.setText("Cambiar fotografía");
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosPerfil();
    }
}