package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;

import com.example.onenutrition.auth.ActividadProtegida;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Geocoder;
import android.location.Address;

import java.util.List;
import java.util.Locale;

import android.os.Build;

import java.io.IOException;

public class DireccionEntregaActivity extends ActividadProtegida {

    private ImageButton btnVolver;
    private TextInputEditText etDireccion;
    private TextInputEditText etBarrio;
    private TextInputEditText etCiudad;
    private TextInputEditText etIndicaciones;
    private MaterialButton btnContinuarPago;
    private MaterialButton btnUsarUbicacion;
    private TextView tvEstadoUbicacion;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Double latitud = null;
    private Double longitud = null;
    private ActivityResultLauncher<String[]> permisoUbicacionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direccion_entrega);

        btnVolver = findViewById(R.id.btnVolverDireccion);
        etDireccion = findViewById(R.id.etDireccion);
        etBarrio = findViewById(R.id.etBarrio);
        etCiudad = findViewById(R.id.etCiudad);
        etIndicaciones = findViewById(R.id.etIndicaciones);
        btnContinuarPago = findViewById(R.id.btnContinuarPago);
        btnUsarUbicacion = findViewById(R.id.btnUsarUbicacion);
        tvEstadoUbicacion = findViewById(R.id.tvEstadoUbicacion);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.contenedorMapa);


        if (mapFragment != null) {
            mapFragment.getMapAsync(map -> {

                googleMap = map;

                // Mostrar controles de zoom
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                // Mostrar ubicación si ya fue obtenida
                if (latitud != null && longitud != null) {
                    mostrarUbicacionEnMapa();
                }

                // Permitir seleccionar un punto tocando el mapa
                googleMap.setOnMapClickListener(punto -> {

                    latitud = punto.latitude;
                    longitud = punto.longitude;

                    // Actualizar el marcador
                    mostrarUbicacionEnMapa();
                    obtenerDireccionDesdeCoordenadas();

                    // Informar al comprador
                    tvEstadoUbicacion.setText("Punto de entrega seleccionado.\n" + "Latitud: " + latitud + "\nLongitud: " + longitud);

                    Toast.makeText(DireccionEntregaActivity.this, "Punto de entrega actualizado", Toast.LENGTH_SHORT).show();

                });

            });
        }


        permisoUbicacionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permisos -> {

            boolean precisa = Boolean.TRUE.equals(permisos.get(Manifest.permission.ACCESS_FINE_LOCATION));
            boolean aproximada = Boolean.TRUE.equals(permisos.get(Manifest.permission.ACCESS_COARSE_LOCATION));
            if (precisa || aproximada) {
                obtenerUbicacionActual();
            } else {
                tvEstadoUbicacion.setText("Permiso denegado. Puedes ingresar la dirección manualmente.");
            }
        });

        btnUsarUbicacion.setOnClickListener(v -> solicitarUbicacion());
        btnVolver.setOnClickListener(v -> finish());
        btnContinuarPago.setOnClickListener(v -> validarDireccion());
    }


    private void solicitarUbicacion() {

        boolean permisoPreciso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permisoAproximado = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permisoPreciso || permisoAproximado) {
            obtenerUbicacionActual();
        } else {
            permisoUbicacionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void obtenerUbicacionActual() {

        boolean permisoPreciso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean permisoAproximado = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!permisoPreciso && !permisoAproximado) {
            return;
        }

        tvEstadoUbicacion.setText("Obteniendo ubicación...");
        btnUsarUbicacion.setEnabled(false);

        int prioridad = permisoPreciso ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        CurrentLocationRequest solicitud = new CurrentLocationRequest.Builder().setPriority(prioridad).setMaxUpdateAgeMillis(10000).build();
        CancellationTokenSource token = new CancellationTokenSource();

        try {

            fusedLocationClient.getCurrentLocation(solicitud, token.getToken()).addOnSuccessListener(this, ubicacion -> {

                btnUsarUbicacion.setEnabled(true);

                if (ubicacion != null) {

                    latitud = ubicacion.getLatitude();
                    longitud = ubicacion.getLongitude();
                    mostrarUbicacionEnMapa();
                    obtenerDireccionDesdeCoordenadas();
                    tvEstadoUbicacion.setText("Ubicación obtenida correctamente.\n" + "Latitud: " + latitud + "\nLongitud: " + longitud);
                    Toast.makeText(this, "Ubicación obtenida correctamente", Toast.LENGTH_SHORT).show();

                } else {

                    tvEstadoUbicacion.setText("No se pudo obtener la ubicación. " + "Activa la ubicación del dispositivo " + "o ingresa la dirección manualmente.");
                }

            }).addOnFailureListener(this, error -> {

                btnUsarUbicacion.setEnabled(true);
                tvEstadoUbicacion.setText("Error al obtener ubicación: " + error.getMessage());
            });

        } catch (SecurityException e) {

            btnUsarUbicacion.setEnabled(true);
            tvEstadoUbicacion.setText("No se cuenta con permisos de ubicación.");
        }
    }


    private void mostrarUbicacionEnMapa() {

        if (googleMap == null || latitud == null || longitud == null) {
            return;
        }

        LatLng ubicacionComprador = new LatLng(latitud, longitud);

        // Eliminar marcadores anteriores
        googleMap.clear();

        // Agregar marcador de ubicación
        googleMap.addMarker(new MarkerOptions().position(ubicacionComprador).title("Mi ubicación").snippet("Punto de entrega de OneNutrition"));

        // Centrar y acercar el mapa
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionComprador, 16f));
    }


    private void obtenerDireccionDesdeCoordenadas() {

        if (latitud == null || longitud == null) {
            return;
        }

        if (!Geocoder.isPresent()) {
            tvEstadoUbicacion.setText("Servicio de geocodificación no disponible. " + "Ingresa la dirección manualmente.");
            return;
        }

        double latitudConsulta = latitud;
        double longitudConsulta = longitud;

        Geocoder geocoder = new Geocoder(DireccionEntregaActivity.this, new Locale("es", "CO"));

        tvEstadoUbicacion.setText("Buscando dirección...");

        // Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            geocoder.getFromLocation(latitudConsulta, longitudConsulta, 1, new Geocoder.GeocodeListener() {

                @Override
                public void onGeocode(List<Address> direcciones) {
                    runOnUiThread(() -> procesarDireccion(direcciones, latitudConsulta, longitudConsulta));
                }

                @Override
                public void onError(String mensajeError) {
                    runOnUiThread(() -> {
                        if (esUbicacionActual(latitudConsulta, longitudConsulta)) {
                            tvEstadoUbicacion.setText("No se pudo obtener la dirección. " + "Ingresa los datos manualmente.");
                        }
                    });
                }
            });

        } else {

            // Android 8 a Android 12:
            // ejecutar la consulta fuera del hilo principal
            new Thread(() -> {

                List<Address> direcciones = null;

                try {
                    direcciones = geocoder.getFromLocation(latitudConsulta, longitudConsulta, 1);
                } catch (IOException | IllegalArgumentException e) {
                    android.util.Log.e("OneNutrition", "Error de geocodificación", e);
                }

                final List<Address> resultado = direcciones;

                runOnUiThread(() -> procesarDireccion(resultado, latitudConsulta, longitudConsulta));

            }).start();
        }
    }

    private boolean esUbicacionActual(double latitudConsulta, double longitudConsulta) {

        return latitud != null && longitud != null && Double.compare(latitud, latitudConsulta) == 0 && Double.compare(longitud, longitudConsulta) == 0;
    }

    private void procesarDireccion(List<Address> direcciones, double latitudConsulta, double longitudConsulta) {

        // Ignorar respuestas de ubicaciones anteriores
        if (!esUbicacionActual(latitudConsulta, longitudConsulta)) {
            return;
        }

        if (direcciones == null || direcciones.isEmpty()) {
            tvEstadoUbicacion.setText("No se encontró una dirección. " + "Puedes ingresarla manualmente.");
            return;
        }

        Address direccion = direcciones.get(0);
        String calle = direccion.getThoroughfare();
        String numero = direccion.getSubThoroughfare();
        String barrio = direccion.getSubLocality();
        String ciudad = direccion.getLocality();

        if (ciudad == null || ciudad.isEmpty()) {
            ciudad = direccion.getSubAdminArea();
        }

        String direccionCompleta = "";

        if (calle != null && !calle.isEmpty()) {
            direccionCompleta = calle;

            if (numero != null && !numero.isEmpty()) {
                direccionCompleta += " " + numero;
            }
        }

        etDireccion.setText(direccionCompleta);
        etBarrio.setText(barrio != null ? barrio : "");
        etCiudad.setText(ciudad != null ? ciudad : "");

        tvEstadoUbicacion.setText("Dirección aproximada encontrada. " + "Verifica los datos antes de continuar.");
    }


    private void validarDireccion() {

        String direccion = etDireccion.getText().toString().trim();
        String barrio = etBarrio.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();

        if (direccion.isEmpty()) {
            etDireccion.setError("Ingresa la dirección de entrega");
            etDireccion.requestFocus();
            return;
        }

        if (barrio.isEmpty()) {
            etBarrio.setError("Ingresa el barrio");
            etBarrio.requestFocus();
            return;
        }

        if (ciudad.isEmpty()) {
            etCiudad.setError("Ingresa la ciudad");
            etCiudad.requestFocus();
            return;
        }

        Intent intent = new Intent(DireccionEntregaActivity.this, MetodoPagoActivity.class);

// Datos de entrega
        intent.putExtra("direccion", direccion);
        intent.putExtra("barrio", barrio);
        intent.putExtra("ciudad", ciudad);
        String indicaciones = etIndicaciones.getText() != null ? etIndicaciones.getText().toString().trim() : "";
        intent.putExtra("indicaciones", indicaciones);

// Coordenadas seleccionadas en Google Maps
        if (latitud != null && longitud != null) {
            intent.putExtra("latitud", latitud);
            intent.putExtra("longitud", longitud);
        }

        startActivity(intent);


    }
}