package com.example.onenutrition.comprador.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class PedidoFirebase {

    private String compradorId;
    private String estado;
    private String estadoPago;
    private String metodoPago;
    private String direccion;
    private String barrio;
    private String ciudad;
    private String indicaciones;
    private String numeroPedido;
    private Long consecutivo;
    private Double latitud;
    private Double longitud;
    private String nombreComprador;
    private double total;
    private Timestamp fechaCreacion;
    private String vendedorAsignadoId;
    private List<ProductoPedido> productos;

    // Constructor vacío requerido por Firestore
    public PedidoFirebase() {
    }

    public PedidoFirebase(String compradorId, String metodoPago, String direccion, String barrio, String ciudad, String indicaciones, Double latitud, Double longitud, double total, List<ProductoPedido> productos) {

        this.compradorId = compradorId;
        this.metodoPago = metodoPago;
        this.estado = "Recibido";
        this.estadoPago = "Pendiente";
        this.direccion = direccion;
        this.barrio = barrio;
        this.ciudad = ciudad;
        this.indicaciones = indicaciones;
        this.latitud = latitud;
        this.longitud = longitud;
        this.total = total;
        this.productos = productos;
        this.fechaCreacion = Timestamp.now();
    }

    public String getCompradorId() {
        return compradorId;
    }

    public String getEstado() {
        return estado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getBarrio() {
        return barrio;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public double getTotal() {
        return total;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public List<ProductoPedido> getProductos() {
        return productos;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public Long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Long consecutivo) {
        this.consecutivo = consecutivo;
    }

    public String getVendedorAsignadoId() {
        return vendedorAsignadoId;
    }

    public void setVendedorAsignadoId(String vendedorAsignadoId) {
        this.vendedorAsignadoId = vendedorAsignadoId;
    }

    public String getNombreComprador() {
        return nombreComprador;
    }

    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }
}
