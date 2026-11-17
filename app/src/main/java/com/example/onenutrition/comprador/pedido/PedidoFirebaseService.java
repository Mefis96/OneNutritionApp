package com.example.onenutrition.comprador.pedido;

import com.example.onenutrition.comprador.model.PedidoFirebase;
import com.example.onenutrition.vendedor.PedidoPublicoFirebase;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class PedidoFirebaseService {

    private final FirebaseFirestore db;

    public PedidoFirebaseService() {
        db = FirebaseFirestore.getInstance();
    }

    // Comunicar el resultado del guardado
    public interface PedidoCallback {

        void onExito(String idPedido);

        void onError(Exception error);
    }


    // Guardar un pedido en Firebase Firestore
    public void guardarPedido(String idPedido, PedidoFirebase pedido, PedidoCallback callback) {

        if (callback == null) {
            throw new IllegalArgumentException("El callback no puede ser nulo");
        }

        if (idPedido == null || idPedido.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("El identificador del pedido es obligatorio"));
            return;
        }

        if (pedido == null) {
            callback.onError(new IllegalArgumentException("El pedido no puede ser nulo"));
            return;
        }

        if (pedido.getCompradorId() == null || pedido.getCompradorId().trim().isEmpty()) {

            callback.onError(new IllegalArgumentException("El pedido debe tener un comprador"));
            return;
        }

        if (pedido.getProductos() == null || pedido.getProductos().isEmpty()) {

            callback.onError(new IllegalArgumentException("El pedido debe contener productos"));
            return;
        }

        String uidAutenticado = FirebaseAuth.getInstance().getUid();

        if (uidAutenticado == null || !uidAutenticado.equals(pedido.getCompradorId())) {

            callback.onError(new IllegalStateException("El comprador del pedido no coincide con el usuario autenticado"));
            return;
        }

        // Primero consultar el nombre del comprador.
        // La consulta llamará a ejecutarGuardadoPedido()
        // únicamente cuando obtenga el nombre.
        consultarNombreComprador(idPedido, uidAutenticado, pedido, callback);
    }

    // Ejecutar la transacción después de obtener el nombre
    private void ejecutarGuardadoPedido(String idPedido, PedidoFirebase pedido, PedidoCallback callback) {

        // Referencia al pedido original
        DocumentReference referenciaPedido = db.collection("pedidos").document(idPedido);

        // Referencia al contador de pedidos
        DocumentReference referenciaContador = db.collection("contadores").document("pedidos");

        // Referencia al registro público
        DocumentReference referenciaPublica = db.collection("pedidosPublicos").document(idPedido);

        db.runTransaction(transaction -> {

            // 1. LEER TODOS LOS DOCUMENTOS

            DocumentSnapshot documentoPedido = transaction.get(referenciaPedido);

            DocumentSnapshot documentoContador = transaction.get(referenciaContador);

            DocumentSnapshot documentoPublico = transaction.get(referenciaPublica);

            // 2. VALIDAR LOS DOCUMENTOS

            if (documentoPedido.exists()) {
                throw new IllegalStateException("Este pedido ya fue registrado");
            }

            if (documentoPublico.exists()) {
                throw new IllegalStateException("Este pedido ya tiene un registro público");
            }

            if (!documentoContador.exists()) {
                throw new IllegalStateException("No existe el contador de pedidos");
            }

            Long ultimoNumero = documentoContador.getLong("ultimoNumero");

            if (ultimoNumero == null || ultimoNumero < 0) {
                throw new IllegalStateException("El contador de pedidos no es válido");
            }

            // 3. GENERAR EL CONSECUTIVO

            long siguienteNumero = ultimoNumero + 1;

            String numeroPedido = String.format(Locale.ROOT, "ON-%03d", siguienteNumero);

            pedido.setNumeroPedido(numeroPedido);
            pedido.setConsecutivo(siguienteNumero);

            // 4. PREPARAR EL REGISTRO PÚBLICO

            PedidoPublicoFirebase pedidoPublico = new PedidoPublicoFirebase();

            pedidoPublico.setPedidoId(idPedido);
            pedidoPublico.setNumeroPedido(numeroPedido);
            pedidoPublico.setEstado("Recibido");
            pedidoPublico.setVendedorAsignadoId(null);
            pedidoPublico.setTotal(pedido.getTotal());
            pedidoPublico.setFechaCreacion(pedido.getFechaCreacion());

            // 5. REALIZAR TODAS LAS ESCRITURAS

            // Actualizar consecutivo
            transaction.update(referenciaContador, "ultimoNumero", siguienteNumero, "ultimoPedidoId", idPedido);

            // Guardar pedido original con nombreComprador
            transaction.set(referenciaPedido, pedido);

            // Guardar registro público
            transaction.set(referenciaPublica, pedidoPublico);

            return null;

        }).addOnSuccessListener(unused -> {

            callback.onExito(idPedido);

        }).addOnFailureListener(error -> {

            callback.onError(error);

        });
    }


    private void consultarNombreComprador(String idPedido, String uid, PedidoFirebase pedido, PedidoCallback callback) {

        db.collection("usuarios").document(uid).get().addOnSuccessListener(documento -> {

            if (!documento.exists()) {
                callback.onError(new IllegalStateException("No se encontró el perfil del comprador"));
                return;
            }

            String nombre = documento.getString("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                callback.onError(new IllegalStateException("El comprador no tiene un nombre registrado"));
                return;
            }

            pedido.setNombreComprador(nombre.trim());

            ejecutarGuardadoPedido(idPedido, pedido, callback);

        }).addOnFailureListener(callback::onError);
    }

}
