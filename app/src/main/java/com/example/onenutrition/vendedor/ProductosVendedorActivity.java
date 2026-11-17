package com.example.onenutrition.vendedor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onenutrition.auth.ActividadProtegida;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.Producto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductosVendedorActivity extends ActividadProtegida {

    private TextInputEditText etBuscarProducto;
    private MaterialButton btnNuevoProducto;
    private RecyclerView recyclerProductos;
    private ProductoVendedorAdapter adapter;
    private final List<Producto> listaProductos = new ArrayList<>();
    private final List<Producto> listaFiltrada = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos_vendedor);

        etBuscarProducto = findViewById(R.id.etBuscarProductoVendedor);
        btnNuevoProducto = findViewById(R.id.btnNuevoProducto);
        recyclerProductos = findViewById(R.id.recyclerProductosVendedor);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        configurarRecyclerView();
        configurarBuscador();
        configurarNavegacion();
        btnNuevoProducto.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosVendedorActivity.this, CrearProductoVendedorActivity.class);

            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {

        super.onStart();
        escucharProductosFirestore();
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (listenerProductos != null) {
            listenerProductos.remove();
            listenerProductos = null;
        }
    }

    private void escucharProductosFirestore() {

        FirebaseUser usuario = firebaseAuth.getCurrentUser();

        if (usuario == null || !usuario.isEmailVerified()) {
            Toast.makeText(this, "Debes iniciar sesión con un correo verificado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (listenerProductos != null) {
            listenerProductos.remove();
            listenerProductos = null;
        }

        String uidVendedor = usuario.getUid();

        listenerProductos = firestore.collection("productos").whereEqualTo("idVendedor", uidVendedor).addSnapshotListener((documentos, error) -> {

            if (error != null) {

                Toast.makeText(this, "Error al cargar productos: " + error.getMessage(), Toast.LENGTH_LONG).show();

                return;
            }

            if (documentos == null) {
                return;
            }

            listaProductos.clear();

            for (com.google.firebase.firestore.DocumentSnapshot documento : documentos.getDocuments()) {

                Producto producto = new Producto();
                producto.setIdProducto(documento.getId());
                producto.setIdVendedor(documento.getString("idVendedor"));
                producto.setNombre(documento.getString("nombre"));
                producto.setCategoria(documento.getString("categoria"));
                producto.setDescripcion(documento.getString("descripcion"));
                Double precio = documento.getDouble("precio");
                producto.setPrecio(precio != null ? precio : 0);
                producto.setImagenUrl(documento.getString("imagenUrl"));

                // Imagen de respaldo del proyecto.
                producto.setImagen(R.drawable.proteina_whey);

                listaProductos.add(producto);
            }

            filtrarProductos(etBuscarProducto.getText() != null ? etBuscarProducto.getText().toString() : "");
        });
    }

    private void configurarRecyclerView() {

        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductoVendedorAdapter(listaFiltrada, new ProductoVendedorAdapter.OnProductoActionListener() {


            @Override
            public void onEditar(Producto producto) {

                Intent intent = new Intent(ProductosVendedorActivity.this, EditarProductoVendedorActivity.class);

                intent.putExtra("idProducto", producto.getIdProducto());
                intent.putExtra("idVendedor", producto.getIdVendedor());
                intent.putExtra("nombreProducto", producto.getNombre());
                intent.putExtra("categoriaProducto", producto.getCategoria());
                intent.putExtra("descripcionProducto", producto.getDescripcion());
                intent.putExtra("precioProducto", producto.getPrecio());
                intent.putExtra("imagenUrlProducto", producto.getImagenUrl());
                startActivity(intent);
            }


            @Override
            public void onEliminar(Producto producto) {
                confirmarEliminacion(producto);
            }
        });

        recyclerProductos.setAdapter(adapter);
    }

    private void configurarBuscador() {

        etBuscarProducto.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filtrarProductos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filtrarProductos(String texto) {

        listaFiltrada.clear();

        String busqueda = texto.trim().toLowerCase(Locale.ROOT);

        for (Producto producto : listaProductos) {

            String nombre = producto.getNombre() != null ? producto.getNombre().toLowerCase(Locale.ROOT) : "";
            String categoria = producto.getCategoria() != null ? producto.getCategoria().toLowerCase(Locale.ROOT) : "";
            if (busqueda.isEmpty() || nombre.contains(busqueda) || categoria.contains(busqueda)) {

                listaFiltrada.add(producto);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void configurarNavegacion() {

        TextView navInicio = findViewById(R.id.navInicio);
        TextView navPedidos = findViewById(R.id.navPedidos);
        TextView navPerfil = findViewById(R.id.navPerfil);
        navInicio.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosVendedorActivity.this, VendedorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navPedidos.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosVendedorActivity.this, PedidosVendedorActivity.class);
            startActivity(intent);
            finish();
        });

        navPerfil.setOnClickListener(v -> {

            Intent intent = new Intent(ProductosVendedorActivity.this, PerfilVendedorActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void confirmarEliminacion(Producto producto) {

        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Eliminar producto").setMessage("¿Estás seguro de que deseas eliminar " + producto.getNombre() + "?").setNegativeButton("Cancelar", null).setPositiveButton("Eliminar", (dialog, which) -> {

            FirebaseUser usuario = firebaseAuth.getCurrentUser();

            if (usuario == null || !usuario.isEmailVerified() || !usuario.getUid().equals(producto.getIdVendedor()) || producto.getIdProducto() == null || producto.getIdProducto().isEmpty()) {

                Toast.makeText(this, "No tienes permiso para eliminar este producto", Toast.LENGTH_LONG).show();

                return;
            }

            firestore.collection("productos").document(producto.getIdProducto()).delete().addOnSuccessListener(unused -> {

                Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {

                Toast.makeText(this, "Error al eliminar producto: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).show();
    }

}
