package com.example.onenutrition.administrador;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.Producto;
import com.example.onenutrition.vendedor.CrearProductoVendedorActivity;
import com.example.onenutrition.vendedor.EditarProductoVendedorActivity;
import com.example.onenutrition.vendedor.ProductoManager;
import com.example.onenutrition.vendedor.ProductoVendedorAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import com.example.onenutrition.auth.ActividadProtegida;

public class ProductosAdministradorActivity extends ActividadProtegida {

    private static final int REQUEST_CREAR_PRODUCTO = 300;
    private static final int REQUEST_EDITAR_PRODUCTO = 301;
    private Producto productoEditando;
    private TextInputEditText etBuscarProducto;
    private MaterialButton btnNuevoProducto;
    private RecyclerView recyclerProductos;
    private ProductoVendedorAdapter adapter;
    private List<Producto> listaProductos;
    private List<Producto> listaFiltrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos_administrador);

        etBuscarProducto = findViewById(R.id.etBuscarProductoAdmin);
        btnNuevoProducto = findViewById(R.id.btnNuevoProductoAdmin);
        recyclerProductos = findViewById(R.id.recyclerProductosAdmin);

        cargarProductos();
        configurarRecyclerView();
        configurarBuscador();
        configurarNavegacion();

        btnNuevoProducto.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosAdministradorActivity.this, CrearProductoVendedorActivity.class);

            startActivityForResult(intent, REQUEST_CREAR_PRODUCTO);
        });
    }

    private void cargarProductos() {

        listaProductos = ProductoManager.obtenerProductos();
        listaFiltrada = new ArrayList<>(listaProductos);
    }

    private void configurarRecyclerView() {

        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductoVendedorAdapter(listaFiltrada, new ProductoVendedorAdapter.OnProductoActionListener() {

                    @Override
                    public void onEditar(Producto producto) {

                        productoEditando = producto;

                        Intent intent = new Intent(ProductosAdministradorActivity.this, EditarProductoVendedorActivity.class);
                        intent.putExtra("nombreProducto", producto.getNombre());
                        intent.putExtra("categoriaProducto", producto.getCategoria());
                        intent.putExtra("descripcionProducto", producto.getDescripcion());
                        intent.putExtra("precioProducto", producto.getPrecio());
                        intent.putExtra("imagenProducto", producto.getImagen());
                        intent.putExtra("imagenUriProducto", producto.getImagenUri());

                        startActivityForResult(intent, REQUEST_EDITAR_PRODUCTO);
                    }

                    @Override
                    public void onEliminar(Producto producto) {

                        confirmarEliminacion(producto);
                    }
                }
        );

        recyclerProductos.setAdapter(adapter);
    }

    private void configurarBuscador() {

        etBuscarProducto.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        filtrarProductos(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    private void filtrarProductos(String texto) {

        listaFiltrada.clear();

        String busqueda = texto.trim().toLowerCase();

        if (busqueda.isEmpty()) {

            listaFiltrada.addAll(listaProductos);

        } else {

            for (Producto producto : listaProductos) {

                if (producto.getNombre().toLowerCase().contains(busqueda) || producto.getCategoria().toLowerCase().contains(busqueda)) {

                    listaFiltrada.add(producto);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void configurarNavegacion() {

        TextView navInicio = findViewById(R.id.navAdminInicio);
        TextView navProductos = findViewById(R.id.navAdminProductos);
        TextView navUsuarios = findViewById(R.id.navAdminUsuarios);
        TextView navReportes = findViewById(R.id.navAdminReportes);

        // INICIO
        navInicio.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosAdministradorActivity.this, AdministradorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // PRODUCTOS
        navProductos.setOnClickListener(v -> {

        });

        // USUARIOS
        navUsuarios.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosAdministradorActivity.this, UsuariosAdministradorActivity.class);
            startActivity(intent);
            finish();
        });

        // REPORTES
        navReportes.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosAdministradorActivity.this, ReporteVentasActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // CREAR PRODUCTO
        if (requestCode == REQUEST_CREAR_PRODUCTO && resultCode == RESULT_OK && data != null) {

            String nombre = data.getStringExtra("nombreProducto");
            String categoria = data.getStringExtra("categoriaProducto");
            String descripcion = data.getStringExtra("descripcionProducto");
            double precio = data.getDoubleExtra("precioProducto", 0);
            String imagenUri = data.getStringExtra("imagenUriProducto");
            Producto nuevoProducto = new Producto(nombre, categoria, descripcion, precio, R.drawable.proteina_whey);
            nuevoProducto.setImagenUri(imagenUri);
            ProductoManager.agregarProducto(nuevoProducto);
            actualizarLista();
        }

        // EDITAR PRODUCTO
        if (requestCode == REQUEST_EDITAR_PRODUCTO && resultCode == RESULT_OK && data != null && productoEditando != null) {

            String nombre = data.getStringExtra("nombreProducto");
            String categoria = data.getStringExtra("categoriaProducto");
            String descripcion = data.getStringExtra("descripcionProducto");
            double precio = data.getDoubleExtra("precioProducto", 0);
            int imagen = data.getIntExtra("imagenProducto", productoEditando.getImagen());
            String imagenUri = data.getStringExtra("imagenUriProducto");
            productoEditando.setNombre(nombre);
            productoEditando.setCategoria(categoria);
            productoEditando.setDescripcion(descripcion);
            productoEditando.setPrecio(precio);
            productoEditando.setImagen(imagen);
            productoEditando.setImagenUri(imagenUri);
            actualizarLista();
            productoEditando = null;
        }
    }

    private void actualizarLista() {

        etBuscarProducto.setText("");

        listaFiltrada.clear();

        listaFiltrada.addAll(listaProductos);

        adapter.notifyDataSetChanged();
    }

    private void confirmarEliminacion(Producto producto) {

        new AlertDialog.Builder(this).setTitle("Eliminar producto").setMessage("¿Estás seguro de que deseas eliminar " + producto.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {

                            ProductoManager.eliminarProducto(producto);
                            listaFiltrada.remove(producto);

                            adapter.notifyDataSetChanged();

                            android.widget.Toast.makeText(this, "Producto eliminado correctamente", android.widget.Toast.LENGTH_SHORT).show();

                        }).setNegativeButton("Cancelar", null).show();
    }
}