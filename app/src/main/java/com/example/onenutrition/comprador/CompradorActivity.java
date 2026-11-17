package com.example.onenutrition.comprador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.Producto;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import android.widget.TextView;
import android.widget.Toast;

import com.example.onenutrition.comprador.perfil.PerfilManager;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onenutrition.comprador.adapter.ProductoDestacadoAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import com.example.onenutrition.auth.ActividadProtegida;


public class CompradorActivity extends ActividadProtegida {

    private BottomNavigationView bottomNavigation;
    private View cardProteinas;
    private View cardCreatinas;
    private View cardVitaminas;
    private View cardHidratacion;
    private View cardPreentrenos;
    private View cardWheyProtein;
    private View cardCreatina;
    private View btnAddWhey;
    private View btnAddCreatina;
    private TextInputEditText etBuscar;
    private TextView tvSaludoComprador;
    private RecyclerView rvProductosDestacados;
    private ProductoDestacadoAdapter destacadoAdapter;
    private final List<Producto> listaDestacados = new ArrayList<>();
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerDestacados;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprador);
        ofrecerActivacionBiometrica();
        findViewById(R.id.tvViewAllCategories).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogoActivity.class));
        });

        findViewById(R.id.tvViewAllProducts).setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogoActivity.class));
        });

        bottomNavigation = findViewById(R.id.bottomNavigation);
        cardProteinas = findViewById(R.id.cardProteinas);
        cardCreatinas = findViewById(R.id.cardCreatinas);
        cardVitaminas = findViewById(R.id.cardVitaminas);
        cardHidratacion = findViewById(R.id.cardHidratacion);
        rvProductosDestacados = findViewById(R.id.rvProductosDestacados);
        destacadoAdapter = new ProductoDestacadoAdapter(new ArrayList<>());
        rvProductosDestacados.setAdapter(destacadoAdapter);
        firestore = FirebaseFirestore.getInstance();
        rvProductosDestacados.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvProductosDestacados.setNestedScrollingEnabled(false);
        cardPreentrenos = findViewById(R.id.cardPreentrenos);
        etBuscar = findViewById(R.id.etSearchProducts);
        tvSaludoComprador = findViewById(R.id.tvSaludoComprador);

        configurarNavegacion();
        configurarCategorias();
        configurarBuscador();
    }


    private void cargarProductosDestacados() {

        if (listenerDestacados != null) {
            listenerDestacados.remove();
            listenerDestacados = null;
        }

        listenerDestacados = firestore.collection("productos").addSnapshotListener((documentos, error) -> {

            if (error != null) {
                Toast.makeText(this, "Error al cargar productos destacados", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentos == null) {
                return;
            }

            listaDestacados.clear();

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
                listaDestacados.add(producto);
            }

            destacadoAdapter.actualizarLista(listaDestacados);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarProductosDestacados();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (listenerDestacados != null) {
            listenerDestacados.remove();
            listenerDestacados = null;
        }
    }


    private void cargarSaludoComprador() {

        String nombreCompleto = PerfilManager.getNombre();

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {

            tvSaludoComprador.setText("¡Hola!");
            return;
        }

        // Mostrar únicamente el primer nombre.
        String primerNombre = nombreCompleto.trim().split("\\s+")[0];

        tvSaludoComprador.setText("¡Hola, " + primerNombre + "!");
    }


    // Configura la navegación entre las diferentes pantallas mediante los botones del menú.
    private void configurarNavegacion() {

        bottomNavigation.setSelectedItemId(R.id.nav_inicio);
        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                return true;
            }

            if (id == R.id.nav_buscar) {
                startActivity(new Intent(CompradorActivity.this, CatalogoActivity.class));
                return true;
            }

            if (id == R.id.nav_carrito) {
                startActivity(new Intent(CompradorActivity.this, CarritoActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_pedidos) {
                startActivity(new Intent(CompradorActivity.this, MisPedidosActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_perfil) {

                Intent intent = new Intent(CompradorActivity.this, PerfilCompradorActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void configurarCategorias() {

        cardProteinas.setOnClickListener(v -> abrirCatalogo("Proteínas"));
        cardCreatinas.setOnClickListener(v -> abrirCatalogo("Creatinas"));
        cardVitaminas.setOnClickListener(v -> abrirCatalogo("Vitaminas"));
        cardHidratacion.setOnClickListener(v -> abrirCatalogo("Hidratación"));
        cardPreentrenos.setOnClickListener(v -> abrirCatalogo("Preentrenos"));
    }

    private void abrirCatalogo(String categoria) {

        Intent intent = new Intent(CompradorActivity.this, CatalogoActivity.class);
        intent.putExtra("categoria", categoria);
        startActivity(intent);
    }


    private void configurarBuscador() {

        etBuscar.setOnEditorActionListener((v, actionId, event) -> {

            String textoBusqueda = etBuscar.getText().toString().trim();

            if (!textoBusqueda.isEmpty()) {
                Intent intent = new Intent(CompradorActivity.this, CatalogoActivity.class);
                intent.putExtra("busqueda", textoBusqueda);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        cargarSaludoComprador();

        bottomNavigation.setSelectedItemId(R.id.nav_inicio);
    }
}