
package com.example.onenutrition.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.example.onenutrition.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.onenutrition.administrador.AdministradorActivity;
import com.example.onenutrition.comprador.CompradorActivity;
import com.example.onenutrition.vendedor.VendedorActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import java.util.concurrent.Executor;
import com.example.onenutrition.administrador.Usuario;
import com.example.onenutrition.comprador.perfil.PerfilManager;
import com.google.android.material.button.MaterialButton;

public class DesbloqueoBiometricoActivity
        extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(  R.layout.activity_desbloqueo_biometrico);
        MaterialButton btnIniciarConContrasena =findViewById(R.id.btnIniciarConContrasena);
        btnIniciarConContrasena.setOnClickListener(v -> {
            cerrarSesionYVolverAlLogin();
        });
        FirebaseUser usuario =FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {

            BloqueoBiometricoManager.limpiarBloqueo(this);
            Intent intent = new Intent(this,MainActivity.class);
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity(intent);
            finish();
            return;
        }

        configurarBiometria();
    }

    private void configurarBiometria() {

        int autenticadores =BiometricManager.Authenticators.BIOMETRIC_STRONG| BiometricManager.Authenticators.BIOMETRIC_WEAK;

        BiometricManager manager =BiometricManager.from(this);

        if (manager.canAuthenticate(autenticadores)!= BiometricManager.BIOMETRIC_SUCCESS) {

            Toast.makeText(this,"La autenticación biométrica no está disponible", Toast.LENGTH_LONG).show();
            return;
        }

        Executor executor =ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this,executor,new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {

                        super.onAuthenticationSucceeded(result);
                        // La huella fue reconocida.
                        // Mantener el indicador activo mientras validamos Firebase.
                        validarSesion();
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            CharSequence errString) {

                        super.onAuthenticationError(errorCode, errString);
                        BloqueoBiometricoManager.setAutenticacionEnCurso(false);
                        Toast.makeText( DesbloqueoBiometricoActivity.this,"Autenticación cancelada: " + errString, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        BiometricPrompt.PromptInfo promptInfo =new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("OneNutrition")
                        .setSubtitle("Desbloquea tu aplicación")
                        .setAllowedAuthenticators(autenticadores)
                        .setNegativeButtonText("Cancelar")
                        .build();

        BloqueoBiometricoManager.setAutenticacionEnCurso(true);
        biometricPrompt.authenticate(promptInfo);
    }


    private void validarSesion() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser usuario = auth.getCurrentUser();

        if (usuario == null) {
            regresarAlLogin();
            return;
        }

        // Actualizar y comprobar la sesión autenticada
        usuario.reload().addOnCompleteListener(tarea -> {

            if (!tarea.isSuccessful()) {
                Toast.makeText(this,"No se pudo validar la sesión",Toast.LENGTH_LONG ).show();
                return;
            }

            FirebaseUser usuarioActual = auth.getCurrentUser();

            if (usuarioActual == null) {
                regresarAlLogin();
                return;
            }

            if (!usuarioActual.isEmailVerified()) {

                Toast.makeText(this,"Debes verificar tu correo electrónico",Toast.LENGTH_LONG).show();
                auth.signOut();
                regresarAlLogin();
                return;
            }

            String uid = usuarioActual.getUid();

            // Consultar el perfil actualizado en Firestore
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .get(Source.SERVER)
                    .addOnSuccessListener(documento -> {

                        if (!documento.exists()) {

                            Toast.makeText(this,"No se encontró el perfil del usuario",Toast.LENGTH_LONG).show();
                            auth.signOut();
                            regresarAlLogin();
                            return;
                        }

                        // Comprobar que la cuenta esté activa
                        Boolean activo =documento.getBoolean("activo");

                        if (!Boolean.TRUE.equals(activo)) {

                            Toast.makeText(this,"Tu cuenta ha sido deshabilitada", Toast.LENGTH_LONG).show();
                            auth.signOut();
                            regresarAlLogin();
                            return;
                        }

                        // Obtener el rol real
                        String rol = documento.getString("rol");

                        Intent intent;

                        if ("Comprador".equals(rol)) {

                            intent = new Intent(this, CompradorActivity.class);

                        } else if ("Vendedor".equals(rol)) {

                            intent = new Intent(this,VendedorActivity.class);

                        } else if ("Administrador".equals(rol)) {

                            intent = new Intent(this, AdministradorActivity.class);

                        } else {

                            Toast.makeText(this, "Rol no autorizado",Toast.LENGTH_LONG).show();
                            auth.signOut();
                            regresarAlLogin();
                            return;
                        }


                       // RECUPERAR LOS DATOS DEL USUARIO

                        String nombre = documento.getString("nombre");
                        String correo = documento.getString("correo");
                        String telefono = documento.getString("telefono");

                        // Crear el usuario con los datos actuales de Firestore
                        Usuario usuarioPerfil = new Usuario(nombre,correo,telefono,"",rol);

                    // Asociar el UID de Firebase
                        usuarioPerfil.setUid(uid);

                        // Restaurar la sesión local del perfil
                        PerfilManager.setUsuarioActual(usuarioPerfil);

                        // La huella y las validaciones fueron correctas
                        BloqueoBiometricoManager.desbloquear(this);

                        // La huella y las validaciones fueron correctas
                        BloqueoBiometricoManager.desbloquear(this);
                        BloqueoBiometricoManager.setAutenticacionEnCurso(false);
                        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }).addOnFailureListener(e -> {

                        Toast.makeText( this,"No se pudo verificar el perfil. Comprueba tu conexión.",Toast.LENGTH_LONG).show();
                    });
        });
    }
    private void regresarAlLogin() {

        BloqueoBiometricoManager.limpiarBloqueo(this);

        Intent intent = new Intent( this,MainActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void cerrarSesionYVolverAlLogin() {

        BloqueoBiometricoManager.setAutenticacionEnCurso(false);
        BloqueoBiometricoManager.limpiarBloqueo(this);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this,MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
