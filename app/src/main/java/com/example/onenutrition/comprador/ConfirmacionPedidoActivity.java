package com.example.onenutrition.comprador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.carrito.CarritoManager;
import com.example.onenutrition.comprador.model.ItemCarrito;
import com.example.onenutrition.comprador.model.PedidoFirebase;
import com.example.onenutrition.comprador.model.ProductoPedido;
import com.example.onenutrition.comprador.pedido.PedidoFirebaseService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ConfirmacionPedidoActivity extends ActividadProtegida {

    private TextView tvMetodoConfirmacion;
    private TextView tvTotalConfirmacion;
    private TextView tvNumeroPedido;
    private MaterialButton btnVolverInicio;

    private double totalPedido;
    private String idPedido;
    private boolean pedidoConfirmado = false;
    private boolean guardandoPedido = false;

    private PedidoFirebaseService pedidoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion_pedido);

        tvMetodoConfirmacion = findViewById(R.id.tvMetodoConfirmacion);
        tvTotalConfirmacion = findViewById(R.id.tvTotalConfirmacion);
        tvNumeroPedido = findViewById(R.id.tvNumeroPedido);
        btnVolverInicio = findViewById(R.id.btnVolverInicioConfirmacion);
        pedidoService = new PedidoFirebaseService();
        btnVolverInicio.setEnabled(false);
        btnVolverInicio.setOnClickListener(v -> volverInicio());
        String metodoPago = getIntent().getStringExtra("metodoPago");

        if (metodoPago == null) {
            metodoPago = "No especificado";
        }

        if (savedInstanceState != null) {
            idPedido = savedInstanceState.getString("idPedido");
            pedidoConfirmado = savedInstanceState.getBoolean("pedidoConfirmado", false);
        }

        if (idPedido == null) {
            idPedido = UUID.randomUUID().toString();
        }

        totalPedido = CarritoManager.calcularSubtotal();
        tvMetodoConfirmacion.setText("Método de pago: " + metodoPago);
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        tvTotalConfirmacion.setText(formato.format(totalPedido));

        if (pedidoConfirmado) {
            mostrarConfirmacion();
        } else {
            tvNumeroPedido.setText("Registrando pedido...");
            registrarPedido(metodoPago);
        }
    }

    private void registrarPedido(String metodoPago) {

        if (guardandoPedido) {
            return;
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            mostrarError("Debes iniciar sesión para comprar.");
            return;
        }

        if (CarritoManager.getItems().isEmpty()) {
            mostrarError("El carrito está vacío.");
            return;
        }


        for (ItemCarrito item : CarritoManager.getItems()) {

            if (item.getIdProducto() == null || item.getIdProducto().trim().isEmpty() || item.getIdVendedor() == null || item.getIdVendedor().trim().isEmpty()) {

                mostrarError("El carrito contiene productos sin identificar. " + "Elimínalos y agrégalos nuevamente desde el catálogo.");

                return;
            }
        }


        String direccion = getIntent().getStringExtra("direccion");
        String barrio = getIntent().getStringExtra("barrio");
        String ciudad = getIntent().getStringExtra("ciudad");
        String indicaciones = getIntent().getStringExtra("indicaciones");

        if (direccion == null || direccion.trim().isEmpty() || barrio == null || barrio.trim().isEmpty() || ciudad == null || ciudad.trim().isEmpty()) {

            mostrarError("Faltan los datos de entrega.");
            return;
        }

        Double latitud = getIntent().hasExtra("latitud") ? getIntent().getDoubleExtra("latitud", 0) : null;
        Double longitud = getIntent().hasExtra("longitud") ? getIntent().getDoubleExtra("longitud", 0) : null;
        List<ProductoPedido> productos = new ArrayList<>();

        for (ItemCarrito item : CarritoManager.getItems()) {
            productos.add(new ProductoPedido(item));
        }

        PedidoFirebase pedido = new PedidoFirebase(usuario.getUid(), metodoPago, direccion, barrio, ciudad, indicaciones, latitud, longitud, totalPedido, productos);

        guardandoPedido = true;
        pedidoService.guardarPedido(idPedido, pedido, new PedidoFirebaseService.PedidoCallback() {

            @Override
            public void onExito(String idRegistrado) {

                guardandoPedido = false;
                pedidoConfirmado = true;

                CarritoManager.vaciarCarrito();

                mostrarConfirmacion();
            }

            @Override
            public void onError(Exception error) {

                guardandoPedido = false;
                android.util.Log.e("ERROR_PEDIDO", "Falló el registro del pedido en Firestore", error);

                // Si se perdió la respuesta, comprobar si
                // el documento llegó a guardarse.
                FirebaseFirestore.getInstance().collection("pedidos").document(idPedido).get().addOnSuccessListener(documento -> {

                    if (documento.exists() && usuario.getUid().equals(documento.getString("compradorId"))) {

                        pedidoConfirmado = true;
                        CarritoManager.vaciarCarrito();
                        mostrarConfirmacion();

                    } else {
                        mostrarError("No se pudo registrar el pedido: " + error.getMessage());
                    }
                }).addOnFailureListener(e -> mostrarError("No se pudo verificar el pedido: " + error.getMessage()));
            }
        });
    }


    private void mostrarConfirmacion() {

        tvNumeroPedido.setText("Consultando número de pedido...");
        btnVolverInicio.setEnabled(false);
        FirebaseFirestore.getInstance().collection("pedidos").document(idPedido).get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(documento -> {

            String numeroPedido = documento.getString("numeroPedido");

            if (documento.exists() && numeroPedido != null && !numeroPedido.trim().isEmpty()) {

                tvNumeroPedido.setText("Pedido #" + numeroPedido);

            } else {

                tvNumeroPedido.setText("Pedido registrado - Número no disponible");
            }

            btnVolverInicio.setEnabled(true);
        }).addOnFailureListener(error -> {

            tvNumeroPedido.setText("Pedido registrado - No se pudo consultar el número");
            btnVolverInicio.setEnabled(true);
            Toast.makeText(this, "No se pudo consultar el número: " + error.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    private void mostrarError(String mensaje) {

        tvNumeroPedido.setText("Pedido no confirmado");
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        // Volver para corregir el problema sin vaciar el carrito.
        btnVolverInicio.setEnabled(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("idPedido", idPedido);
        outState.putBoolean("pedidoConfirmado", pedidoConfirmado);
        super.onSaveInstanceState(outState);
    }

    private void volverInicio() {

        Intent intent = new Intent(ConfirmacionPedidoActivity.this, CompradorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
