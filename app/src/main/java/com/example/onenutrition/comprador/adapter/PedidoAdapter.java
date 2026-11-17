package com.example.onenutrition.comprador.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.Pedido;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private final List<Pedido> pedidos;

    public PedidoAdapter(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {

        Pedido pedido = pedidos.get(position);

        holder.tvNumero.setText("Pedido #" + pedido.getNumeroPedido());
        holder.tvEstado.setText(pedido.getEstado());
        holder.tvFecha.setText("Fecha: " + pedido.getFecha());
        holder.tvMetodo.setText("Pago: " + pedido.getMetodoPago());
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        holder.tvTotal.setText(formato.format(pedido.getTotal()));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {

        TextView tvNumero;
        TextView tvEstado;
        TextView tvFecha;
        TextView tvMetodo;
        TextView tvTotal;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNumero = itemView.findViewById(R.id.tvPedidoNumero);
            tvEstado = itemView.findViewById(R.id.tvPedidoEstado);
            tvFecha = itemView.findViewById(R.id.tvPedidoFecha);
            tvMetodo = itemView.findViewById(R.id.tvPedidoMetodo);
            tvTotal = itemView.findViewById(R.id.tvPedidoTotal);
        }
    }
}