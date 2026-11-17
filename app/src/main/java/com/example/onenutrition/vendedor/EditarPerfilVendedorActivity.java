package com.example.onenutrition.vendedor;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditarPerfilVendedorActivity extends ActividadProtegida {

    private TextInputEditText etNombre;
    private TextInputEditText etCorreo;
    private TextInputEditText etTelefono;
    private ImageButton btnVolver;
    private MaterialButton btnGuardar;
    private FirebaseFirestore db;
    private FirebaseUser usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_vendedor);
        etNombre = findViewById(R.id.etNombreEditarPerfilVendedor);
        etCorreo = findViewById(R.id.etCorreoEditarPerfilVendedor);
        etTelefono = findViewById(R.id.etTelefonoEditarPerfilVendedor);
        btnVolver = findViewById(R.id.btnVolverEditarPerfilVendedor);
        btnGuardar = findViewById(R.id.btnGuardarPerfilVendedor);
        db = FirebaseFirestore.getInstance();
        usuario = FirebaseAuth.getInstance().getCurrentUser();

        // El correo no se puede modificar desde esta pantalla
        etCorreo.setEnabled(false);
        btnGuardar.setEnabled(false);
        cargarDatos();
        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void cargarDatos() {

        if (usuario == null) {
            Toast.makeText(this, "No hay una sesión activa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("usuarios").document(usuario.getUid()).get().addOnSuccessListener(documento -> {

            if (!documento.exists()) {
                Toast.makeText(this, "No se encontró el perfil", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombre = documento.getString("nombre");
            String correo = documento.getString("correo");
            String telefono = documento.getString("telefono");
            etNombre.setText(nombre != null ? nombre : "");
            etCorreo.setText(correo != null ? correo : "");
            etTelefono.setText(telefono != null ? telefono : "");

            btnGuardar.setEnabled(true);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void guardarCambios() {

        if (usuario == null) {
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa tu nombre");
            etNombre.requestFocus();
            return;
        }

        if (nombre.length() > 150) {
            etNombre.setError("Máximo 150 caracteres");
            etNombre.requestFocus();
            return;
        }

        if (!telefono.matches("[0-9]{10}")) {
            etTelefono.setError("El teléfono debe tener 10 dígitos");
            etTelefono.requestFocus();
            return;
        }

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("nombre", nombre);
        cambios.put("telefono", telefono);

        btnGuardar.setEnabled(false);

        db.collection("usuarios").document(usuario.getUid()).update(cambios).addOnSuccessListener(unused -> {

            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

            finish();
        }).addOnFailureListener(e -> {

            btnGuardar.setEnabled(true);

            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
