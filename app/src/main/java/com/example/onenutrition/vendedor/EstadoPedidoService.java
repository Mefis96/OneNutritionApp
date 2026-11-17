package com.example.onenutrition.vendedor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EstadoPedidoService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface EstadoCallback {
        void onExito();

        void onError(Exception error);
    }

    public void cambiarEstado(String pedidoId, String nuevoEstado, EstadoCallback callback) {

        if (callback == null) {
            throw new IllegalArgumentException("El callback es obligatorio");
        }

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null || pedidoId == null || pedidoId.trim().isEmpty()) {

            callback.onError(new IllegalStateException("No hay un vendedor o pedido válido"));
            return;
        }

        DocumentReference original = db.collection("pedidos").document(pedidoId);

        DocumentReference publico = db.collection("pedidosPublicos").document(pedidoId);

        db.runTransaction(transaction -> {

            // Primero leer ambos documentos.
            var documentoOriginal = transaction.get(original);
            var documentoPublico = transaction.get(publico);

            if (!documentoOriginal.exists() || !documentoPublico.exists()) {
                throw new IllegalStateException("El pedido no existe");
            }

            String asignadoOriginal = documentoOriginal.getString("vendedorAsignadoId");
            String asignadoPublico = documentoPublico.getString("vendedorAsignadoId");

            if (!uid.equals(asignadoOriginal) || !uid.equals(asignadoPublico)) {
                throw new IllegalStateException("Este pedido no está asignado a ti");
            }

            String estadoActual = documentoOriginal.getString("estado");
            String estadoPublico = documentoPublico.getString("estado");

            if (estadoActual == null || !estadoActual.equals(estadoPublico)) {
                throw new IllegalStateException("Los estados del pedido no coinciden");
            }

            boolean transicionValida = ("Recibido".equals(estadoActual) && "En preparación".equals(nuevoEstado)) || ("En preparación".equals(estadoActual) && "Listo para entrega".equals(nuevoEstado)) || ("Listo para entrega".equals(estadoActual) && "Entregado".equals(nuevoEstado));

            if (!transicionValida) {
                throw new IllegalStateException("Cambio de estado no permitido");
            }

            // Actualizar ambos documentos.
            if ("Entregado".equals(nuevoEstado)) {

                if (!"Pendiente".equals(documentoOriginal.getString("estadoPago"))) {
                    throw new IllegalStateException("El estado del pago no es válido");
                }

                transaction.update(original, "estado", nuevoEstado, "estadoPago", "Pagado");

            } else {

                transaction.update(original, "estado", nuevoEstado);
            }

            transaction.update(publico, "estado", nuevoEstado);
            return null;

        }).addOnSuccessListener(resultado -> callback.onExito()).addOnFailureListener(callback::onError);
    }
}
