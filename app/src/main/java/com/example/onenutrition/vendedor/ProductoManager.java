package com.example.onenutrition.vendedor;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.Producto;
import java.util.ArrayList;
import java.util.List;

public class ProductoManager {

    private static final List<Producto> productos = new ArrayList<>();

    // Evita cargar los productos iniciales más de una vez.
    static {

        productos.add(new Producto(
                "Whey Protein",
                "Proteínas",
                "Proteína de suero de leche diseñada para complementar la ingesta diaria de proteína y apoyar la recuperación y el desarrollo muscular.",
                149900,
                R.drawable.proteina_whey
        ));

        productos.add(new Producto(
                "Creatina Monohidratada",
                "Creatinas",
                "Creatina monohidratada para complementar el rendimiento durante ejercicios de alta intensidad y apoyar el desarrollo de fuerza y potencia.",
                89900,
                R.drawable.creatina_monohidratada
        ));

        productos.add(new Producto(
                "Multivitamínico",
                "Vitaminas",
                "Complemento nutricional con vitaminas y minerales diseñado para apoyar la ingesta diaria de micronutrientes.",
                59900,
                R.drawable.multivitaminico
        ));

        productos.add(new Producto(
                "Electrolitos",
                "Hidratación",
                "Suplemento con electrolitos diseñado para complementar la hidratación y ayudar a reponer minerales durante o después de la actividad física.",
                44900,
                R.drawable.electrolitos
        ));

        productos.add(new Producto(
                "Pre-Workout",
                "Preentrenos",
                "Suplemento preentrenamiento formulado para complementar la preparación antes de la actividad física y apoyar el rendimiento durante el entrenamiento.",
                119900,
                R.drawable.preentreno
        ));
    }

    public static List<Producto> obtenerProductos() {
        return productos;
    }

    public static void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public static void eliminarProducto(Producto producto) {
        productos.remove(producto);
    }
}