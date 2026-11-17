package com.example.onenutrition.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onenutrition.R;
import com.example.onenutrition.administrador.UsuarioFirestore;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends ActividadProtegida {

    private TextInputEditText etName;
    private TextInputEditText etRegisterEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etRegisterPassword;
    private TextInputEditText etConfirmRegisterPassword;

    private CheckBox cbTerms;
    private Button btnCreateAccount;
    private TextView tvGoToLogin;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etName = findViewById(R.id.etName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmRegisterPassword = findViewById(R.id.etConfirmRegisterPassword);

        cbTerms = findViewById(R.id.cbTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Inicializar servicios de Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnCreateAccount.setOnClickListener(v -> validarRegistro());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    // Valida los datos antes de crear la cuenta.
    private void validarRegistro() {

        String nombre = etName.getText().toString().trim();
        String correo = etRegisterEmail.getText().toString().trim();
        String telefono = etPhone.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmarPassword = etConfirmRegisterPassword.getText().toString();

        if (nombre.isEmpty()) {
            etName.setError("Ingresa tu nombre completo");
            etName.requestFocus();
            return;
        }

        if (correo.isEmpty()) {
            etRegisterEmail.setError("Ingresa tu correo electrónico");
            etRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etRegisterEmail.setError("Ingresa un correo electrónico válido");
            etRegisterEmail.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            etPhone.setError("Ingresa tu número de teléfono");
            etPhone.requestFocus();
            return;
        }

        if (!telefono.matches("[0-9]{10}")) {
            etPhone.setError("El teléfono debe tener 10 dígitos");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegisterPassword.setError("Ingresa una contraseña");
            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            etRegisterPassword.setError("La contraseña debe tener mínimo 8 caracteres");
            etRegisterPassword.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            etRegisterPassword.setError("Debe contener al menos una letra mayúscula");
            etRegisterPassword.requestFocus();
            return;
        }

        if (!password.matches(".*[0-9].*")) {
            etRegisterPassword.setError("Debe contener al menos un número");
            etRegisterPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            etConfirmRegisterPassword.setError("Las contraseñas no coinciden");
            etConfirmRegisterPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        registrarUsuario(nombre, correo, telefono, password);
    }

    // Crea la cuenta en Firebase Authentication.
    private void registrarUsuario(String nombre, String correo, String telefono, String password) {

        btnCreateAccount.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(correo, password).addOnCompleteListener(this, task -> {

            if (!task.isSuccessful()) {
                btnCreateAccount.setEnabled(true);

                Toast.makeText(this, "No se pudo crear la cuenta: " + (task.getException() != null ? task.getException().getMessage() : "Error desconocido"), Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                btnCreateAccount.setEnabled(true);

                Toast.makeText(this, "No se pudo obtener el usuario creado", Toast.LENGTH_LONG).show();
                return;
            }

            String uid = firebaseUser.getUid();
            String correoFirebase = firebaseUser.getEmail();

            // Crear perfil sin almacenar contraseña
            UsuarioFirestore nuevoUsuario = new UsuarioFirestore(uid, nombre, correo, telefono, "Comprador");

            android.util.Log.d("REGISTRO_FIREBASE", "UID: " + uid);
            android.util.Log.d("REGISTRO_FIREBASE", "Nombre: " + nombre);
            android.util.Log.d("REGISTRO_FIREBASE", "Correo ingresado: " + correo);
            android.util.Log.d("REGISTRO_FIREBASE", "Correo Firebase: " + firebaseUser.getEmail());
            android.util.Log.d("REGISTRO_FIREBASE", "Rol: Comprador");

            // Guardar perfil en Firestore
            firestore.collection("usuarios").document(uid).set(nuevoUsuario)

                    .addOnSuccessListener(unused -> {

                        // Enviar correo de verificación al usuario
                        firebaseUser.sendEmailVerification().addOnCompleteListener(verificacion -> {

                            firebaseAuth.signOut();

                            if (verificacion.isSuccessful()) {

                                Toast.makeText(RegistroActivity.this, "Cuenta creada. Revisa tu correo y verifica tu cuenta.", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(RegistroActivity.this, "Cuenta creada, pero no se pudo enviar el correo. " + "Podrás solicitar otro enlace.", Toast.LENGTH_LONG).show();
                            }

                            finish();
                        });
                    })

                    .addOnFailureListener(e -> {

                        btnCreateAccount.setEnabled(true);

                        Toast.makeText(this, "La cuenta se creó, pero no se pudo guardar " + "el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        // No borrar automáticamente la cuenta:
                        // conservamos el error para diagnosticarlo.
                        firebaseAuth.signOut();
                    });
        });
    }
}
