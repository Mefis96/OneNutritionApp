package com.example.onenutrition.comprador.model;

public class ProductoPedido {

    private String idProducto;
    private String idVendedor;
    private String nombre;
    private String categoria;
    private String imagenUrl;

    private double precioUnitario;
    private int cantidad;
    private double subtotal;

    // Constructor vacío requerido por Firestore
    public ProductoPedido() {
    }

    public ProductoPedido(ItemCarrito item) {

        this.idProducto = item.getIdProducto();
        this.idVendedor = item.getIdVendedor();
        this.nombre = item.getNombre();
        this.categoria = item.getCategoria();
        this.imagenUrl = item.getImagenUrl();

        this.precioUnitario = item.getPrecio();
        this.cantidad = item.getCantidad();
        this.subtotal = item.getSubtotal();
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
