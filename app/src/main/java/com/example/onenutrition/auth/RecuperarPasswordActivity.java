package com.example.onenutrition.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onenutrition.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarPasswordActivity extends ActividadProtegida {

    private TextInputEditText etEmail;
    private Button btnSendCode;
    private TextView tvBackLogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        // Componentes de la interfaz
        etEmail = findViewById(R.id.etRecoveryEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        // Inicializar Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Enviar correo de recuperación
        btnSendCode.setOnClickListener(v -> recuperarPassword());

        // Regresar al Login
        tvBackLogin.setOnClickListener(v -> finish());
    }

    // Valida el correo y solicita el enlace de recuperación.
    private void recuperarPassword() {

        String correo = etEmail.getText().toString().trim();

        if (correo.isEmpty()) {
            etEmail.setError("Ingresa tu correo electrónico");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmail.setError("Ingresa un correo electrónico válido");
            etEmail.requestFocus();
            return;
        }

        // Evitar múltiples solicitudes simultáneas
        btnSendCode.setEnabled(false);

        // Solicitar a Firebase el correo de recuperación
        firebaseAuth.sendPasswordResetEmail(correo).addOnCompleteListener(this, task -> {

            btnSendCode.setEnabled(true);

            if (task.isSuccessful()) {

                Toast.makeText(RecuperarPasswordActivity.this, "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.", Toast.LENGTH_LONG).show();

                // Regresar al Login
                finish();

            } else {

                Toast.makeText(RecuperarPasswordActivity.this, "No se pudo procesar la solicitud. Comprueba tu conexión e inténtalo nuevamente.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
