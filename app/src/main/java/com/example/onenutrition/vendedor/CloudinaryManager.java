package com.example.onenutrition.vendedor;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudinaryManager {

    private static final String CLOUD_NAME = "jcprngi5";

    private static final String PRESET_PRODUCTOS = "onenutrition_productos";

    private static final String PRESET_PERFILES = "onenutrition_perfiles";

    public interface SubidaCallback {
        void onExito(String imagenUrl);

        void onError(String mensaje);
    }

    // Subir imágenes de productos.
    // Conserva la compatibilidad con el módulo vendedor.
    public static void subirImagen(Context context, Uri imagenUri, SubidaCallback callback) {

        subirImagenConPreset(context, imagenUri, PRESET_PRODUCTOS, callback);
    }

    // Subir fotografías de perfil.
    public static void subirFotoPerfil(Context context, Uri imagenUri, SubidaCallback callback) {

        subirImagenConPreset(context, imagenUri, PRESET_PERFILES, callback);
    }

    // Método reutilizable para los dos tipos de imágenes.
    private static void subirImagenConPreset(Context context, Uri imagenUri, String uploadPreset, SubidaCallback callback) {

        if (context == null || imagenUri == null || callback == null) {
            throw new IllegalArgumentException("Los parámetros de subida son obligatorios");
        }

        new Thread(() -> {

            HttpURLConnection conexion = null;

            try {

                String endpoint = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
                conexion = (HttpURLConnection) new URL(endpoint).openConnection();
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);
                conexion.setConnectTimeout(15000);
                conexion.setReadTimeout(30000);
                String boundary = "OneNutritionBoundary" + System.currentTimeMillis();
                conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream salida = conexion.getOutputStream()) {

                    // Enviar el preset correspondiente.
                    escribirTexto(salida, "--" + boundary + "\r\n" + "Content-Disposition: form-data; " + "name=\"upload_preset\"\r\n\r\n" + uploadPreset + "\r\n");

                    // Preparar el archivo de imagen.
                    escribirTexto(salida, "--" + boundary + "\r\n" + "Content-Disposition: form-data; " + "name=\"file\"; " + "filename=\"imagen.jpg\"\r\n" + "Content-Type: application/octet-stream\r\n\r\n");

                    // Leer la imagen seleccionada.
                    try (InputStream entrada = context.getContentResolver().openInputStream(imagenUri)) {

                        if (entrada == null) {
                            throw new Exception("No se pudo abrir la imagen");
                        }

                        byte[] buffer = new byte[8192];
                        int bytesLeidos;
                        while ((bytesLeidos = entrada.read(buffer)) != -1) {

                            salida.write(buffer, 0, bytesLeidos);
                        }
                    }

                    // Finalizar la petición multipart.
                    escribirTexto(salida, "\r\n--" + boundary + "--\r\n");
                }

                int codigo = conexion.getResponseCode();
                InputStream respuestaStream = codigo >= 200 && codigo < 300 ? conexion.getInputStream() : conexion.getErrorStream();
                String respuesta;

                try (InputStream entrada = respuestaStream) {

                    if (entrada == null) {
                        throw new Exception("Cloudinary no devolvió una respuesta");
                    }

                    ByteArrayOutputStream bufferRespuesta = new ByteArrayOutputStream();

                    byte[] buffer = new byte[8192];
                    int bytesLeidos;
                    while ((bytesLeidos = entrada.read(buffer)) != -1) {

                        bufferRespuesta.write(buffer, 0, bytesLeidos);
                    }

                    respuesta = new String(bufferRespuesta.toByteArray(), StandardCharsets.UTF_8);
                }

                if (codigo >= 200 && codigo < 300) {

                    JSONObject json = new JSONObject(respuesta);
                    String imagenUrl = json.getString("secure_url");
                    new Handler(Looper.getMainLooper()).post(() -> callback.onExito(imagenUrl));

                } else {

                    throw new Exception("Error de Cloudinary (" + codigo + "): " + respuesta);
                }

            } catch (Exception e) {

                String mensaje = e.getMessage() != null ? e.getMessage() : "Error desconocido al subir la imagen";
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(mensaje));

            } finally {

                if (conexion != null) {
                    conexion.disconnect();
                }
            }

        }).start();
    }

    // Escribir texto en la petición HTTP.
    private static void escribirTexto(OutputStream salida, String texto) throws Exception {

        salida.write(texto.getBytes(StandardCharsets.UTF_8));
    }
}
