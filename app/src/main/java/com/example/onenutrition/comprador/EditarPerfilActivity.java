package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.perfil.PerfilManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditarPerfilActivity extends ActividadProtegida {

    private ImageButton btnVolver;
    private TextInputEditText etNombre;
    private TextInputEditText etCorreo;
    private TextInputEditText etTelefono;
    private MaterialButton btnGuardar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // Componentes del XML
        btnVolver = findViewById(R.id.btnVolverEditarPerfil);
        etNombre = findViewById(R.id.etEditarNombre);
        etCorreo = findViewById(R.id.etEditarCorreo);
        etTelefono = findViewById(R.id.etEditarTelefono);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);

        // Inicializar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Mostrar los datos actuales
        cargarDatos();

        // El correo no se puede modificar desde esta pantalla
        etCorreo.setEnabled(false);
        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void cargarDatos() {

        etNombre.setText(PerfilManager.getNombre());
        etCorreo.setText(PerfilManager.getCorreo());
        etTelefono.setText(PerfilManager.getTelefono());
    }

    private void guardarCambios() {

        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        // Validar nombre
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa tu nombre");
            etNombre.requestFocus();
            return;
        }

        // Validar teléfono
        if (!telefono.matches("\\d{10}")) {
            etTelefono.setError("El teléfono debe tener 10 dígitos");
            etTelefono.requestFocus();
            return;
        }

        // Obtener el usuario autenticado
        FirebaseUser usuarioFirebase = firebaseAuth.getCurrentUser();

        if (usuarioFirebase == null) {

            Toast.makeText(this, "Tu sesión ha finalizado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

            return;
        }

        // Solo enviamos los campos permitidos por Firestore
        Map<String, Object> cambios = new HashMap<>();

        cambios.put("nombre", nombre);
        cambios.put("telefono", telefono);

        // Evitar solicitudes repetidas
        btnGuardar.setEnabled(false);

        // Actualizar el documento del usuario
        firestore.collection("usuarios").document(usuarioFirebase.getUid()).update(cambios)

                .addOnSuccessListener(unused -> {

                    // Actualizar también los datos en memoria
                    PerfilManager.actualizarPerfil(nombre, PerfilManager.getCorreo(), telefono);

                    Toast.makeText(EditarPerfilActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

                    finish();
                })

                .addOnFailureListener(e -> {

                    btnGuardar.setEnabled(true);

                    Toast.makeText(EditarPerfilActivity.this, "Error al actualizar el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
