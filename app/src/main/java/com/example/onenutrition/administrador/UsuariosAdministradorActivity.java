package com.example.onenutrition.administrador;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenutrition.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import android.util.Log;
import com.example.onenutrition.auth.ActividadProtegida;

public class UsuariosAdministradorActivity extends ActividadProtegida {

    private TextInputEditText etBuscarUsuario;
    private RecyclerView recyclerUsuarios;
    private UsuarioAdapter adapter;
    private List<Usuario> listaUsuarios;
    private List<Usuario> listaFiltrada;
    private MaterialButton btnEditarPerfilAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_usuarios_administrador);
        btnEditarPerfilAdmin = findViewById(R.id.btnEditarPerfilAdmin);

        inicializarVistas();
        cargarUsuarios();
        configurarRecyclerView();
        cargarUsuariosFirebase();
        configurarBuscador();
        configurarNavegacion();

    }

    private void inicializarVistas() {

        etBuscarUsuario = findViewById(R.id.etBuscarUsuarioAdmin);
        recyclerUsuarios = findViewById(R.id.recyclerUsuariosAdmin);
    }

    private void cargarUsuarios() {

        listaUsuarios = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

    }


    private void cargarUsuariosFirebase() {

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .get()
                .addOnSuccessListener(documentos -> {

                    listaUsuarios.clear();

                    for (DocumentSnapshot documento : documentos) {

                        Usuario usuario = new Usuario();
                        usuario.setUid(documento.getId());
                        usuario.setNombre(documento.getString("nombre") != null ? documento.getString("nombre") : "");
                        usuario.setCorreo(documento.getString("correo") != null ? documento.getString("correo") : "");
                        usuario.setTelefono(documento.getString("telefono") != null ? documento.getString("telefono") : "");
                        usuario.setRol(documento.getString("rol") != null? documento.getString("rol"): "Comprador");
                        Boolean activo = documento.getBoolean("activo");
                        usuario.setActivo(activo == null || activo);

                        listaUsuarios.add(usuario);
                    }

                    filtrarUsuarios(etBuscarUsuario.getText() != null? etBuscarUsuario.getText().toString(): "");
                    Log.d("ADMIN_USUARIOS","Usuarios cargados: " + listaUsuarios.size());

                }) .addOnFailureListener(e -> {

                    Log.e("ADMIN_USUARIOS","Error al cargar usuarios",e);
                    Toast.makeText(this,"No se pudieron cargar los usuarios", Toast.LENGTH_SHORT).show();
                });
    }

    private void configurarRecyclerView() {

        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuarioAdapter(listaFiltrada, new UsuarioAdapter.OnUsuarioActionListener() {

                    @Override
                    public void onEditar(Usuario usuario) {
                        mostrarDialogoCambiarRol(usuario);
                    }

                    @Override
                    public void onEliminar(Usuario usuario) {

                        confirmarEliminacion(usuario);
                    }
                }
        );

        recyclerUsuarios.setAdapter(adapter);
    }

    private void configurarBuscador() {

        etBuscarUsuario.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        filtrarUsuarios(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    private void filtrarUsuarios(String texto) {

        listaFiltrada.clear();
        String busqueda = texto.trim().toLowerCase();

        if (busqueda.isEmpty()) {

            listaFiltrada.addAll(listaUsuarios);

        } else {

            for (Usuario usuario : listaUsuarios) {

                boolean coincideNombre = usuario.getNombre().toLowerCase().contains(busqueda);
                boolean coincideCorreo = usuario.getCorreo().toLowerCase().contains(busqueda);
                boolean coincideRol = usuario.getRol().toLowerCase().contains(busqueda);

                if (coincideNombre || coincideCorreo || coincideRol) {

                    listaFiltrada.add(usuario);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    private void mostrarDialogoCambiarRol(Usuario usuario) {

        // No permitir modificar administradores
        if ("Administrador".equals(usuario.getRol())) {

            Toast.makeText(this,"No se puede modificar el rol de un administrador",Toast.LENGTH_SHORT).show();
            return;
        }

        String[] roles = {"Comprador", "Vendedor"};

        int rolSeleccionado = "Vendedor".equals(usuario.getRol()) ? 1 : 0;
        final int[] seleccion = {rolSeleccionado};

        new AlertDialog.Builder(this).setTitle("Cambiar rol de " + usuario.getNombre()).setSingleChoiceItems(roles, rolSeleccionado, (dialog, which) -> seleccion[0] = which)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoRol = roles[seleccion[0]];

                    if (nuevoRol.equals(usuario.getRol())) {
                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(usuario.getUid())
                            .update("rol", nuevoRol)
                            .addOnSuccessListener(unused -> {

                                usuario.setRol(nuevoRol);
                                adapter.notifyDataSetChanged();
                                Toast.makeText( this,"Rol actualizado a " + nuevoRol,Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {

                                Log.e("ADMIN_ROLES","Error al cambiar el rol",e);
                                Toast.makeText(this,"No se pudo actualizar el rol", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }



    private void confirmarEliminacion(Usuario usuario) {

        if ("Administrador".equals(usuario.getRol())) {

            Toast.makeText(this,"No se puede deshabilitar una cuenta administradora",Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nuevoEstado = !usuario.isActivo();

        String accion = nuevoEstado ? "Habilitar" : "Deshabilitar";

        new AlertDialog.Builder(this)
                .setTitle(accion + " usuario")
                .setMessage("¿Deseas " + accion.toLowerCase() + " la cuenta de " + usuario.getNombre() + "?")
                .setPositiveButton(accion, (dialog, which) -> {

                    FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(usuario.getUid())
                            .update("activo", nuevoEstado)
                            .addOnSuccessListener(unused -> {

                                usuario.setActivo(nuevoEstado);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this,"Cuenta " + ( nuevoEstado ? "habilitada": "deshabilitada") + " correctamente", Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {

                                android.util.Log.e("ADMIN_ESTADO","Error al actualizar el estado",e);
                                Toast.makeText(this,"No se pudo actualizar la cuenta",Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void configurarNavegacion() {

        TextView navInicio = findViewById(R.id.navAdminInicio);
        TextView navProductos = findViewById(R.id.navAdminProductos);
        TextView navUsuarios = findViewById(R.id.navAdminUsuarios);
        TextView navReportes = findViewById(R.id.navAdminReportes);
        navInicio.setOnClickListener(v -> {

            Intent intent = new Intent(UsuariosAdministradorActivity.this, AdministradorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnEditarPerfilAdmin.setOnClickListener(v -> {

            Intent intent = new Intent(UsuariosAdministradorActivity.this, EditarPerfilAdministradorActivity.class);
            startActivity(intent);
        });

        navProductos.setOnClickListener(v -> {

            Intent intent = new Intent(UsuariosAdministradorActivity.this, ProductosAdministradorActivity.class);
            startActivity(intent);
            finish();
        });

        navUsuarios.setOnClickListener(v -> {

        });

        navReportes.setOnClickListener(v -> {

            Intent intent = new Intent(UsuariosAdministradorActivity.this, ReporteVentasActivity.class);
            startActivity(intent);

            finish();
        });
    }
}