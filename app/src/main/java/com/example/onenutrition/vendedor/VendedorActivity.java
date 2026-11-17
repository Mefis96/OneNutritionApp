package com.example.onenutrition.vendedor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onenutrition.R;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import com.example.onenutrition.auth.ActividadProtegida;

public class VendedorActivity extends ActividadProtegida {

    private TextView navPedidos;
    private TextView navProductos;
    private TextView navPerfil;
    private TextView tvPendientes;
    private TextView tvPreparacion;
    private LinearLayout contenedorPedidosRecientes;
    private PedidoVendedorService pedidoVendedorService;
    private ListenerRegistration listenerPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendedor);
        ofrecerActivacionBiometrica();

        // NAVEGACIÓN
        navPedidos = findViewById(R.id.navPedidos);
        navProductos = findViewById(R.id.navProductos);
        navPerfil = findViewById(R.id.navPerfil);

        // INDICADORES
        tvPendientes = findViewById(R.id.tvPendientes);
        tvPreparacion = findViewById(R.id.tvPreparacion);

        // PEDIDOS RECIENTES
        contenedorPedidosRecientes = findViewById(R.id.contenedorPedidosRecientes);

        pedidoVendedorService = new PedidoVendedorService();

        // Abrir pedidos
        navPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(VendedorActivity.this, PedidosVendedorActivity.class);
            startActivity(intent);
        });

        // Abrir productos
        navProductos.setOnClickListener(v -> {
            Intent intent = new Intent(VendedorActivity.this, ProductosVendedorActivity.class);
            startActivity(intent);
        });

        // Abrir perfil
        navPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(VendedorActivity.this, PerfilVendedorActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        listenerPedidos = pedidoVendedorService.escucharPedidos(new PedidoVendedorService.PedidosCallback() {

            @Override
            public void onPedidosActualizados(List<PedidoPublicoFirebase> pedidos) {

                actualizarInicio(pedidos);
            }

            @Override
            public void onError(Exception error) {

                android.util.Log.e("ERROR_PEDIDOS_VENDEDOR", "Error al consultar pedidos", error);
                Toast.makeText(VendedorActivity.this, "Error al cargar pedidos: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

    private void actualizarInicio(List<PedidoPublicoFirebase> pedidos) {

        int pendientes = 0;
        int preparacion = 0;

        // CONTAR PEDIDOS POR ESTADO
        for (PedidoPublicoFirebase pedido : pedidos) {

            String estado = pedido.getEstado();

            if (estado == null) {
                continue;
            }

            if (estado.equalsIgnoreCase("Recibido") || estado.equalsIgnoreCase("Pendiente")) {

                pendientes++;
            }

            if (estado.equalsIgnoreCase("En preparación")) {
                preparacion++;
            }
        }

        // ACTUALIZAR INDICADORES
        tvPendientes.setText(pendientes + "\nPendientes");

        tvPreparacion.setText(preparacion + "\nEn preparación");

        // LIMPIAR PEDIDOS ANTERIORES
        contenedorPedidosRecientes.removeAllViews();

        if (pedidos.isEmpty()) {

            TextView mensaje = new TextView(this);
            mensaje.setText("No hay pedidos registrados.");
            mensaje.setTextSize(15);
            mensaje.setTextColor(Color.GRAY);
            contenedorPedidosRecientes.addView(mensaje);

            return;
        }

        // MOSTRAR LOS CINCO PEDIDOS MÁS RECIENTES
        int limite = Math.min(pedidos.size(), 5);

        for (int i = 0; i < limite; i++) {

            PedidoPublicoFirebase pedido = pedidos.get(i);
            TextView tarjeta = new TextView(this);
            String numero = pedido.getNumeroPedido() != null ? pedido.getNumeroPedido() : "Pedido sin número";
            String estado = pedido.getEstado() != null ? pedido.getEstado() : "Sin estado";
            String vendedorAsignado = pedido.getVendedorAsignadoId();
            String asignacion;

            if (vendedorAsignado == null || vendedorAsignado.isEmpty()) {

                asignacion = "Disponible";

            } else {

                asignacion = "Asignado";
            }

            tarjeta.setText(numero + "  •  " + estado + "\n" + asignacion);

            tarjeta.setTextColor(Color.parseColor("#374151"));
            tarjeta.setTextSize(16);
            tarjeta.setGravity(Gravity.CENTER_VERTICAL);
            tarjeta.setBackgroundColor(Color.WHITE);
            tarjeta.setPadding(dp(16), 0, dp(16), 0);
            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(70));
            parametros.bottomMargin = dp(8);
            tarjeta.setLayoutParams(parametros);
            tarjeta.setOnClickListener(v -> {

                Intent intent = new Intent(VendedorActivity.this, PedidosVendedorActivity.class);
                startActivity(intent);
            });

            contenedorPedidosRecientes.addView(tarjeta);
        }
    }

    private int dp(int valor) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics());
    }
}
