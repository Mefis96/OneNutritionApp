package com.example.onenutrition.vendedor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.onenutrition.auth.ActividadProtegida;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CrearProductoVendedorActivity extends ActividadProtegida {

    private TextInputEditText etNombre;
    private TextInputEditText etCategoria;
    private TextInputEditText etPrecio;
    private TextInputEditText etDescripcion;
    private ImageView imgNuevoProducto;
    private ImageButton btnVolver;
    private MaterialButton btnGuardar;
    private MaterialButton btnSeleccionarImagen;
    private Uri imagenSeleccionadaUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final long TAMANO_MAXIMO = 2 * 1024 * 1024;
    private final ActivityResultLauncher<String[]> seleccionarImagenLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {

        if (uri != null) {

            imagenSeleccionadaUri = null;

            try {

                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (!imagenValida(uri)) {
                    return;
                }
                imagenSeleccionadaUri = uri;
                imgNuevoProducto.setImageURI(uri);

                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {

                Toast.makeText(this, "No se pudo seleccionar la imagen", Toast.LENGTH_LONG).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crear_producto_vendedor);
        etNombre = findViewById(R.id.etNombreNuevoProducto);
        etCategoria = findViewById(R.id.etCategoriaNuevoProducto);
        etDescripcion = findViewById(R.id.etDescripcionNuevoProducto);
        etPrecio = findViewById(R.id.etPrecioNuevoProducto);
        imgNuevoProducto = findViewById(R.id.imgNuevoProducto);
        btnVolver = findViewById(R.id.btnVolverCrearProducto);
        btnGuardar = findViewById(R.id.btnGuardarNuevoProducto);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagenProducto);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        btnVolver.setOnClickListener(v -> finish());
        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());
        btnGuardar.setOnClickListener(v -> guardarProducto());
    }

    private void seleccionarImagen() {

        seleccionarImagenLauncher.launch(new String[]{"image/jpeg", "image/png", "image/webp"});
    }

    private boolean imagenValida(Uri uri) {

        try {

            String tipo = getContentResolver().getType(uri);

            if (tipo == null || (!tipo.equals("image/jpeg") && !tipo.equals("image/png") && !tipo.equals("image/webp"))) {

                Toast.makeText(this, "Selecciona una imagen JPG, PNG o WEBP", Toast.LENGTH_LONG).show();

                return false;
            }

            long tamano = 0;

            try (InputStream entrada = getContentResolver().openInputStream(uri)) {

                if (entrada == null) {
                    return false;
                }

                byte[] buffer = new byte[8192];
                int leidos;

                while ((leidos = entrada.read(buffer)) != -1) {

                    tamano += leidos;

                    if (tamano > TAMANO_MAXIMO) {

                        Toast.makeText(this, "La imagen no puede superar 2 MB", Toast.LENGTH_LONG).show();

                        return false;
                    }
                }
            }

            return tamano > 0;

        } catch (Exception e) {

            Toast.makeText(this, "Error al verificar la imagen", Toast.LENGTH_LONG).show();

            return false;
        }
    }

    private void guardarProducto() {

        String nombre = etNombre.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioTexto = etPrecio.getText().toString().trim();

        if (nombre.isEmpty()) {

            etNombre.setError("Ingresa el nombre del producto");
            etNombre.requestFocus();
            return;
        }

        if (categoria.isEmpty()) {

            etCategoria.setError("Ingresa la categoría");
            etCategoria.requestFocus();
            return;
        }

        if (descripcion.isEmpty()) {

            etDescripcion.setError("Ingresa una descripción");
            etDescripcion.requestFocus();
            return;
        }

        if (precioTexto.isEmpty()) {

            etPrecio.setError("Ingresa el precio");
            etPrecio.requestFocus();
            return;
        }

        double precio;

        try {

            precio = Double.parseDouble(precioTexto);

        } catch (NumberFormatException e) {

            etPrecio.setError("Ingresa un precio válido");
            etPrecio.requestFocus();
            return;
        }

        if (!Double.isFinite(precio) || precio <= 0) {

            etPrecio.setError("El precio debe ser mayor que cero");
            etPrecio.requestFocus();
            return;
        }

        if (imagenSeleccionadaUri == null) {

            Toast.makeText(this, "Selecciona una imagen del producto", Toast.LENGTH_SHORT).show();

            return;
        }

        FirebaseUser usuario = firebaseAuth.getCurrentUser();

        if (usuario == null || !usuario.isEmailVerified()) {

            Toast.makeText(this, "Debes iniciar sesión con un correo verificado", Toast.LENGTH_LONG).show();

            return;
        }

        // Evitar guardar varias veces.
        btnGuardar.setEnabled(false);
        btnSeleccionarImagen.setEnabled(false);

        Toast.makeText(this, "Subiendo imagen del producto...", Toast.LENGTH_SHORT).show();

        // Primero subir la fotografía.
        CloudinaryManager.subirImagen(this, imagenSeleccionadaUri, new CloudinaryManager.SubidaCallback() {

            @Override
            public void onExito(String imagenUrl) {

                guardarEnFirestore(usuario.getUid(), nombre, categoria, descripcion, precio, imagenUrl);
            }

            @Override
            public void onError(String mensaje) {

                btnGuardar.setEnabled(true);
                btnSeleccionarImagen.setEnabled(true);
                Toast.makeText(CrearProductoVendedorActivity.this, "Error al subir imagen: " + mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarEnFirestore(String uidVendedor, String nombre, String categoria, String descripcion, double precio, String imagenUrl) {

        DocumentReference referencia = firestore.collection("productos").document();
        String idProducto = referencia.getId();
        Map<String, Object> datos = new HashMap<>();

        datos.put("idProducto", idProducto);
        datos.put("idVendedor", uidVendedor);
        datos.put("nombre", nombre);
        datos.put("categoria", categoria);
        datos.put("descripcion", descripcion);
        datos.put("precio", precio);
        datos.put("imagenUrl", imagenUrl);

        referencia.set(datos).addOnSuccessListener(unused -> {

            Toast.makeText(this, "Producto guardado en Firebase", Toast.LENGTH_SHORT).show();

            Intent resultado = new Intent();

            resultado.putExtra("idProducto", idProducto);
            resultado.putExtra("nombreProducto", nombre);
            resultado.putExtra("categoriaProducto", categoria);
            resultado.putExtra("descripcionProducto", descripcion);
            resultado.putExtra("precioProducto", precio);
            resultado.putExtra("imagenUrlProducto", imagenUrl);

            // Compatibilidad temporal con la lista local.
            resultado.putExtra("imagenUriProducto", imagenSeleccionadaUri.toString());

            setResult(RESULT_OK, resultado);

            finish();
        }).addOnFailureListener(e -> {

            btnGuardar.setEnabled(true);
            btnSeleccionarImagen.setEnabled(true);

            Toast.makeText(this, "Error al guardar en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
