package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.auth.ActividadProtegida;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.adapter.PedidoAdapter;
import com.example.onenutrition.comprador.model.Pedido;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MisPedidosActivity extends ActividadProtegida {

    private ImageButton btnVolver;
    private RecyclerView recyclerPedidos;
    private TextView tvSinPedidos;
    private final List<Pedido> listaPedidos = new ArrayList<>();
    private PedidoAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pedidos);

        btnVolver = findViewById(R.id.btnVolverPedidos);
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
        tvSinPedidos = findViewById(R.id.tvSinPedidos);
        firestore = FirebaseFirestore.getInstance();
        btnVolver.setOnClickListener(v -> finish());
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoAdapter(listaPedidos);
        recyclerPedidos.setAdapter(adapter);
        recyclerPedidos.setVisibility(View.GONE);
        tvSinPedidos.setVisibility(View.VISIBLE);
        tvSinPedidos.setText("Cargando pedidos...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarPedidosFirestore();
    }

    private void cargarPedidosFirestore() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null || !usuario.isEmailVerified()) {
            Toast.makeText(this, "Debes iniciar sesión con un correo verificado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (listenerPedidos != null) {
            listenerPedidos.remove();
            listenerPedidos = null;
        }

        listenerPedidos = firestore.collection("pedidos").whereEqualTo("compradorId", usuario.getUid()).addSnapshotListener((documentos, error) -> {

            if (error != null) {
                tvSinPedidos.setVisibility(View.VISIBLE);
                recyclerPedidos.setVisibility(View.GONE);
                tvSinPedidos.setText("Error al cargar los pedidos");

                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (documentos == null) {
                return;
            }

            listaPedidos.clear();

            for (DocumentSnapshot documento : documentos.getDocuments()) {

                String numeroPedido = documento.getString("numeroPedido");

                if (numeroPedido == null || numeroPedido.trim().isEmpty()) {
                    numeroPedido = "Sin número";
                }

                String estado = documento.getString("estado");
                String metodoPago = documento.getString("metodoPago");
                Double total = documento.getDouble("total");
                Timestamp fechaTimestamp = documento.getTimestamp("fechaCreacion");
                Date fecha = fechaTimestamp != null ? fechaTimestamp.toDate() : null;
                String fechaTexto = fecha != null ? new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "CO")).format(fecha) : "Sin fecha";
                Pedido pedido = new Pedido(numeroPedido, fechaTexto, metodoPago != null ? metodoPago : "No especificado", estado != null ? estado : "Recibido", total != null ? total : 0);

                listaPedidos.add(pedido);
            }
            // Ordenar por número consecutivo, del mayor al menor.
            listaPedidos.sort((pedido1, pedido2) -> {

                int numero1 = obtenerConsecutivo(pedido1.getNumeroPedido());
                int numero2 = obtenerConsecutivo(pedido2.getNumeroPedido());

                return Integer.compare(numero2, numero1);
            });

            adapter.notifyDataSetChanged();
            verificarPedidos();
        });
    }

    private void verificarPedidos() {

        if (listaPedidos.isEmpty()) {
            recyclerPedidos.setVisibility(View.GONE);
            tvSinPedidos.setVisibility(View.VISIBLE);
            tvSinPedidos.setText("Todavía no tienes pedidos.");
        } else {
            recyclerPedidos.setVisibility(View.VISIBLE);
            tvSinPedidos.setVisibility(View.GONE);
        }
    }

    private int obtenerConsecutivo(String numeroPedido) {

        if (numeroPedido == null || !numeroPedido.matches("ON-[0-9]+")) {
            return -1;
        }

        try {
            return Integer.parseInt(numeroPedido.substring(3));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listenerPedidos != null) {
            listenerPedidos.remove();
            listenerPedidos = null;
        }
    }
}
