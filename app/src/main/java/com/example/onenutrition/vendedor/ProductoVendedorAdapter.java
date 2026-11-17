package com.example.onenutrition.vendedor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.model.Producto;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;

public class ProductoVendedorAdapter extends RecyclerView.Adapter<ProductoVendedorAdapter.ProductoViewHolder> {

    private final List<Producto> productos;
    private final OnProductoActionListener listener;

    public interface OnProductoActionListener {
        void onEditar(Producto producto);

        void onEliminar(Producto producto);
    }

    public ProductoVendedorAdapter(List<Producto> productos, OnProductoActionListener listener) {

        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_vendedor, parent, false);

        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {

        Producto producto = productos.get(position);

        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {

            // Imagen guardada en Cloudinary
            Glide.with(holder.itemView.getContext()).load(producto.getImagenUrl()).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else if (producto.tieneImagenUri()) {

            // Imagen seleccionada desde el celular
            Glide.with(holder.itemView.getContext()).load(Uri.parse(producto.getImagenUri())).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else {

            // Imagen local de drawable
            Glide.with(holder.itemView.getContext()).load(producto.getImagen() != 0 ? producto.getImagen() : R.drawable.proteina_whey).into(holder.imgProducto);
        }

        holder.tvNombre.setText(producto.getNombre());
        holder.tvCategoria.setText(producto.getCategoria());
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        holder.tvPrecio.setText(formato.format(producto.getPrecio()));
        holder.btnEditar.setOnClickListener(v -> {

            if (listener != null) {
                listener.onEditar(producto);

            }

        });

        holder.btnEliminar.setOnClickListener(v -> {

            if (listener != null) {
                listener.onEliminar(producto);

            }
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProducto;
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvPrecio;
        MaterialButton btnEditar;
        MaterialButton btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {

            super(itemView);

            imgProducto = itemView.findViewById(R.id.imgProductoVendedor);
            tvNombre = itemView.findViewById(R.id.tvNombreProductoVendedor);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaProductoVendedor);
            tvPrecio = itemView.findViewById(R.id.tvPrecioProductoVendedor);
            btnEditar = itemView.findViewById(R.id.btnEditarProducto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProducto);
        }
    }
}