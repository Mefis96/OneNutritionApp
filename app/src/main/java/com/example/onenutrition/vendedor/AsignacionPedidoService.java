package com.example.onenutrition.vendedor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AsignacionPedidoService {

    private final FirebaseFirestore db;

    public AsignacionPedidoService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface AsignacionCallback {
        void onExito();

        void onError(Exception error);
    }

    public void tomarPedido(String pedidoId, AsignacionCallback callback) {

        if (callback == null) {
            throw new IllegalArgumentException("El callback no puede ser nulo");
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            callback.onError(new IllegalStateException("No hay un vendedor autenticado"));
            return;
        }

        if (pedidoId == null || pedidoId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("El identificador del pedido es obligatorio"));
            return;
        }

        String vendedorId = usuario.getUid();
        DocumentReference pedidoOriginal = db.collection("pedidos").document(pedidoId);
        DocumentReference pedidoPublico = db.collection("pedidosPublicos").document(pedidoId);
        db.runTransaction(transaction -> {

            // Leer únicamente el documento público
            DocumentSnapshot publico = transaction.get(pedidoPublico);

            if (!publico.exists()) {
                throw new IllegalStateException("El pedido no existe");
            }

            // Verificar si ya tiene vendedor asignado
            String asignadoPublico = publico.getString("vendedorAsignadoId");

            if (asignadoPublico != null) {
                throw new IllegalStateException("Este pedido ya fue tomado por otro vendedor");
            }

            // Verificar que siga disponible
            if (!"Recibido".equals(publico.getString("estado"))) {

                throw new IllegalStateException("El pedido ya no está disponible");
            }

            // Asignar el mismo vendedor en ambos documentos
            transaction.update(pedidoOriginal, "vendedorAsignadoId", vendedorId);
            transaction.update(pedidoPublico, "vendedorAsignadoId", vendedorId);

            return null;

        }).addOnSuccessListener(resultado -> {
            callback.onExito();
        }).addOnFailureListener(error -> {
            callback.onError(error);
        });
    }
}
