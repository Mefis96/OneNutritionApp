package com.example.onenutrition.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onenutrition.administrador.Usuario;
import com.example.onenutrition.comprador.perfil.PerfilManager;
import com.example.onenutrition.R;
import com.example.onenutrition.administrador.AdministradorActivity;
import com.example.onenutrition.comprador.CompradorActivity;
import com.example.onenutrition.vendedor.VendedorActivity;
import com.google.android.material.textfield.TextInputEditText;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Source;


public class MainActivity extends ActividadProtegida {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private Button btnBiometric;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Componentes del formulario
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnBiometric = findViewById(R.id.btnBiometric);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // Iniciar sesión normal
        btnLogin.setOnClickListener(v -> iniciarSesion());

        // Iniciar sesión con biometría
        btnBiometric.setOnClickListener(v -> iniciarSesionBiometria());

        // Recuperar contraseña
        tvForgotPassword.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, RecuperarPasswordActivity.class);

            startActivity(intent);

        });

        // Registro
        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, RegistroActivity.class);

            startActivity(intent);
        });
    }

    /// /////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Valida las credenciales ingresadas y permite el acceso si el usuario está registrado.
    private void iniciarSesion() {

        String correo = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validar correo vacío
        if (correo.isEmpty()) {
            etEmail.setError("Ingresa tu correo electrónico");
            etEmail.requestFocus();
            return;
        }

        // Validar formato del correo
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmail.setError("Ingresa un correo electrónico válido");
            etEmail.requestFocus();
            return;
        }

        // Validar contraseña vacía
        if (password.isEmpty()) {
            etPassword.setError("Ingresa tu contraseña");
            etPassword.requestFocus();
            return;
        }

        // Desactivar botón mientras Firebase verifica las credenciales
        btnLogin.setEnabled(false);

        // Autenticar usuario con Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(correo, password).addOnCompleteListener(this, task -> {

            // Volver a activar el botón
            btnLogin.setEnabled(true);


            if (task.isSuccessful()) {

                // Obtener el usuario autenticado en Firebase
                FirebaseUser usuarioFirebase = firebaseAuth.getCurrentUser();

                if (usuarioFirebase == null) {

                    Toast.makeText(MainActivity.this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_LONG).show();

                    firebaseAuth.signOut();
                    return;
                }

                // Actualizar el estado de verificación del correo
                usuarioFirebase.reload().addOnCompleteListener(recarga -> {

                    if (!recarga.isSuccessful()) {

                        Toast.makeText(MainActivity.this, "No se pudo comprobar la verificación del correo", Toast.LENGTH_LONG).show();

                        firebaseAuth.signOut();
                        return;
                    }

                    // Bloquear acceso si el correo no está verificado
                    if (!usuarioFirebase.isEmailVerified()) {

                        Toast.makeText(MainActivity.this, "Debes verificar tu correo electrónico antes de ingresar", Toast.LENGTH_LONG).show();

                        firebaseAuth.signOut();
                        return;
                    }

                    // Obtener el UID del usuario verificado
                    String uid = usuarioFirebase.getUid();

                    // Buscar su perfil en Cloud Firestore
                    firestore.collection("usuarios").document(uid).get(Source.SERVER).addOnSuccessListener(document -> {

                        if (!document.exists()) {

                            Toast.makeText(MainActivity.this, "No se encontró el perfil del usuario", Toast.LENGTH_LONG).show();

                            firebaseAuth.signOut();
                            return;
                        }

                        // COMPROBAR SI LA CUENTA ESTÁ HABILITADA

                        Boolean activo = document.getBoolean("activo");

                        if (!Boolean.TRUE.equals(activo)) {

                            Toast.makeText(MainActivity.this, "Tu cuenta ha sido deshabilitada. Comunícate con el administrador.", Toast.LENGTH_LONG).show();

                            firebaseAuth.signOut();
                            PerfilManager.setUsuarioActual(null);

                            return;
                        }


                        // Obtener el rol almacenado en Firestore
                        String rol = document.getString("rol");

                        android.util.Log.d("LOGIN_ROL", "UID: " + uid + " | Rol recibido: [" + rol + "]" + " | Longitud: " + (rol != null ? rol.length() : 0));


                        if ("Comprador".equals(rol) || "Vendedor".equals(rol) || "Administrador".equals(rol)) {

                            // Obtener los datos personales desde Firestore
                            String nombre = document.getString("nombre");
                            String correoUsuario = document.getString("correo");
                            String telefono = document.getString("telefono");

                            // Crear objeto Usuario
                            Usuario usuario = new Usuario(nombre, correoUsuario, telefono, "", rol);

                            // Asociar UID de Firebase
                            usuario.setUid(uid);

                            // Guardar el usuario en PerfilManager
                            PerfilManager.setUsuarioActual(usuario);

                            Toast.makeText(MainActivity.this, "Bienvenido a OneNutrition", Toast.LENGTH_SHORT).show();

                            // Redirigir según el rol
                            Intent intent;

                            if ("Comprador".equals(rol)) {

                                intent = new Intent(MainActivity.this, CompradorActivity.class);

                            } else if ("Vendedor".equals(rol)) {

                                intent = new Intent(MainActivity.this, VendedorActivity.class);

                            } else {

                                intent = new Intent(MainActivity.this, AdministradorActivity.class);
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(MainActivity.this, "Rol no autorizado: " + rol, Toast.LENGTH_LONG).show();

                            firebaseAuth.signOut();
                            PerfilManager.setUsuarioActual(null);
                        }

                    }).addOnFailureListener(e -> {

                        Toast.makeText(MainActivity.this, "Error al consultar Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        firebaseAuth.signOut();
                        PerfilManager.setUsuarioActual(null);
                    });


                });

            } else {

                Toast.makeText(MainActivity.this, "No fue posible iniciar sesión. Verifica tus credenciales y conexión.", Toast.LENGTH_LONG).show();
            }


        });
    }

    /// /////////////////////////////////////////////////////////////////////////////////////////////////////
// Verifica la disponibilidad de biometría y permite iniciar sesión mediante huella.
    private void iniciarSesionBiometria() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Comprobar que exista una sesión de Firebase
        if (firebaseUser == null) {
            Toast.makeText(this, "Primero inicia sesión con tu correo y contraseña", Toast.LENGTH_LONG).show();
            return;
        }

        // Comprobar que el correo esté verificado
        firebaseUser.reload().addOnCompleteListener(task -> {

            FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

            if (!task.isSuccessful() || usuarioActual == null || !usuarioActual.isEmailVerified()) {

                Toast.makeText(this, "No se pudo validar una cuenta verificada", Toast.LENGTH_LONG).show();
                return;
            }

            // Comprobar disponibilidad de biometría
            BiometricManager biometricManager = BiometricManager.from(this);

            int autenticadores = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK;


            int resultadoBiometria = biometricManager.canAuthenticate(autenticadores);

            if (resultadoBiometria != BiometricManager.BIOMETRIC_SUCCESS) {

                Toast.makeText(MainActivity.this, "Error biométrico. Código: " + resultadoBiometria, Toast.LENGTH_LONG).show();

                return;
            }


            Executor executor = ContextCompat.getMainExecutor(this);

            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {

                    super.onAuthenticationSucceeded(result);

                    // Consultar el perfil real en Firestore
                    FirebaseFirestore.getInstance().collection("usuarios").document(usuarioActual.getUid()).get().addOnSuccessListener(documento -> {

                        if (!documento.exists()) {
                            Toast.makeText(MainActivity.this, "No se encontró el perfil del usuario", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String rol = documento.getString("rol");

                        if (!"Comprador".equals(rol)) {
                            Toast.makeText(MainActivity.this, "Rol no autorizado", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Usuario usuario = new Usuario(documento.getString("nombre"), documento.getString("correo"), documento.getString("telefono"), "", rol);

                        usuario.setUid(usuarioActual.getUid());

                        PerfilManager.setUsuarioActual(usuario);

                        Intent intent = new Intent(MainActivity.this, CompradorActivity.class);

                        startActivity(intent);
                        finish();

                    }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "No se pudo consultar el perfil", Toast.LENGTH_LONG).show());
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Acceso a OneNutrition").setSubtitle("Confirma tu identidad").setAllowedAuthenticators(autenticadores).setNegativeButtonText("Cancelar").build();

            biometricPrompt.authenticate(promptInfo);
        });
    }


    /// ////////////////////////////////////////////////////////////////////////////////////////////////////

    // Identifica el rol del usuario y abre la pantalla correspondiente.
    private void abrirSegunRol(Usuario usuario) {

        switch (usuario.getRol()) {

            case "Comprador":

                PerfilManager.setUsuarioActual(usuario);
                Toast.makeText(this, "Bienvenido a OneNutrition", Toast.LENGTH_SHORT).show();
                abrirPantalla(CompradorActivity.class);
                break;

            case "Vendedor":

                Toast.makeText(this, "Bienvenido, vendedor", Toast.LENGTH_SHORT).show();
                abrirPantalla(VendedorActivity.class);
                break;

            case "Administrador":

                Toast.makeText(this, "Bienvenido, administrador", Toast.LENGTH_SHORT).show();
                abrirPantalla(AdministradorActivity.class);
                break;

            default:

                Toast.makeText(this, "El usuario no tiene un rol válido", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /// //////////////////////////////////////////////////////////////////////
    // Abre la Activity indicada utilizando un Intent.
    private void abrirPantalla(Class<?> activityDestino) {

        Intent intent = new Intent(MainActivity.this, activityDestino);
        startActivity(intent);
    }
}