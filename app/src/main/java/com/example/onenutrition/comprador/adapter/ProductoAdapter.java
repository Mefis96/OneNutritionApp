package com.example.onenutrition.comprador.adapter;

import android.content.Intent;
import android.net.Uri;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final List<Producto> listaProductos;

    public ProductoAdapter(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);

        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {

        Producto producto = listaProductos.get(position);

        // Información del producto
        holder.tvNombreProducto.setText(producto.getNombre());
        holder.tvCategoriaProducto.setText(producto.getCategoria());

        NumberFormat formatoPrecio = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        holder.tvPrecioProducto.setText(formatoPrecio.format(producto.getPrecio()));

        // Imagen del producto
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {

            // Fotografía almacenada en Cloudinary
            Glide.with(holder.itemView.getContext()).load(producto.getImagenUrl()).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else if (producto.tieneImagenUri()) {

            // Imagen local seleccionada desde Android
            Glide.with(holder.itemView.getContext()).load(Uri.parse(producto.getImagenUri())).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(holder.imgProducto);

        } else {

            // Imagen de respaldo
            Glide.with(holder.itemView.getContext()).load(producto.getImagen() != 0 ? producto.getImagen() : R.drawable.proteina_whey).into(holder.imgProducto);
        }

        // Abrir detalle del producto seleccionado
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), DetalleProductoActivity.class);

            intent.putExtra("idProducto", producto.getIdProducto());

            intent.putExtra("idVendedor", producto.getIdVendedor());

            intent.putExtra("nombre", producto.getNombre());

            intent.putExtra("categoria", producto.getCategoria());

            intent.putExtra("descripcion", producto.getDescripcion());

            intent.putExtra("precio", producto.getPrecio());

            intent.putExtra("imagen", producto.getImagen());

            intent.putExtra("imagenUri", producto.getImagenUri());

            intent.putExtra("imagenUrl", producto.getImagenUrl());

            v.getContext().startActivity(intent);
        });
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

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProducto;
        TextView tvNombreProducto;
        TextView tvCategoriaProducto;
        TextView tvPrecioProducto;

        public ProductoViewHolder(@NonNull View itemView) {

            super(itemView);

            imgProducto = itemView.findViewById(R.id.imgProducto);

            tvNombreProducto = itemView.findViewById(R.id.tvNombreProducto);

            tvCategoriaProducto = itemView.findViewById(R.id.tvCategoriaProducto);

            tvPrecioProducto = itemView.findViewById(R.id.tvPrecioProducto);
        }
    }
}
