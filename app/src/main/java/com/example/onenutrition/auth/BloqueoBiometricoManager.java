
package com.example.onenutrition.auth;

import android.content.Context;

public class BloqueoBiometricoManager {

    private static final String PREFS = "onenutrition_biometria";
    private static final String CLAVE_ACTIVADA = "biometria_activada";
    private static final String CLAVE_BLOQUEADA = "aplicacion_bloqueada";
    private static boolean autenticacionEnCurso = false;

    public static void activarBiometria(Context context, boolean activar) {

        String uid = obtenerUidActual();

        if (uid == null) {
            return;
        }

        context.getSharedPreferences( PREFS,Context.MODE_PRIVATE).edit().putBoolean(CLAVE_ACTIVADA + "_" + uid, activar).apply();
    }

    public static boolean biometriaActivada( Context context) {

        String uid = obtenerUidActual();

        if (uid == null) {
            return false;
        }

        return context.getSharedPreferences( PREFS,Context.MODE_PRIVATE ).getBoolean(CLAVE_ACTIVADA + "_" + uid, false);
    }

    private static String obtenerUidActual() {

        com.google.firebase.auth.FirebaseUser usuario = com.google.firebase.auth.FirebaseAuth .getInstance().getCurrentUser();
        return usuario != null ? usuario.getUid() : null;
    }


    // Bloquear la aplicación
    public static void bloquear(Context context) {

        context.getSharedPreferences( PREFS,Context.MODE_PRIVATE ).edit().putBoolean(CLAVE_BLOQUEADA, true).apply();
    }

    // Desbloquear la aplicación
    public static void desbloquear(Context context) {

        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE ).edit().putBoolean(CLAVE_BLOQUEADA, false).apply();
    }

    // Consultar si está bloqueada
    public static boolean estaBloqueada(Context context) {

        return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE ).getBoolean(CLAVE_BLOQUEADA, false);
    }

    // Limpiar el bloqueo al cerrar sesión
    public static void limpiarBloqueo(Context context) {

        context.getSharedPreferences( PREFS,Context.MODE_PRIVATE ).edit().putBoolean(CLAVE_BLOQUEADA, false) .apply();
    }
    public static void setAutenticacionEnCurso(boolean enCurso) {
        autenticacionEnCurso = enCurso;
    }

    public static boolean autenticacionEnCurso() {
        return autenticacionEnCurso;
    }
}
