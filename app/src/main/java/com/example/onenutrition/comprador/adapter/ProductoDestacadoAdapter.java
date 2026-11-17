package com.example.onenutrition.comprador.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.DetalleProductoActivity;
import com.example.onenutrition.comprador.model.Producto;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductoDestacadoAdapter extends RecyclerView.Adapter<ProductoDestacadoAdapter.DestacadoViewHolder> {

    private final List<Producto> listaProductos;

    public ProductoDestacadoAdapter(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    @NonNull
    @Override
    public DestacadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_destacado, parent, false);

        return new DestacadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DestacadoViewHolder holder, int position) {

        Producto producto = listaProductos.get(position);

        // Mostrar información real del producto
        holder.tvNombre.setText(producto.getNombre());
        holder.tvCategoria.setText(producto.getCategoria());

        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        holder.tvPrecio.setText(formato.format(producto.getPrecio()));

        // Cargar imagen desde Cloudinary
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {

            Glide.with(holder.itemView.getContext()).load(producto.getImagenUrl()).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else {

            Glide.with(holder.itemView.getContext()).load(producto.getImagen() != 0 ? producto.getImagen() : R.drawable.proteina_whey).into(holder.imgProducto);
        }

        // Abrir detalle al tocar la tarjeta
        holder.itemView.setOnClickListener(v -> abrirDetalle(v, producto));

        // Abrir detalle desde el botón
        holder.btnVerProducto.setOnClickListener(v -> abrirDetalle(v, producto));
    }

    private void abrirDetalle(View vista, Producto producto) {

        Intent intent = new Intent(vista.getContext(), DetalleProductoActivity.class);

        intent.putExtra("idProducto", producto.getIdProducto());
        intent.putExtra("idVendedor", producto.getIdVendedor());
        intent.putExtra("nombre", producto.getNombre());
        intent.putExtra("categoria", producto.getCategoria());
        intent.putExtra("descripcion", producto.getDescripcion());
        intent.putExtra("precio", producto.getPrecio());
        intent.putExtra("imagen", producto.getImagen());
        intent.putExtra("imagenUri", producto.getImagenUri());
        intent.putExtra("imagenUrl", producto.getImagenUrl());

        vista.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void actualizarLista(List<Producto> nuevaLista) {

        listaProductos.clear();
        listaProductos.addAll(nuevaLista);

        notifyDataSetChanged();
    }

    public static class DestacadoViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProducto;
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvPrecio;
        MaterialButton btnVerProducto;

        public DestacadoViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProducto = itemView.findViewById(R.id.imgProductoDestacado);
            tvNombre = itemView.findViewById(R.id.tvNombreDestacado);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaDestacado);
            tvPrecio = itemView.findViewById(R.id.tvPrecioDestacado);
            btnVerProducto = itemView.findViewById(R.id.btnVerProductoDestacado);
        }
    }
}
