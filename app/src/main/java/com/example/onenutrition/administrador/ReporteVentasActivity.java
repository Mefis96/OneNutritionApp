
package com.example.onenutrition.administrador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onenutrition.auth.ActividadProtegida;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class ReporteVentasActivity extends ActividadProtegida {

    private TextView tvVentasTotales;
    private TextView tvTotalPedidos;
    private TextView tvTicketPromedio;
    private TextView tvSinVentas;
    private RecyclerView recyclerReporteVentas;
    private TextView navInicio;
    private TextView navProductos;
    private TextView navUsuarios;
    private TextView navReportes;
    private FirebaseFirestore firestore;
    private final List<DocumentSnapshot> listaPedidos = new ArrayList<>();
    private ReporteVentaAdapter adapter;
    private MaterialButtonToggleGroup grupoFiltrosVentas;
    private final List<DocumentSnapshot> todosLosPedidos = new ArrayList<>();
    private int filtroSeleccionado = R.id.btnFiltroTodas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_ventas);
        firestore = FirebaseFirestore.getInstance();

        // COMPONENTES
        tvVentasTotales = findViewById(R.id.tvVentasTotalesReporte);
        tvTotalPedidos = findViewById(R.id.tvTotalPedidosReporte);
        tvTicketPromedio = findViewById(R.id.tvTicketPromedioReporte);
        tvSinVentas = findViewById(R.id.tvSinVentasReporte);
        recyclerReporteVentas =findViewById(R.id.recyclerReporteVentas);
        navInicio = findViewById(R.id.navAdminInicio);
        navProductos = findViewById(R.id.navAdminProductos);
        navUsuarios = findViewById(R.id.navAdminUsuarios);
        navReportes = findViewById(R.id.navAdminReportes);

        // CONFIGURAR RECYCLERVIEW
        recyclerReporteVentas.setLayoutManager( new LinearLayoutManager(this));
        recyclerReporteVentas.setNestedScrollingEnabled(false);
        recyclerReporteVentas.setClipToPadding(false);
        adapter = new ReporteVentaAdapter(listaPedidos);
        recyclerReporteVentas.setAdapter(adapter);
        grupoFiltrosVentas = findViewById(R.id.grupoFiltrosVentas);
        grupoFiltrosVentas.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

                    if (isChecked) {
                        filtroSeleccionado = checkedId;
                        aplicarFiltro();
                    }
                }
        );
        configurarNavegacion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReporteFirebase();
    }

    // CONSULTAR PEDIDOS REALES DE FIREBASE
    private void cargarReporteFirebase() {

        firestore.collection("pedidos")
                .get(Source.SERVER)
                .addOnSuccessListener(resultado -> {
                    todosLosPedidos.clear();
                    todosLosPedidos.addAll(resultado.getDocuments());
                    aplicarFiltro();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ReporteVentasActivity.this,"Error al cargar reporte: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void aplicarFiltro() {

        listaPedidos.clear();

        Calendar inicio = Calendar.getInstance();

        if (filtroSeleccionado == R.id.btnFiltroHoy) {

            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);
            inicio.set(Calendar.MILLISECOND, 0);

        } else if (filtroSeleccionado == R.id.btnFiltroMes) {

            inicio.set(Calendar.DAY_OF_MONTH, 1);
            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);
            inicio.set(Calendar.MILLISECOND, 0);
        }

        Date fechaInicio = inicio.getTime();
        Date fechaActual = new Date();

        double ventasTotales = 0;
        int pedidosPagados = 0;

        for (DocumentSnapshot documento : todosLosPedidos) {

            boolean incluir = true;

            if (filtroSeleccionado != R.id.btnFiltroTodas) {

                Timestamp fecha = documento.getTimestamp("fechaCreacion");

                if (fecha == null) {
                    incluir = false;
                } else {

                    Date fechaPedido = fecha.toDate();
                    incluir = !fechaPedido.before(fechaInicio)  && !fechaPedido.after(fechaActual);
                }
            }

            if (!incluir) {
                continue;
            }

            listaPedidos.add(documento);

            String estadoPago = documento.getString("estadoPago");

            if ("Pagado".equalsIgnoreCase(estadoPago)) {

                Number total = (Number) documento.get("total");

                if (total != null) {
                    ventasTotales += total.doubleValue();
                    pedidosPagados++;
                }
            }
        }

        double ticketPromedio = pedidosPagados > 0
                ? ventasTotales / pedidosPagados
                : 0;

        NumberFormat formato =NumberFormat.getCurrencyInstance(  new Locale("es", "CO"));

        tvVentasTotales.setText(formato.format(ventasTotales));
        tvTotalPedidos.setText(String.valueOf(listaPedidos.size()) );
        tvTicketPromedio.setText(formato.format(ticketPromedio));
        boolean hayPedidos = !listaPedidos.isEmpty();
        recyclerReporteVentas.setVisibility( hayPedidos ? View.VISIBLE : View.GONE);
        tvSinVentas.setVisibility(hayPedidos ? View.GONE : View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    // NAVEGACIÓN DEL ADMINISTRADOR
    private void configurarNavegacion() {

        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent( ReporteVentasActivity.this, AdministradorActivity.class);
            startActivity(intent);
            finish();
        });
        navProductos.setOnClickListener(v -> {
            Intent intent = new Intent(ReporteVentasActivity.this, ProductosAdministradorActivity.class);
            startActivity(intent);
            finish();
        });
        navUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(ReporteVentasActivity.this, UsuariosAdministradorActivity.class);
            startActivity(intent);
            finish();
        });
        navReportes.setOnClickListener(v -> {
        });
    }
}
