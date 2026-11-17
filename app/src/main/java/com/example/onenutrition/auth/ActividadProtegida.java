
package com.example.onenutrition.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import androidx.biometric.BiometricManager;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public abstract class ActividadProtegida
        extends AppCompatActivity {

    private boolean redirigiendo = false;
    private static boolean activacionConsultada = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        verificarBloqueo();
    }

    @Override
    protected void onResume() {
        super.onResume();

        verificarBloqueo();
    }

    private void verificarBloqueo() {

        if (redirigiendo) {
            return;
        }

        // No abrir el desbloqueo sin una sesión de Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        // Comprobar si la biometría está activada
        if (!BloqueoBiometricoManager.biometriaActivada(this)) {
            return;
        }

        // Comprobar si la aplicación está bloqueada
        if (!BloqueoBiometricoManager.estaBloqueada(this)) {
            return;
        }

        redirigiendo = true;

        Intent intent = new Intent(
                this,
                DesbloqueoBiometricoActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    protected void ofrecerActivacionBiometrica() {

        if (activacionConsultada) {
            return;
        }
        // No volver a preguntar si ya está activada
        if (BloqueoBiometricoManager.biometriaActivada(this)) {
            return;
        }

        // Comprobar disponibilidad de huella
        BiometricManager manager = BiometricManager.from(this);

        int autenticadores = BiometricManager.Authenticators.BIOMETRIC_STRONG| BiometricManager.Authenticators.BIOMETRIC_WEAK;

        if (manager.canAuthenticate(autenticadores) != BiometricManager.BIOMETRIC_SUCCESS) {
            return;
        }

        activacionConsultada = true;

        new AlertDialog.Builder(this)
                .setTitle("Activar acceso biométrico")
                .setMessage( "¿Deseas utilizar tu huella para desbloquear " + "OneNutrition cuando regreses a la aplicación?")
                .setPositiveButton("Activar", (dialog, which) -> {
                    confirmarActivacionBiometrica();
                }).setNegativeButton("Ahora no", null).show();
    }

    private void confirmarActivacionBiometrica() {

        int autenticadores = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK;
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt( this, executor, new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {

                        super.onAuthenticationSucceeded(result);

                        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            BloqueoBiometricoManager.setAutenticacionEnCurso(false);
                            return;
                        }

                        BloqueoBiometricoManager.activarBiometria( ActividadProtegida.this,true);
                        BloqueoBiometricoManager.setAutenticacionEnCurso(false);

                        Toast.makeText( ActividadProtegida.this,"Desbloqueo con huella activado correctamente",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,CharSequence errString) {
                        super.onAuthenticationError(errorCode,errString);
                        BloqueoBiometricoManager.setAutenticacionEnCurso(false);
                        Toast.makeText(ActividadProtegida.this,"No se activó la biometría: " + errString,Toast.LENGTH_SHORT).show();
                    }
                }
        );

        BiometricPrompt.PromptInfo promptInfo =new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Activar huella digital")
                        .setSubtitle("Confirma tu identidad para activar el desbloqueo")
                        .setAllowedAuthenticators(autenticadores)
                        .setNegativeButtonText("Cancelar")
                        .build();

        BloqueoBiometricoManager.setAutenticacionEnCurso(true);
        biometricPrompt.authenticate(promptInfo);
    }


}
