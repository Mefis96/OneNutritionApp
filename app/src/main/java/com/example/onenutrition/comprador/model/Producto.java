package com.example.onenutrition.comprador.model;

public class Producto {

    private String nombre;
    private String categoria;
    private String descripcion;
    private double precio;

    // Imagen incluida en drawable
    private int imagen;

    // Imagen seleccionada desde el celular
    private String imagenUri;
    // Datos para Firebase
    private String idProducto;
    private String idVendedor;
    private String imagenUrl;

    // Constructor vacío requerido para Firestore
    public Producto() {
    }

    // Constructor original.
    // Se mantiene para no romper el código existente.
    public Producto(String nombre, String categoria, double precio, int imagen) {

        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = "";
        this.precio = precio;
        this.imagen = imagen;
        this.imagenUri = null;
    }

    // Constructor con descripción.
    public Producto(String nombre, String categoria, String descripcion, double precio, int imagen) {

        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
        this.imagenUri = null;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public int getImagen() {
        return imagen;
    }

    public String getImagenUri() {
        return imagenUri;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public void setImagenUri(String imagenUri) {
        this.imagenUri = imagenUri;
    }

    public boolean tieneImagenUri() {
        return imagenUri != null && !imagenUri.isEmpty();
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

}