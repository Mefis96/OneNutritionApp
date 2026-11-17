package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.onenutrition.R;
import com.example.onenutrition.comprador.carrito.CarritoManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.NumberFormat;
import java.util.Locale;
import android.content.Intent;
import com.example.onenutrition.auth.ActividadProtegida;

public class PasarelaPagoActivity extends ActividadProtegida {

    private ImageButton btnVolver;
    private TextInputEditText etTitular;
    private TextInputEditText etNumeroTarjeta;
    private TextInputEditText etFechaVencimiento;
    private TextInputEditText etCvv;
    private TextView tvTotalPasarela;
    private MaterialButton btnPagar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasarela_pago);
        btnVolver = findViewById(R.id.btnVolverPasarela);
        etTitular = findViewById(R.id.etTitular);
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        etCvv = findViewById(R.id.etCvv);
        tvTotalPasarela = findViewById(R.id.tvTotalPasarela);
        btnPagar = findViewById(R.id.btnPagar);
        btnVolver.setOnClickListener(v -> finish());
        mostrarTotal();
        btnPagar.setOnClickListener(v -> validarPago());
    }

    private void mostrarTotal() {

        double total = CarritoManager.calcularSubtotal();

        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        tvTotalPasarela.setText(formato.format(total));
    }

    private void validarPago() {

        String titular = etTitular.getText().toString().trim();
        String numero = etNumeroTarjeta.getText().toString().trim();
        String fecha = etFechaVencimiento.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        if (titular.isEmpty()) {
            etTitular.setError("Ingresa el nombre del titular");
            etTitular.requestFocus();
            return;
        }

        if (numero.length() != 16) {
            etNumeroTarjeta.setError("La tarjeta debe tener 16 dígitos");
            etNumeroTarjeta.requestFocus();
            return;
        }

        if (!fecha.matches("\\d{2}/\\d{2}")) {
            etFechaVencimiento.setError("Usa el formato MM/AA");
            etFechaVencimiento.requestFocus();
            return;
        }

        if (cvv.length() < 3) {
            etCvv.setError("CVV inválido");
            etCvv.requestFocus();
            return;
        }

        Toast.makeText(this, "Pago simulado correctamente", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PasarelaPagoActivity.this, ConfirmacionPedidoActivity.class);
        intent.putExtra("metodoPago", "Tarjeta");
        startActivity(intent);
        finish();


    }
}