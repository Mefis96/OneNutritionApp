
package com.example.onenutrition.administrador;

public class UsuarioFirestore {

    private String uid;
    private String nombre;
    private String correo;
    private String telefono;
    private String rol;

    // Constructor vacío que requiere Firestore
    public UsuarioFirestore() {
    }

    // Constructor para crear perfiles de usuario
    public UsuarioFirestore(String uid, String nombre,String correo, String telefono,String rol) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
