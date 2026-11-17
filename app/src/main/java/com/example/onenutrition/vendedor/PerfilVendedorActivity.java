package com.example.onenutrition.vendedor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.example.onenutrition.auth.BloqueoBiometricoManager;
import com.example.onenutrition.auth.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilVendedorActivity extends ActividadProtegida {

    private MaterialButton btnEditarPerfil;
    private MaterialButton btnCerrarSesion;
    private TextView tvNombre;
    private TextView tvCorreo;
    private TextView tvTelefono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_vendedor);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfilVendedor);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionVendedor);
        tvNombre = findViewById(R.id.tvNombrePerfilVendedor);
        tvCorreo = findViewById(R.id.tvCorreoPerfilVendedor);
        tvTelefono = findViewById(R.id.tvTelefonoPerfilVendedor);
        configurarBotones();
        configurarNavegacion();
    }

    private void configurarBotones() {

        btnEditarPerfil.setOnClickListener(v -> {

            Intent intent = new Intent(PerfilVendedorActivity.this, EditarPerfilVendedorActivity.class);
            startActivity(intent);
        });

        btnCerrarSesion.setOnClickListener(v -> confirmarCerrarSesion());
    }

    private void confirmarCerrarSesion() {

        new AlertDialog.Builder(this).setTitle("Cerrar sesión").setMessage("¿Estás seguro de que deseas cerrar sesión?").setPositiveButton("Cerrar sesión", (dialog, which) -> cerrarSesion()).setNegativeButton("Cancelar", null).show();
    }


    private void cerrarSesion() {

        // 1. Finalizar cualquier autenticación biométrica en curso
        BloqueoBiometricoManager.setAutenticacionEnCurso(false);

        // 2. Limpiar el estado de bloqueo de la aplicación
        BloqueoBiometricoManager.limpiarBloqueo(this);

        // 3. Cerrar la sesión activa de Firebase
        FirebaseAuth.getInstance().signOut();

        // 4. Regresar a la pantalla de inicio de sesión
        Intent intent = new Intent(PerfilVendedorActivity.this, MainActivity.class);

        // 5. Eliminar las pantallas anteriores del historial
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }


    private void configurarNavegacion() {

        TextView navInicio = findViewById(R.id.navInicio);
        TextView navPedidos = findViewById(R.id.navPedidos);
        TextView navProductos = findViewById(R.id.navProductos);
        navInicio.setOnClickListener(v -> {

            Intent intent = new Intent(PerfilVendedorActivity.this, VendedorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navPedidos.setOnClickListener(v -> {

            Intent intent = new Intent(PerfilVendedorActivity.this, PedidosVendedorActivity.class);
            startActivity(intent);
            finish();
        });

        navProductos.setOnClickListener(v -> {

            Intent intent = new Intent(PerfilVendedorActivity.this, ProductosVendedorActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void cargarDatosPerfil() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            return;
        }

        FirebaseFirestore.getInstance().collection("usuarios").document(usuario.getUid()).get().addOnSuccessListener(documento -> {

            if (documento.exists()) {

                String nombre = documento.getString("nombre");
                String correo = documento.getString("correo");
                String telefono = documento.getString("telefono");
                tvNombre.setText(nombre != null ? nombre : "Sin nombre");
                tvCorreo.setText(correo != null ? correo : "Sin correo");
                tvTelefono.setText(telefono != null ? telefono : "Sin teléfono");
            }

        }).addOnFailureListener(e -> {

            Toast.makeText(PerfilVendedorActivity.this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();

        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosPerfil();
    }
}