package com.example.onenutrition.vendedor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidosVendedorActivity extends ActividadProtegida {

    private LinearLayout contenedorPedidos;
    private String filtroActual = "Todos";
    private PedidoVendedorService pedidoVendedorService;
    private ListenerRegistration listenerPedidos;

    private final List<PedidoPublicoFirebase> pedidos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_vendedor);

        contenedorPedidos = findViewById(R.id.contenedorPedidos);
        Button btnTodos = findViewById(R.id.btnTodos);
        Button btnPendientes = findViewById(R.id.btnPendientes);
        Button btnPreparacion = findViewById(R.id.btnPreparacion);
        Button btnListos = findViewById(R.id.btnListos);

        pedidoVendedorService = new PedidoVendedorService();

        // FILTROS
        btnTodos.setOnClickListener(v -> {
            filtroActual = "Todos";
            mostrarPedidos();
        });

        btnPendientes.setOnClickListener(v -> {
            filtroActual = "Pendiente";
            mostrarPedidos();
        });

        btnPreparacion.setOnClickListener(v -> {
            filtroActual = "En preparación";
            mostrarPedidos();
        });

        btnListos.setOnClickListener(v -> {
            filtroActual = "Listo para entrega";
            mostrarPedidos();
        });

        // NAVEGACIÓN
        TextView navInicio = findViewById(R.id.navInicio);
        TextView navProductos = findViewById(R.id.navProductos);
        TextView navPerfil = findViewById(R.id.navPerfil);

        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, VendedorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navProductos.setOnClickListener(v -> {
            startActivity(new Intent(this, ProductosVendedorActivity.class));
            finish();
        });


        navPerfil.setOnClickListener(v -> {

            Intent intent = new Intent(PedidosVendedorActivity.this, PerfilVendedorActivity.class);

            startActivity(intent);
            finish();
        });


        mostrarPedidos();
    }

    @Override
    protected void onStart() {
        super.onStart();

        listenerPedidos = pedidoVendedorService.escucharPedidos(new PedidoVendedorService.PedidosCallback() {

            @Override
            public void onPedidosActualizados(List<PedidoPublicoFirebase> actualizados) {

                pedidos.clear();
                pedidos.addAll(actualizados);

                mostrarPedidos();
            }

            @Override
            public void onError(Exception error) {

                android.util.Log.e("ERROR_PEDIDOS_VENDEDOR", "Error al cargar gestión de pedidos", error);

                Toast.makeText(PedidosVendedorActivity.this, "Error al cargar pedidos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listenerPedidos != null) {
            listenerPedidos.remove();
            listenerPedidos = null;
        }
    }

    private void mostrarPedidos() {

        contenedorPedidos.removeAllViews();

        int cantidadMostrada = 0;

        for (PedidoPublicoFirebase pedido : pedidos) {

            String estado = pedido.getEstado();

            if (estado == null) {
                estado = "Sin estado";
            }

            // Aceptar Recibido como pendiente
            if (!filtroActual.equals("Todos")) {

                boolean coincide;

                if (filtroActual.equals("Pendiente")) {
                    coincide = estado.equalsIgnoreCase("Recibido") || estado.equalsIgnoreCase("Pendiente");
                } else {
                    coincide = estado.equalsIgnoreCase(filtroActual);
                }

                if (!coincide) {
                    continue;
                }
            }

            cantidadMostrada++;

            TextView tarjeta = new TextView(this);

            String total = NumberFormat.getNumberInstance(new Locale("es", "CO")).format(pedido.getTotal());

            String fecha = "Sin fecha";

            if (pedido.getFechaCreacion() != null) {
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "CO"));

                fecha = formato.format(pedido.getFechaCreacion().toDate());
            }

            String asignacion;

            if (pedido.getVendedorAsignadoId() == null || pedido.getVendedorAsignadoId().isEmpty()) {

                asignacion = "Disponible para tomar";

            } else {

                asignacion = "Asignado a un vendedor";
            }

            tarjeta.setText(pedido.getNumeroPedido() + "\n" + "Fecha: " + fecha + "\n" + estado + "  •  $" + total + "\n" + asignacion);

            tarjeta.setTextSize(15);
            tarjeta.setTextColor(Color.parseColor("#1F2937"));
            tarjeta.setBackgroundColor(Color.WHITE);
            tarjeta.setPadding(40, 28, 40, 28);
            tarjeta.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.bottomMargin = 20;
            tarjeta.setLayoutParams(params);

            tarjeta.setOnClickListener(v -> abrirDetalle(pedido));

            contenedorPedidos.addView(tarjeta);
        }

        if (cantidadMostrada == 0) {

            TextView mensaje = new TextView(this);

            mensaje.setText("No hay pedidos para mostrar.");
            mensaje.setTextSize(15);
            mensaje.setTextColor(Color.GRAY);
            mensaje.setPadding(16, 32, 16, 32);

            contenedorPedidos.addView(mensaje);
        }
    }

    private void abrirDetalle(PedidoPublicoFirebase pedido) {

        Intent intent = new Intent(this, DetallePedidoVendedorActivity.class);

        intent.putExtra("pedidoId", pedido.getPedidoId());
        intent.putExtra("numero", pedido.getNumeroPedido());
        intent.putExtra("vendedorAsignadoId", pedido.getVendedorAsignadoId());
        // Datos privados: todavía no disponibles
        intent.putExtra("cliente", "Datos protegidos");
        intent.putExtra("telefono", "No disponible");
        intent.putExtra("direccion", "No disponible");
        intent.putExtra("productos", "Pendiente de asignación");
        intent.putExtra("metodoPago", "No disponible");
        String total = NumberFormat.getNumberInstance(new Locale("es", "CO")).format(pedido.getTotal());
        intent.putExtra("total", "$" + total);
        intent.putExtra("estado", pedido.getEstado());

        startActivity(intent);
    }
}
