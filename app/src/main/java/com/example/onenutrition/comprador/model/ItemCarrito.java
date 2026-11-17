package com.example.onenutrition.comprador.model;

public class ItemCarrito {

    private String idProducto;
    private String idVendedor;
    private String nombre;
    private String categoria;
    private double precio;
    private int imagen;
    private String imagenUri;
    private String imagenUrl;
    private int cantidad;

    // Constructor original para imágenes drawable.
    public ItemCarrito(String nombre, String categoria, double precio, int imagen, int cantidad) {

        this(nombre, categoria, precio, imagen, null, cantidad);
    }

    // Constructor original para imágenes locales.
    public ItemCarrito(String nombre, String categoria, double precio, int imagen, String imagenUri, int cantidad) {

        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagen = imagen;
        this.imagenUri = imagenUri;
        this.cantidad = cantidad;
    }

    // Constructor para productos registrados en Firebase.
    public ItemCarrito(String idProducto, String idVendedor, String nombre, String categoria, double precio, int imagen, String imagenUrl, int cantidad) {

        this.idProducto = idProducto;
        this.idVendedor = idVendedor;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagen = imagen;
        this.imagenUrl = imagenUrl;
        this.cantidad = cantidad;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public boolean tieneImagenUri() {
        return imagenUri != null && !imagenUri.isEmpty();
    }

    public double getSubtotal() {
        return precio * cantidad;
    }
}
