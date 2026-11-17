package com.example.onenutrition.comprador.perfil;

import com.example.onenutrition.administrador.Usuario;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilManager {

    private static Usuario usuarioActual;

    private PerfilManager() {
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static String getNombre() {

        if (usuarioActual != null) {
            return usuarioActual.getNombre();
        }

        return "";
    }

    public static String getCorreo() {

        if (usuarioActual != null) {
            return usuarioActual.getCorreo();
        }

        return "";
    }

    public static String getTelefono() {

        if (usuarioActual != null) {
            return usuarioActual.getTelefono();
        }

        return "";
    }

    public static void actualizarPerfil(String nuevoNombre, String nuevoCorreo, String nuevoTelefono) {

        if (usuarioActual != null) {

            usuarioActual.setNombre(nuevoNombre);
            usuarioActual.setCorreo(nuevoCorreo);
            usuarioActual.setTelefono(nuevoTelefono);
        }
    }

    public static void cerrarSesion() {

        // Cerrar la sesión de Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Limpiar los datos del usuario en memoria
        usuarioActual = null;
    }
}