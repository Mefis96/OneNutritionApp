
package com.example.onenutrition;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.example.onenutrition.auth.BloqueoBiometricoManager;
import com.google.firebase.auth.FirebaseAuth;

public class OneNutritionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(new DefaultLifecycleObserver() {

                    @Override
                    public void onStop(@NonNull LifecycleOwner owner) {

                        // Solo bloquear si el usuario tiene
                        // una sesión autenticada en Firebase.
                        if (FirebaseAuth.getInstance()
                                .getCurrentUser() == null) {
                            return;
                        }

                        // Respetar la preferencia del usuario.
                        if (!BloqueoBiometricoManager
                                .biometriaActivada(
                                        OneNutritionApplication.this)) {
                            return;
                        }

                        // Evitar bloquear durante una autenticación biométrica
                        if (BloqueoBiometricoManager.autenticacionEnCurso()) {
                            return;
                        }

                        // Registrar que se requiere desbloqueo.
                        BloqueoBiometricoManager.bloquear(
                                OneNutritionApplication.this
                        );
                    }
                });
    }
}
