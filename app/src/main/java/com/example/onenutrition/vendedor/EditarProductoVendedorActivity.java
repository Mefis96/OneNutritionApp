package com.example.onenutrition.vendedor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditarProductoVendedorActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etCategoria;
    private TextInputEditText etDescripcion;
    private TextInputEditText etPrecio;
    private ImageView imgProducto;
    private ImageButton btnVolver;
    private MaterialButton btnCambiarImagen;
    private MaterialButton btnGuardar;
    private String idProducto;
    private String idVendedor;
    private String imagenUrlActual;
    private Uri nuevaImagenUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final long TAMANO_MAXIMO = 2 * 1024 * 1024;

    private final ActivityResultLauncher<String[]> seleccionarImagenLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {

        if (uri == null) {
            return;
        }

        nuevaImagenUri = null;

        try {

            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (!imagenValida(uri)) {
                mostrarImagenActual();
                return;
            }

            nuevaImagenUri = uri;
            imgProducto.setImageURI(uri);

            Toast.makeText(this, "Nueva imagen seleccionada", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            mostrarImagenActual();

            Toast.makeText(this, "No se pudo seleccionar la imagen", Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto_vendedor);
        etNombre = findViewById(R.id.etNombreEditarProducto);
        etCategoria = findViewById(R.id.etCategoriaEditarProducto);
        etDescripcion = findViewById(R.id.etDescripcionEditarProducto);
        etPrecio = findViewById(R.id.etPrecioEditarProducto);
        imgProducto = findViewById(R.id.imgEditarProducto);
        btnVolver = findViewById(R.id.btnVolverEditarProducto);
        btnCambiarImagen = findViewById(R.id.btnCambiarImagenProducto);
        btnGuardar = findViewById(R.id.btnGuardarCambiosProducto);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        cargarDatosProducto();
        btnVolver.setOnClickListener(v -> finish());
        btnCambiarImagen.setOnClickListener(v -> seleccionarImagenLauncher.launch(new String[]{"image/jpeg", "image/png", "image/webp"}));
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void cargarDatosProducto() {

        Intent intent = getIntent();

        idProducto = intent.getStringExtra("idProducto");
        idVendedor = intent.getStringExtra("idVendedor");
        imagenUrlActual = intent.getStringExtra("imagenUrlProducto");
        etNombre.setText(intent.getStringExtra("nombreProducto"));
        etCategoria.setText(intent.getStringExtra("categoriaProducto"));
        etDescripcion.setText(intent.getStringExtra("descripcionProducto"));
        double precio = intent.getDoubleExtra("precioProducto", 0);
        etPrecio.setText(String.valueOf(precio));
        mostrarImagenActual();
    }

    private void mostrarImagenActual() {

        if (imagenUrlActual != null && !imagenUrlActual.isEmpty()) {

            Glide.with(this).load(imagenUrlActual).placeholder(R.drawable.proteina_whey).error(R.drawable.proteina_whey).into(imgProducto);

        } else {

            imgProducto.setImageResource(R.drawable.proteina_whey);
        }
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

    private void guardarCambios() {

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

            etDescripcion.setError("Ingresa la descripción");
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

        FirebaseUser usuario = firebaseAuth.getCurrentUser();

        if (usuario == null || !usuario.isEmailVerified()) {

            Toast.makeText(this, "Debes iniciar sesión con un correo verificado", Toast.LENGTH_LONG).show();

            return;
        }

        if (idProducto == null || idProducto.isEmpty() || !usuario.getUid().equals(idVendedor)) {

            Toast.makeText(this, "No tienes permiso para editar este producto", Toast.LENGTH_LONG).show();

            return;
        }

        btnGuardar.setEnabled(false);
        btnCambiarImagen.setEnabled(false);

        if (nuevaImagenUri != null) {

            Toast.makeText(this, "Subiendo nueva fotografía...", Toast.LENGTH_SHORT).show();

            CloudinaryManager.subirImagen(this, nuevaImagenUri, new CloudinaryManager.SubidaCallback() {

                @Override
                public void onExito(String imagenUrl) {

                    actualizarFirestore(nombre, categoria, descripcion, precio, imagenUrl);
                }

                @Override
                public void onError(String mensaje) {

                    habilitarBotones();

                    Toast.makeText(EditarProductoVendedorActivity.this, "Error al subir imagen: " + mensaje, Toast.LENGTH_LONG).show();
                }
            });

        } else {

            actualizarFirestore(nombre, categoria, descripcion, precio, imagenUrlActual);
        }
    }

    private void actualizarFirestore(String nombre, String categoria, String descripcion, double precio, String imagenUrl) {

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("nombre", nombre);
        cambios.put("categoria", categoria);
        cambios.put("descripcion", descripcion);
        cambios.put("precio", precio);
        cambios.put("imagenUrl", imagenUrl != null ? imagenUrl : "");

        firestore.collection("productos").document(idProducto).update(cambios).addOnSuccessListener(unused -> {

            Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {

            habilitarBotones();

            Toast.makeText(this, "Error al actualizar producto: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void habilitarBotones() {

        btnGuardar.setEnabled(true);
        btnCambiarImagen.setEnabled(true);
    }
}
