package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onenutrition.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.content.Intent;
import com.example.onenutrition.auth.ActividadProtegida;

public class MetodoPagoActivity extends ActividadProtegida {

    private ImageButton btnVolver;
    private RadioButton radioTarjeta;
    private RadioButton radioEfectivo;
    private MaterialCardView cardTarjeta;
    private MaterialCardView cardEfectivo;
    private TextView tvInfoPago;
    private MaterialButton btnContinuar;
    private String metodoSeleccionado = "";
    private String direccion;
    private String barrio;
    private String ciudad;
    private String indicaciones;
    private Double latitud;
    private Double longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metodo_pago);
        Intent datosEntrega = getIntent();

        direccion = datosEntrega.getStringExtra("direccion");
        barrio = datosEntrega.getStringExtra("barrio");
        ciudad = datosEntrega.getStringExtra("ciudad");
        indicaciones = datosEntrega.getStringExtra("indicaciones");

        if (datosEntrega.hasExtra("latitud") && datosEntrega.hasExtra("longitud")) {

            latitud = datosEntrega.getDoubleExtra("latitud", 0);
            longitud = datosEntrega.getDoubleExtra("longitud", 0);
        }

        btnVolver = findViewById(R.id.btnVolverMetodoPago);
        radioTarjeta = findViewById(R.id.radioTarjeta);
        radioEfectivo = findViewById(R.id.radioEfectivo);
        cardTarjeta = findViewById(R.id.cardTarjeta);
        cardEfectivo = findViewById(R.id.cardEfectivo);
        tvInfoPago = findViewById(R.id.tvInfoPago);
        btnContinuar = findViewById(R.id.btnContinuarMetodoPago);
        btnVolver.setOnClickListener(v -> finish());
        configurarMetodosPago();
        btnContinuar.setOnClickListener(v -> continuar());
    }

    private void configurarMetodosPago() {

        radioTarjeta.setOnClickListener(v -> seleccionarTarjeta());
        cardTarjeta.setOnClickListener(v -> seleccionarTarjeta());
        radioEfectivo.setOnClickListener(v -> seleccionarEfectivo());
        cardEfectivo.setOnClickListener(v -> seleccionarEfectivo());
    }

    private void seleccionarTarjeta() {

        metodoSeleccionado = "Tarjeta";
        radioTarjeta.setChecked(true);
        radioEfectivo.setChecked(false);
        tvInfoPago.setText("Serás dirigido a la pasarela de pago para completar la transacción de forma segura.");
    }

    private void seleccionarEfectivo() {

        metodoSeleccionado = "Efectivo";
        radioEfectivo.setChecked(true);
        radioTarjeta.setChecked(false);
        tvInfoPago.setText("El pago se realizará en efectivo al momento de recibir tu pedido.");
    }

    private void continuar() {

        if (metodoSeleccionado.isEmpty()) {

            Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        if (metodoSeleccionado.equals("Tarjeta")) {

            Intent intent = new Intent(MetodoPagoActivity.this, PasarelaPagoActivity.class);
            startActivity(intent);


        } else {

            Intent intent = new Intent(MetodoPagoActivity.this, ConfirmacionPedidoActivity.class);

            // Método de pago
            intent.putExtra("metodoPago", "Efectivo contra entrega");

            // Datos de entrega
            intent.putExtra("direccion", direccion);
            intent.putExtra("barrio", barrio);
            intent.putExtra("ciudad", ciudad);
            intent.putExtra("indicaciones", indicaciones);

            // Coordenadas seleccionadas en Google Maps
            if (latitud != null && longitud != null) {
                intent.putExtra("latitud", latitud);
                intent.putExtra("longitud", longitud);
            }

            startActivity(intent);
            finish();
        }

    }
}