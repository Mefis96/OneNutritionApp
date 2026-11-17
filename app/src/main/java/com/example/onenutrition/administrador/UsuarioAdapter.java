package com.example.onenutrition.administrador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class UsuarioAdapter
        extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final List<Usuario> usuarios;
    private final OnUsuarioActionListener listener;

    public interface OnUsuarioActionListener {
        void onEditar(Usuario usuario);
        void onEliminar(Usuario usuario);
    }

    public UsuarioAdapter(List<Usuario> usuarios, OnUsuarioActionListener listener) {

        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_administrador, parent, false);

        return new UsuarioViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull UsuarioViewHolder holder,
            int position) {

        Usuario usuario = usuarios.get(position);

        // MOSTRAR INFORMACIÓN DEL USUARIO
        holder.tvNombre.setText(usuario.getNombre());
        holder.tvCorreo.setText(usuario.getCorreo());
        holder.tvRol.setText(usuario.getRol());
        boolean esAdministrador = "Administrador".equals(usuario.getRol());
        holder.btnEditar.setEnabled(!esAdministrador);
        holder.btnEliminar.setEnabled(!esAdministrador);

        // CAMBIAR TEXTO SEGÚN EL ESTADO
        if (usuario.isActivo()) {

            holder.btnEliminar.setText("Deshabilitar");

        } else {

            holder.btnEliminar.setText("Habilitar");
        }

        // EDITAR ROL
        holder.btnEditar.setOnClickListener(v -> {
            listener.onEditar(usuario);
        });

        // HABILITAR O DESHABILITAR CUENTA
        holder.btnEliminar.setOnClickListener(v -> {
            listener.onEliminar(usuario);
        });
    }


    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre;
        TextView tvCorreo;
        TextView tvRol;
        MaterialButton btnEditar;
        MaterialButton btnEliminar;

        public UsuarioViewHolder(@NonNull View itemView) {

            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvCorreo = itemView.findViewById(R.id.tvCorreoUsuario);
            tvRol = itemView.findViewById(R.id.tvRolUsuario);
            btnEditar = itemView.findViewById(R.id.btnEditarUsuario);
            btnEliminar = itemView.findViewById(R.id.btnEliminarUsuario);
        }
    }
}