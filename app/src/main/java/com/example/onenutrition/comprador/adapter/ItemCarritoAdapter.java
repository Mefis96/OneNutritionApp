package com.example.onenutrition.comprador.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.carrito.CarritoManager;
import com.example.onenutrition.comprador.model.ItemCarrito;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;

public class ItemCarritoAdapter extends RecyclerView.Adapter<ItemCarritoAdapter.CarritoViewHolder> {

    private final List<ItemCarrito> items;
    private final Runnable onCarritoActualizado;

    public ItemCarritoAdapter(List<ItemCarrito> items, Runnable onCarritoActualizado) {

        this.items = items;
        this.onCarritoActualizado = onCarritoActualizado;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {

        ItemCarrito item = items.get(position);
        holder.tvNombre.setText(item.getNombre());
        holder.tvCategoria.setText(item.getCategoria());


        if (item.getImagenUrl() != null && !item.getImagenUrl().isEmpty()) {

            // Imagen del producto almacenada en Cloudinary.
            Glide.with(holder.itemView.getContext()).load(item.getImagenUrl()).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else if (item.tieneImagenUri()) {

            // Imagen local seleccionada desde el dispositivo.
            Glide.with(holder.itemView.getContext()).load(Uri.parse(item.getImagenUri())).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else {

            // Imagen de respaldo para productos locales.
            Glide.with(holder.itemView.getContext()).load(item.getImagen() != 0 ? item.getImagen() : R.drawable.proteina_whey).into(holder.imgProducto);
        }

        actualizarDatos(holder, item);
        // Sumar cantidad
        holder.btnSumar.setOnClickListener(v -> {

            item.setCantidad(item.getCantidad() + 1);
            actualizarDatos(holder, item);
            onCarritoActualizado.run();
        });
        // Restar cantidad
        holder.btnRestar.setOnClickListener(v -> {

            if (item.getCantidad() > 1) {
                item.setCantidad(item.getCantidad() - 1);
                actualizarDatos(holder, item);
                onCarritoActualizado.run();
            }
        });
        // Eliminar producto
        holder.btnEliminar.setOnClickListener(v -> {

            int posicionActual = holder.getAdapterPosition();

            if (posicionActual != RecyclerView.NO_POSITION) {

                ItemCarrito itemEliminar = items.get(posicionActual);
                CarritoManager.eliminarProducto(itemEliminar);
                notifyItemRemoved(posicionActual);
                onCarritoActualizado.run();
            }
        });
    }

    private void actualizarDatos(

            CarritoViewHolder holder, ItemCarrito item) {

        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        holder.tvPrecio.setText(formato.format(item.getPrecio()));
        holder.tvCantidad.setText(String.valueOf(item.getCantidad()));
        holder.tvSubtotal.setText("Subtotal: " + formato.format(item.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CarritoViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProducto;
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvPrecio;
        TextView tvCantidad;
        TextView tvSubtotal;
        MaterialButton btnRestar;
        MaterialButton btnSumar;
        ImageButton btnEliminar;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProducto = itemView.findViewById(R.id.imgCarritoProducto);
            tvNombre = itemView.findViewById(R.id.tvCarritoNombre);
            tvCategoria = itemView.findViewById(R.id.tvCarritoCategoria);
            tvPrecio = itemView.findViewById(R.id.tvCarritoPrecio);
            tvCantidad = itemView.findViewById(R.id.tvCarritoCantidad);
            tvSubtotal = itemView.findViewById(R.id.tvCarritoSubtotal);
            btnRestar = itemView.findViewById(R.id.btnCarritoRestar);
            btnSumar = itemView.findViewById(R.id.btnCarritoSumar);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProducto);
        }
    }
}