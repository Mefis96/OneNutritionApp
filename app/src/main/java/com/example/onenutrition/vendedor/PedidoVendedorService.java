package com.example.onenutrition.vendedor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class PedidoVendedorService {

    private final FirebaseFirestore db;

    public PedidoVendedorService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface PedidosCallback {

        void onPedidosActualizados(List<PedidoPublicoFirebase> pedidos);

        void onError(Exception error);
    }

    public ListenerRegistration escucharPedidos(PedidosCallback callback) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            callback.onError(new IllegalStateException("No hay un vendedor autenticado"));
            return null;
        }

        return db.collection("pedidosPublicos").addSnapshotListener((snapshot, error) -> {

            if (error != null) {
                callback.onError(error);
                return;
            }

            if (snapshot == null) {
                return;
            }

            List<PedidoPublicoFirebase> pedidos = new ArrayList<>();

            for (DocumentSnapshot documento : snapshot.getDocuments()) {

                PedidoPublicoFirebase pedido = documento.toObject(PedidoPublicoFirebase.class);

                if (pedido != null) {
                    pedidos.add(pedido);
                }
            }

            pedidos.sort((a, b) -> {

                if (a.getFechaCreacion() == null) {
                    return 1;
                }

                if (b.getFechaCreacion() == null) {
                    return -1;
                }

                return b.getFechaCreacion().compareTo(a.getFechaCreacion());
            });

            callback.onPedidosActualizados(pedidos);
        });
    }
}
