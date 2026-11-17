package com.example.onenutrition.administrador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;
import com.example.onenutrition.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.onenutrition.auth.BloqueoBiometricoManager;
import com.example.onenutrition.auth.DesbloqueoBiometricoActivity;
import com.example.onenutrition.auth.ActividadProtegida;

public class AdministradorActivity extends ActividadProtegida {

    private TextView tvTotalProductos;
    private TextView tvTotalUsuarios;
    private TextView tvTotalPedidos;
    private TextView tvVentas;
    private TextView navInicio;
    private TextView navProductos;
    private TextView navUsuarios;
    private TextView navReportes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador);

        ofrecerActivacionBiometrica();
        inicializarVistas();
        configurarNavegacion();
    }

    @Override
    protected void onResume() {
        super.onResume();

        actualizarResumen();
    }

    private void inicializarVistas() {

        // Resumen
        tvTotalProductos = findViewById(R.id.tvTotalProductosAdmin);
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuariosAdmin);
        tvTotalPedidos = findViewById(R.id.tvTotalPedidosAdmin);
        tvVentas = findViewById(R.id.tvVentasAdmin);
        // Navegación
        navInicio = findViewById(R.id.navAdminInicio);
        navProductos = findViewById(R.id.navAdminProductos);
        navUsuarios = findViewById(R.id.navAdminUsuarios);
        navReportes = findViewById(R.id.navAdminReportes);

        TextView btnCerrarSesion = findViewById(R.id.btnCerrarSesionAdmin);

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }


    private void actualizarResumen() {

        // 1. TOTAL DE PRODUCTOS
        FirebaseFirestore.getInstance()
                .collection("productos")
                .get()
                .addOnSuccessListener(documentos -> {

                    int totalProductos = documentos.size();

                    tvTotalProductos.setText(
                            String.valueOf(totalProductos)
                    );

                })
                .addOnFailureListener(e -> {

                    tvTotalProductos.setText("--");

                    android.util.Log.e(
                            "ADMIN_PRODUCTOS",
                            "Error al consultar productos",
                            e
                    );
                });


        // 2. TOTAL DE USUARIOS
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .get()
                .addOnSuccessListener(documentos -> {

                    int totalUsuarios = documentos.size();

                    tvTotalUsuarios.setText(
                            String.valueOf(totalUsuarios)
                    );

                    android.util.Log.d(
                            "ADMIN_USUARIOS",
                            "Usuarios encontrados: " + totalUsuarios
                    );

                })
                .addOnFailureListener(e -> {

                    tvTotalUsuarios.setText("--");

                    android.util.Log.e(
                            "ADMIN_USUARIOS",
                            "Error al consultar usuarios",
                            e
                    );
                });



        // 3. TOTAL DE PEDIDOS DESDE FIRESTORE
        FirebaseFirestore.getInstance()
                .collection("pedidosPublicos")
                .get()
                .addOnSuccessListener(documentos -> {

                    int totalPedidos = documentos.size();

                    tvTotalPedidos.setText(
                            String.valueOf(totalPedidos)
                    );

                    android.util.Log.d(
                            "ADMIN_PEDIDOS",
                            "Pedidos encontrados: " + totalPedidos
                    );

                })
                .addOnFailureListener(e -> {

                    tvTotalPedidos.setText("--");

                    android.util.Log.e(
                            "ADMIN_PEDIDOS",
                            "Error al consultar pedidos",
                            e
                    );
                });




        // 4. VENTAS REALES DESDE FIRESTORE
        FirebaseFirestore.getInstance()
                .collection("pedidos")
                .whereEqualTo("estadoPago", "Pagado")
                .get()
                .addOnSuccessListener(documentos -> {

                    double ventasTotales = 0;

                    for (com.google.firebase.firestore.QueryDocumentSnapshot documento
                            : documentos) {

                        Double total = documento.getDouble("total");

                        if (total != null) {
                            ventasTotales += total;
                        }
                    }

                    NumberFormat formato =
                            NumberFormat.getCurrencyInstance(
                                    new Locale("es", "CO")
                            );

                    tvVentas.setText(
                            formato.format(ventasTotales)
                    );

                    android.util.Log.d(
                            "ADMIN_VENTAS",
                            "Ventas pagadas: " + ventasTotales
                    );

                })
                .addOnFailureListener(e -> {

                    tvVentas.setText("--");

                    android.util.Log.e(
                            "ADMIN_VENTAS",
                            "Error al consultar ventas",
                            e
                    );
                });

    }


    private void configurarNavegacion() {

        // INICIO
        navInicio.setOnClickListener(v -> {

        });

        // PRODUCTOS
        navProductos.setOnClickListener(v -> {

            Intent intent = new Intent(AdministradorActivity.this, ProductosAdministradorActivity.class);

            startActivity(intent);
        });

        // USUARIOS
        navUsuarios.setOnClickListener(v -> {

            Intent intent = new Intent(AdministradorActivity.this, UsuariosAdministradorActivity.class);

            startActivity(intent);
        });

        // REPORTES
        navReportes.setOnClickListener(v -> {

            Intent intent = new Intent(AdministradorActivity.this, ReporteVentasActivity.class);

            startActivity(intent);
        });
    }

    private void cerrarSesion() {

        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Cerrar sesión").setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar", (dialog, which) -> {

                    BloqueoBiometricoManager.setAutenticacionEnCurso(false);
                    BloqueoBiometricoManager.limpiarBloqueo(this);
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(AdministradorActivity.this, com.example.onenutrition.auth.MainActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }).setNegativeButton("Cancelar", null).show();


    }

    private void bloquearAplicacion() {

        // Comprobar que exista una sesión de Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "No hay una sesión activa",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Marcar la aplicación como bloqueada
        BloqueoBiometricoManager.bloquear(this);

        // Abrir la pantalla de desbloqueo
        Intent intent = new Intent(
                AdministradorActivity.this,
                DesbloqueoBiometricoActivity.class
        );

        // Eliminar las pantallas anteriores del historial
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

}