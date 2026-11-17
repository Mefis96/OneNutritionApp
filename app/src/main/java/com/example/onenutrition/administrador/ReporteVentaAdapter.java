
package com.example.onenutrition.administrador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReporteVentaAdapter
        extends RecyclerView.Adapter<ReporteVentaAdapter.VentaViewHolder> {

    private final List<DocumentSnapshot> pedidos;

    public ReporteVentaAdapter(List<DocumentSnapshot> pedidos) {
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public VentaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_reporte_venta,
                        parent,
                        false
                );

        return new VentaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VentaViewHolder holder,
            int position) {

        DocumentSnapshot pedido = pedidos.get(position);

        // NÚMERO DE PEDIDO
        String numero = pedido.getString("numeroPedido");

        holder.tvNumero.setText(
                "Pedido " + (numero != null ? numero : "Sin número")
        );

        // FECHA DEL PEDIDO
        Timestamp fecha = pedido.getTimestamp("fechaCreacion");

        if (fecha != null) {

            SimpleDateFormat formatoFecha =
                    new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            new Locale("es", "CO")
                    );

            holder.tvFecha.setText(
                    formatoFecha.format(fecha.toDate())
            );

        } else {
            holder.tvFecha.setText("Sin fecha");
        }

        // MÉTODO DE PAGO
        String metodoPago = pedido.getString("metodoPago");

        holder.tvMetodoPago.setText(
                "Método de pago: " +
                        (metodoPago != null ? metodoPago : "No registrado")
        );

        // ESTADO DEL PEDIDO
        String estado = pedido.getString("estado");

        holder.tvEstado.setText(
                estado != null ? estado : "Sin estado"
        );

        // TOTAL DEL PEDIDO
        Double total = pedido.getDouble("total");

        NumberFormat formatoMoneda =
                NumberFormat.getCurrencyInstance(
                        new Locale("es", "CO")
                );

        holder.tvTotal.setText(
                formatoMoneda.format(total != null ? total : 0)
        );
    }

    //OBTENER Y MOSTRAR INFORMACION DE LOS PEDIDOS
    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class VentaViewHolder extends RecyclerView.ViewHolder {

        TextView tvNumero;
        TextView tvFecha;
        TextView tvMetodoPago;
        TextView tvEstado;
        TextView tvTotal;

        public VentaViewHolder(@NonNull View itemView) {

            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumeroPedidoReporte);
            tvFecha = itemView.findViewById(R.id.tvFechaPedidoReporte);
            tvMetodoPago = itemView.findViewById(R.id.tvMetodoPagoReporte);
            tvEstado = itemView.findViewById(R.id.tvEstadoPedidoReporte);
            tvTotal = itemView.findViewById(R.id.tvTotalPedidoReporte);
        }
    }
}
