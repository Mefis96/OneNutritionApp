package com.example.onenutrition.comprador.carrito;

import com.example.onenutrition.comprador.model.ItemCarrito;
import java.util.ArrayList;
import java.util.List;

public class CarritoManager {

    private static final List<ItemCarrito> items = new ArrayList<>();

    private CarritoManager() {
    }


    public static void agregarProducto(ItemCarrito nuevoItem) {

        // Verificar que el producto sea válido.
        if (nuevoItem == null || nuevoItem.getCantidad() <= 0) {
            return;
        }

        // Solo aceptar productos identificados en Firebase.
        if (nuevoItem.getIdProducto() == null || nuevoItem.getIdProducto().trim().isEmpty() || nuevoItem.getIdVendedor() == null || nuevoItem.getIdVendedor().trim().isEmpty()) {

            android.util.Log.w("CarritoManager", "Producto rechazado: faltan identificadores de Firebase");

            return;
        }

        // Buscar si el producto ya está en el carrito.
        for (ItemCarrito item : items) {

            if (nuevoItem.getIdProducto().equals(item.getIdProducto())) {

                // Si ya existe, aumentar su cantidad.
                item.setCantidad(item.getCantidad() + nuevoItem.getCantidad());

                return;
            }
        }

        // Si no existe, agregarlo al carrito.
        items.add(nuevoItem);
    }


    public static List<ItemCarrito> getItems() {
        return items;
    }

    public static void eliminarProducto(ItemCarrito item) {
        items.remove(item);
    }

    public static double calcularSubtotal() {

        double subtotal = 0;

        for (ItemCarrito item : items) {
            subtotal += item.getSubtotal();
        }

        return subtotal;
    }

    public static void vaciarCarrito() {
        items.clear();
    }
}
