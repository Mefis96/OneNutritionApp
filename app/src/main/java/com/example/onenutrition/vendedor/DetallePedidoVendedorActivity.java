package com.example.onenutrition.vendedor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onenutrition.auth.ActividadProtegida;
import androidx.appcompat.app.AlertDialog;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.PedidoFirebase;
import com.example.onenutrition.comprador.model.ProductoPedido;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.NumberFormat;
import java.util.Locale;

public class DetallePedidoVendedorActivity extends ActividadProtegida {

    private TextView tvNumeroPedido;
    private TextView tvEstadoPedido;
    private TextView tvCliente;
    private TextView tvDireccion;
    private TextView tvProductos;
    private TextView tvMetodoPago;
    private TextView tvTotal;
    private TextView btnVolver;
    private Button btnCambiarEstado;
    private String pedidoId;
    private String vendedorActualId;
    private String vendedorAsignadoId;
    private boolean operacionEnCurso = false;
    private ListenerRegistration listenerPublico;
    private ListenerRegistration listenerOriginal;
    private AsignacionPedidoService asignacionService;
    private EstadoPedidoService estadoPedidoService;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pedido_vendedor);

        tvNumeroPedido = findViewById(R.id.tvNumeroPedido);
        tvEstadoPedido = findViewById(R.id.tvEstadoPedido);
        tvCliente = findViewById(R.id.tvCliente);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvProductos = findViewById(R.id.tvProductos);
        tvMetodoPago = findViewById(R.id.tvMetodoPago);
        tvTotal = findViewById(R.id.tvTotal);
        btnVolver = findViewById(R.id.btnVolver);
        btnCambiarEstado = findViewById(R.id.btnCambiarEstado);
        asignacionService = new AsignacionPedidoService();
        estadoPedidoService = new EstadoPedidoService();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(this, "No hay un vendedor autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        vendedorActualId = usuario.getUid();
        pedidoId = getIntent().getStringExtra("pedidoId");

        if (pedidoId == null || pedidoId.trim().isEmpty()) {
            Toast.makeText(this, "No se recibió el identificador del pedido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvNumeroPedido.setText("Pedido " + getIntent().getStringExtra("numero"));

        tvEstadoPedido.setText(getIntent().getStringExtra("estado"));

        tvTotal.setText("Total: " + getIntent().getStringExtra("total"));

        mostrarDatosProtegidos();
        btnVolver.setOnClickListener(v -> finish());
        btnCambiarEstado.setOnClickListener(v -> {

            if (vendedorAsignadoId == null) {
                tomarPedido();
            } else if (vendedorActualId.equals(vendedorAsignadoId)) {
                cambiarEstadoPedido();
            }
        });
    }

    private void cambiarEstadoPedido() {

        if (operacionEnCurso || !vendedorActualId.equals(vendedorAsignadoId)) {
            return;
        }

        String estadoActual = tvEstadoPedido.getText().toString();
        String nuevoEstado;

        switch (estadoActual) {

            case "Recibido":
                nuevoEstado = "En preparación";
                break;

            case "En preparación":
                nuevoEstado = "Listo para entrega";
                break;

            case "Listo para entrega":
                confirmarEntregaYPago();
                return;

            default:
                return;
        }

        operacionEnCurso = true;
        actualizarBoton(estadoActual);
        estadoPedidoService.cambiarEstado(pedidoId, nuevoEstado, new EstadoPedidoService.EstadoCallback() {

            @Override
            public void onExito() {

                operacionEnCurso = false;

                Toast.makeText(DetallePedidoVendedorActivity.this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();

                // El listener de Firestore actualizará
                // automáticamente el estado mostrado.
                actualizarBoton(tvEstadoPedido.getText().toString());
            }

            @Override
            public void onError(Exception error) {

                operacionEnCurso = false;

                android.util.Log.e("ERROR_ESTADO_PEDIDO", "No se pudo actualizar el estado", error);
                actualizarBoton(tvEstadoPedido.getText().toString());
                Toast.makeText(DetallePedidoVendedorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void confirmarEntregaYPago() {

        if (operacionEnCurso || !vendedorActualId.equals(vendedorAsignadoId) || !"Listo para entrega".equals(tvEstadoPedido.getText().toString())) {
            return;
        }

        new AlertDialog.Builder(this).setTitle("Confirmar entrega y pago").setMessage("¿Confirmas que entregaste los productos " + "al comprador y recibiste el pago " + "completo en efectivo?\n\n" + "Esta acción marcará el pedido como " + "ENTREGADO y el pago como PAGADO.").setNegativeButton("CANCELAR", (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton("CONFIRMAR", (dialog, which) -> {

            operacionEnCurso = true;

            actualizarBoton("Listo para entrega");
            estadoPedidoService.cambiarEstado(pedidoId, "Entregado", new EstadoPedidoService.EstadoCallback() {

                @Override
                public void onExito() {

                    operacionEnCurso = false;

                    Toast.makeText(DetallePedidoVendedorActivity.this, "Pedido entregado y pago registrado", Toast.LENGTH_LONG).show();

                    // El listener actualizará el estado.
                    actualizarBoton(tvEstadoPedido.getText().toString());
                }

                @Override
                public void onError(Exception error) {

                    operacionEnCurso = false;

                    android.util.Log.e("ERROR_ENTREGA_PEDIDO", "No se pudo confirmar la entrega", error);
                    actualizarBoton(tvEstadoPedido.getText().toString());
                    Toast.makeText(DetallePedidoVendedorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }).show();
    }


    @Override
    protected void onStart() {
        super.onStart();

        DocumentReference referenciaPublica = db.collection("pedidosPublicos").document(pedidoId);

        listenerPublico = referenciaPublica.addSnapshotListener((documento, error) -> {

            if (error != null) {
                android.util.Log.e("ERROR_DETALLE_PEDIDO", "Error consultando pedido público", error);
                return;
            }

            if (documento == null || !documento.exists()) {
                btnCambiarEstado.setEnabled(false);
                btnCambiarEstado.setText("PEDIDO NO DISPONIBLE");
                detenerConsultaOriginal();
                mostrarDatosProtegidos();
                return;
            }

            vendedorAsignadoId = documento.getString("vendedorAsignadoId");

            String estado = documento.getString("estado");
            String numero = documento.getString("numeroPedido");

            if (numero != null) {
                tvNumeroPedido.setText("Pedido " + numero);
            }

            tvEstadoPedido.setText(estado != null ? estado : "Sin estado");

            actualizarBoton(estado);

            if (vendedorActualId.equals(vendedorAsignadoId)) {
                iniciarConsultaOriginal();
            } else {
                detenerConsultaOriginal();
                mostrarDatosProtegidos();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listenerPublico != null) {
            listenerPublico.remove();
            listenerPublico = null;
        }

        detenerConsultaOriginal();
        mostrarDatosProtegidos();
    }

    private void iniciarConsultaOriginal() {

        if (listenerOriginal != null) {
            return;
        }

        listenerOriginal = db.collection("pedidos").document(pedidoId).addSnapshotListener((documento, error) -> {

            if (error != null) {
                android.util.Log.e("ERROR_DETALLE_PEDIDO", "Error consultando pedido original", error);
                mostrarDatosProtegidos();
                return;
            }

            if (documento == null || !documento.exists()) {
                mostrarDatosProtegidos();
                return;
            }

            PedidoFirebase pedido = documento.toObject(PedidoFirebase.class);

            if (pedido != null) {
                mostrarDetalleCompleto(pedido);
            }
        });
    }

    private void detenerConsultaOriginal() {

        if (listenerOriginal != null) {
            listenerOriginal.remove();
            listenerOriginal = null;
        }
    }

    private void mostrarDatosProtegidos() {
        tvCliente.setText("Datos protegidos");
        tvDireccion.setText("Disponible al tomar el pedido");
        tvProductos.setText("Disponible al tomar el pedido");
        tvMetodoPago.setText("Disponible al tomar el pedido");
    }

    private void mostrarDetalleCompleto(PedidoFirebase pedido) {

        // El modelo actual identifica al comprador por UID.
        String nombreComprador = pedido.getNombreComprador();

        if (nombreComprador != null && !nombreComprador.trim().isEmpty()) {

            tvCliente.setText("Cliente: " + nombreComprador);

        } else {

            tvCliente.setText("Cliente: Nombre no disponible");
        }

        String direccion = valor(pedido.getDireccion()) + "\nBarrio: " + valor(pedido.getBarrio()) + "\nCiudad: " + valor(pedido.getCiudad()) + "\nIndicaciones: " + valor(pedido.getIndicaciones());

        tvDireccion.setText(direccion);
        StringBuilder productos = new StringBuilder();

        if (pedido.getProductos() != null) {

            for (ProductoPedido producto : pedido.getProductos()) {

                productos.append("• ").append(producto.getNombre()).append("\n");
            }
        }

        tvProductos.setText(productos.length() > 0 ? productos.toString() : "No hay productos registrados");
        tvMetodoPago.setText(valor(pedido.getMetodoPago()));
        String total = NumberFormat.getNumberInstance(new Locale("es", "CO")).format(pedido.getTotal());

        tvTotal.setText("Total: $" + total);
    }

    private String valor(String texto) {
        return texto == null || texto.trim().isEmpty() ? "No especificado" : texto;
    }


    private void actualizarBoton(String estado) {

        if (operacionEnCurso) {
            btnCambiarEstado.setEnabled(false);
            btnCambiarEstado.setText("PROCESANDO...");
            return;
        }

        if (vendedorAsignadoId == null) {

            if ("Recibido".equals(estado)) {
                btnCambiarEstado.setText("TOMAR PEDIDO");
                btnCambiarEstado.setEnabled(true);
            } else {
                btnCambiarEstado.setText("PEDIDO NO DISPONIBLE");
                btnCambiarEstado.setEnabled(false);
            }

            return;
        }

        if (!vendedorActualId.equals(vendedorAsignadoId)) {
            btnCambiarEstado.setText("PEDIDO ASIGNADO A OTRO VENDEDOR");
            btnCambiarEstado.setEnabled(false);
            return;
        }

        switch (estado) {

            case "Recibido":
                btnCambiarEstado.setText("PASAR A PREPARACIÓN");
                btnCambiarEstado.setEnabled(true);
                break;

            case "En preparación":
                btnCambiarEstado.setText("MARCAR COMO LISTO");
                btnCambiarEstado.setEnabled(true);
                break;

            case "Listo para entrega":
                btnCambiarEstado.setText("CONFIRMAR ENTREGA");
                btnCambiarEstado.setEnabled(true);
                break;

            case "Entregado":
                btnCambiarEstado.setText("PEDIDO COMPLETADO");
                btnCambiarEstado.setEnabled(false);
                break;

            default:
                btnCambiarEstado.setText("ESTADO NO DISPONIBLE");
                btnCambiarEstado.setEnabled(false);
                break;
        }
    }


    private void tomarPedido() {

        if (operacionEnCurso || vendedorAsignadoId != null) {
            return;
        }

        operacionEnCurso = true;
        actualizarBoton(tvEstadoPedido.getText().toString());

        asignacionService.tomarPedido(pedidoId, new AsignacionPedidoService.AsignacionCallback() {

            @Override
            public void onExito() {

                operacionEnCurso = false;

                Toast.makeText(DetallePedidoVendedorActivity.this, "Pedido asignado correctamente", Toast.LENGTH_LONG).show();

                btnCambiarEstado.setEnabled(false);
                btnCambiarEstado.setText("PEDIDO ASIGNADO A TI");
            }

            @Override
            public void onError(Exception error) {

                operacionEnCurso = false;

                android.util.Log.e("ERROR_ASIGNACION_PEDIDO", "Error al tomar pedido", error);
                actualizarBoton(tvEstadoPedido.getText().toString());
                Toast.makeText(DetallePedidoVendedorActivity.this, "No se pudo tomar el pedido: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
