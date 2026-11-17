package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.onenutrition.auth.ActividadProtegida;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.adapter.ProductoAdapter;
import com.example.onenutrition.comprador.model.Producto;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CatalogoActivity extends ActividadProtegida {

    private RecyclerView recyclerProductos;
    private TextInputEditText etBuscarCatalogo;
    private Chip chipTodos;
    private Chip chipProteinas;
    private Chip chipCreatinas;
    private Chip chipVitaminas;
    private Chip chipHidratacion;
    private Chip chipPreentrenos;
    private ImageButton btnVolver;
    private ProductoAdapter productoAdapter;
    private final List<Producto> listaProductos = new ArrayList<>();
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerProductos;

    private String categoriaSeleccionada = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        recyclerProductos = findViewById(R.id.recyclerProductos);
        etBuscarCatalogo = findViewById(R.id.etBuscarCatalogo);
        btnVolver = findViewById(R.id.btnVolver);
        chipTodos = findViewById(R.id.chipTodos);
        chipProteinas = findViewById(R.id.chipProteinas);
        chipCreatinas = findViewById(R.id.chipCreatinas);
        chipVitaminas = findViewById(R.id.chipVitaminas);
        chipHidratacion = findViewById(R.id.chipHidratacion);
        chipPreentrenos = findViewById(R.id.chipPreentrenos);
        firestore = FirebaseFirestore.getInstance();
        btnVolver.setOnClickListener(v -> finish());
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));
        productoAdapter = new ProductoAdapter(new ArrayList<>());
        recyclerProductos.setAdapter(productoAdapter);

        configurarBuscador();
        configurarCategorias();

        String categoriaRecibida = getIntent().getStringExtra("categoria");

        if (categoriaRecibida != null) {
            seleccionarCategoria(categoriaRecibida);
        } else {
            chipTodos.setChecked(true);
        }

        String busquedaRecibida = getIntent().getStringExtra("busqueda");

        if (busquedaRecibida != null && !busquedaRecibida.trim().isEmpty()) {

            etBuscarCatalogo.setText(busquedaRecibida);
            etBuscarCatalogo.setSelection(etBuscarCatalogo.getText().length());
        }

        aplicarFiltros();
    }

    @Override
    protected void onStart() {

        super.onStart();
        cargarProductosFirestore();
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (listenerProductos != null) {
            listenerProductos.remove();
            listenerProductos = null;
        }
    }

    private void cargarProductosFirestore() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null || !usuario.isEmailVerified()) {

            Toast.makeText(this, "Debes iniciar sesión con un correo verificado", Toast.LENGTH_LONG).show();

            finish();
            return;
        }

        if (listenerProductos != null) {
            listenerProductos.remove();
            listenerProductos = null;
        }

        // Consultamos todos los productos publicados.
        listenerProductos = firestore.collection("productos").addSnapshotListener((documentos, error) -> {

            if (error != null) {

                Toast.makeText(this, "Error al cargar productos: " + error.getMessage(), Toast.LENGTH_LONG).show();

                return;
            }

            if (documentos == null) {
                return;
            }

            listaProductos.clear();

            for (DocumentSnapshot documento : documentos.getDocuments()) {

                Producto producto = new Producto();
                producto.setIdProducto(documento.getId());
                producto.setIdVendedor(documento.getString("idVendedor"));
                producto.setNombre(documento.getString("nombre"));
                producto.setCategoria(documento.getString("categoria"));
                producto.setDescripcion(documento.getString("descripcion"));
                Double precio = documento.getDouble("precio");
                producto.setPrecio(precio != null ? precio : 0);
                producto.setImagenUrl(documento.getString("imagenUrl"));
                producto.setImagen(R.drawable.proteina_whey);
                listaProductos.add(producto);
            }

            aplicarFiltros();
        });
    }

    private void configurarBuscador() {

        etBuscarCatalogo.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void configurarCategorias() {

        chipTodos.setOnClickListener(v -> filtrarPorCategoria("Todos"));
        chipProteinas.setOnClickListener(v -> filtrarPorCategoria("Proteínas"));
        chipCreatinas.setOnClickListener(v -> filtrarPorCategoria("Creatinas"));
        chipVitaminas.setOnClickListener(v -> filtrarPorCategoria("Vitaminas"));
        chipHidratacion.setOnClickListener(v -> filtrarPorCategoria("Hidratación"));
        chipPreentrenos.setOnClickListener(v -> filtrarPorCategoria("Preentrenos"));
    }

    private void filtrarPorCategoria(String categoria) {

        categoriaSeleccionada = categoria;
        aplicarFiltros();
    }

    private void aplicarFiltros() {

        List<Producto> productosFiltrados = new ArrayList<>();

        String busqueda = etBuscarCatalogo.getText() != null ? etBuscarCatalogo.getText().toString().trim().toLowerCase(Locale.ROOT) : "";

        for (Producto producto : listaProductos) {

            String nombre = producto.getNombre() != null ? producto.getNombre().toLowerCase(Locale.ROOT) : "";
            String categoria = producto.getCategoria() != null ? producto.getCategoria().toLowerCase(Locale.ROOT) : "";
            boolean coincideBusqueda = nombre.contains(busqueda) || categoria.contains(busqueda);
            boolean coincideCategoria = categoriaSeleccionada.equals("Todos") || categoria.equalsIgnoreCase(categoriaSeleccionada);
            if (coincideBusqueda && coincideCategoria) {
                productosFiltrados.add(producto);
            }
        }

        productoAdapter.actualizarLista(productosFiltrados);
    }

    private void seleccionarCategoria(String categoria) {

        switch (categoria) {

            case "Proteínas":
                chipProteinas.setChecked(true);
                categoriaSeleccionada = "Proteínas";
                break;

            case "Creatinas":
                chipCreatinas.setChecked(true);
                categoriaSeleccionada = "Creatinas";
                break;

            case "Vitaminas":
                chipVitaminas.setChecked(true);
                categoriaSeleccionada = "Vitaminas";
                break;

            case "Hidratación":
                chipHidratacion.setChecked(true);
                categoriaSeleccionada = "Hidratación";
                break;

            case "Preentrenos":
                chipPreentrenos.setChecked(true);
                categoriaSeleccionada = "Preentrenos";
                break;

            default:
                chipTodos.setChecked(true);
                categoriaSeleccionada = "Todos";
                break;
        }
    }
}
