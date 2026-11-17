package com.example.onenutrition.administrador;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class EditarPerfilAdministradorActivity extends ActividadProtegida {

    private TextInputEditText etNombreAdmin;
    private TextInputEditText etCorreoAdmin;
    private TextInputEditText etTelefonoAdmin;
    private MaterialButton btnGuardarPerfilAdmin;
    private MaterialButton btnCancelarPerfilAdmin;
    private Usuario administrador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_administrador);

        // Componentes
        etNombreAdmin = findViewById(R.id.etNombreAdmin);
        etCorreoAdmin = findViewById(R.id.etCorreoAdmin);
        etTelefonoAdmin = findViewById(R.id.etTelefonoAdmin);
        btnGuardarPerfilAdmin = findViewById(R.id.btnGuardarPerfilAdmin);
        btnCancelarPerfilAdmin = findViewById(R.id.btnCancelarPerfilAdmin);

        cargarAdministrador();

        btnGuardarPerfilAdmin.setOnClickListener(v -> guardarCambios());
        btnCancelarPerfilAdmin.setOnClickListener(v -> finish());
    }


    private void cargarAdministrador() {

        // OBTENER USUARIO AUTENTICADO
        FirebaseUser usuarioFirebase =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioFirebase == null) {

            Toast.makeText(
                    this,
                    "No hay una sesión activa",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // OBTENER UID DEL ADMINISTRADOR
        String uid = usuarioFirebase.getUid();

        // CONSULTAR PERFIL EN FIRESTORE
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get(Source.SERVER)
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {

                        Toast.makeText(
                                this,
                                "No se encontró el perfil del administrador",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    // VERIFICAR ROL
                    String rol = documento.getString("rol");

                    if (!"Administrador".equals(
                            rol != null ? rol.trim() : "")) {

                        Toast.makeText(
                                this,
                                "No tienes permisos de administrador",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    // CREAR OBJETO CON DATOS REALES
                    administrador = new Usuario(
                            documento.getString("nombre"),
                            documento.getString("correo"),
                            documento.getString("telefono"),
                            "",
                            rol
                    );

                    administrador.setUid(uid);

                    // MOSTRAR INFORMACIÓN EN LOS CAMPOS
                    etNombreAdmin.setText(administrador.getNombre());
                    etCorreoAdmin.setText(administrador.getCorreo());
                    etTelefonoAdmin.setText(administrador.getTelefono());

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Error al cargar el perfil: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }


    private void guardarCambios() {

        String nombre = etNombreAdmin.getText().toString().trim();
        String correo = etCorreoAdmin.getText().toString().trim();
        String telefono = etTelefonoAdmin.getText().toString().trim();

        // Validar nombre
        if (nombre.isEmpty()) {
            etNombreAdmin.setError("Ingresa tu nombre");
            etNombreAdmin.requestFocus();
            return;
        }

        // Validar correo
        if (correo.isEmpty()) {
            etCorreoAdmin.setError("Ingresa tu correo electrónico");
            etCorreoAdmin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoAdmin.setError("Ingresa un correo electrónico válido");
            etCorreoAdmin.requestFocus();
            return;
        }

        // Validar teléfono
        if (telefono.isEmpty()) {
            etTelefonoAdmin.setError("Ingresa tu teléfono");
            etTelefonoAdmin.requestFocus();
            return;
        }

        if (telefono.length() != 10) {
            etTelefonoAdmin.setError("El teléfono debe tener 10 dígitos");
            etTelefonoAdmin.requestFocus();
            return;
        }

        // Actualizar el mismo usuario
        administrador.setNombre(nombre);
        administrador.setCorreo(correo);
        administrador.setTelefono(telefono);

        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

        finish();
    }
}