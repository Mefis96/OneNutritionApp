package com.example.onenutrition.comprador.model;

public class Pedido {

    private String numeroPedido;
    private String fecha;
    private String metodoPago;
    private String estado;
    private double total;

    public Pedido(String numeroPedido, String fecha, String metodoPago, String estado, double total) {

        this.numeroPedido = numeroPedido;
        this.fecha = fecha;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.total = total;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public String getFecha() {
        return fecha;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public double getTotal() {
        return total;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}