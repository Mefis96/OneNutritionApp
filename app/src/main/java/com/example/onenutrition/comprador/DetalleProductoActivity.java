package com.example.onenutrition.comprador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.carrito.CarritoManager;
import com.example.onenutrition.comprador.model.ItemCarrito;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

import com.bumptech.glide.Glide;


public class DetalleProductoActivity extends ActividadProtegida {

    private ImageButton btnVolver;
    private ImageView imgDetalleProducto;
    private TextView tvDetalleNombre;
    private TextView tvDetalleCategoria;
    private TextView tvDetallePrecio;
    private TextView tvDetalleDescripcion;
    private TextView tvCantidad;
    private MaterialButton btnRestarCantidad;
    private MaterialButton btnSumarCantidad;
    private MaterialButton btnAgregarCarrito;
    private int cantidad = 1;

    // Datos del producto seleccionado
    private String nombre;
    private String categoria;
    private double precio;
    private String descripcion;
    private int imagen;
    private String imagenUri;
    private String imagenUrl;
    private String idProducto;
    private String idVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        btnVolver = findViewById(R.id.btnVolverDetalle);
        imgDetalleProducto = findViewById(R.id.imgDetalleProducto);
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre);
        tvDetalleCategoria = findViewById(R.id.tvDetalleCategoria);
        tvDetallePrecio = findViewById(R.id.tvDetallePrecio);
        tvDetalleDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvCantidad = findViewById(R.id.tvCantidad);
        btnRestarCantidad = findViewById(R.id.btnRestarCantidad);
        btnSumarCantidad = findViewById(R.id.btnSumarCantidad);
        btnAgregarCarrito = findViewById(R.id.btnAgregarCarrito);
        btnVolver.setOnClickListener(v -> finish());

        cargarProducto();
        configurarCantidad();
        configurarAgregarCarrito();
    }


    private void cargarProducto() {

        Intent intent = getIntent();

        idProducto = intent.getStringExtra("idProducto");
        idVendedor = intent.getStringExtra("idVendedor");
        nombre = intent.getStringExtra("nombre");
        categoria = intent.getStringExtra("categoria");
        descripcion = intent.getStringExtra("descripcion");
        precio = intent.getDoubleExtra("precio", 0);
        imagen = intent.getIntExtra("imagen", 0);
        imagenUri = intent.getStringExtra("imagenUri");
        imagenUrl = intent.getStringExtra("imagenUrl");
        tvDetalleNombre.setText(nombre);
        tvDetalleCategoria.setText(categoria);

        if (descripcion != null && !descripcion.isEmpty()) {

            tvDetalleDescripcion.setText(descripcion);

        } else {

            tvDetalleDescripcion.setText("Descripción no disponible.");
        }

        NumberFormat formatoPrecio = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        tvDetallePrecio.setText(formatoPrecio.format(precio));

        // Mostrar imagen del producto
        if (imagenUrl != null && !imagenUrl.isEmpty()) {

            // Fotografía almacenada en Cloudinary
            Glide.with(this).load(imagenUrl).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(imgDetalleProducto);

        } else if (imagenUri != null && !imagenUri.isEmpty()) {

            // Imagen seleccionada localmente
            Glide.with(this).load(Uri.parse(imagenUri)).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(imgDetalleProducto);

        } else {

            // Imagen de respaldo
            Glide.with(this).load(imagen != 0 ? imagen : R.drawable.proteina_whey).into(imgDetalleProducto);
        }
    }


    private void configurarCantidad() {

        btnSumarCantidad.setOnClickListener(v -> {
            cantidad++;
            tvCantidad.setText(String.valueOf(cantidad));
        });

        btnRestarCantidad.setOnClickListener(v -> {

            if (cantidad > 1) {
                cantidad--;
                tvCantidad.setText(String.valueOf(cantidad));
            }
        });
    }


    private void configurarAgregarCarrito() {

        btnAgregarCarrito.setOnClickListener(v -> {

            // Validar que el producto provenga de Firebase.
            if (idProducto == null || idProducto.trim().isEmpty() || idVendedor == null || idVendedor.trim().isEmpty()) {

                Toast.makeText(DetalleProductoActivity.this, "Este producto no está disponible para comprar. " + "Selecciónalo desde el catálogo.", Toast.LENGTH_LONG).show();

                return;
            }
            // Crear el producto con sus identificadores.
            ItemCarrito item = new ItemCarrito(idProducto, idVendedor, nombre, categoria, precio, imagen, imagenUrl, cantidad);
            // Agregar al carrito.
            CarritoManager.agregarProducto(item);
            Toast.makeText(DetalleProductoActivity.this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
            // Abrir el carrito.
            Intent intent = new Intent(DetalleProductoActivity.this, CarritoActivity.class);

            startActivity(intent);
        });
    }


}