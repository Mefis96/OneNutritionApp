
package com.example.onenutrition.vendedor;

import com.google.firebase.Timestamp;

public class PedidoPublicoFirebase {

    private String pedidoId;
    private String numeroPedido;
    private String estado;
    private String vendedorAsignadoId;
    private double total;
    private Timestamp fechaCreacion;

    // Constructor vacío requerido por Firestore
    public PedidoPublicoFirebase() {
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getVendedorAsignadoId() {
        return vendedorAsignadoId;
    }

    public void setVendedorAsignadoId(String vendedorAsignadoId) {
        this.vendedorAsignadoId = vendedorAsignadoId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
