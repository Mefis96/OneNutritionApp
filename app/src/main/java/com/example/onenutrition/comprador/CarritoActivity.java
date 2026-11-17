package com.example.onenutrition.comprador;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.onenutrition.auth.ActividadProtegida;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenutrition.R;
import com.example.onenutrition.comprador.adapter.ItemCarritoAdapter;
import com.example.onenutrition.comprador.carrito.CarritoManager;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.Intent;

import com.google.android.material.button.MaterialButton;

public class CarritoActivity extends ActividadProtegida {

    private ImageButton btnVolverCarrito;
    private RecyclerView recyclerCarrito;
    private TextView tvSubtotal;
    private TextView tvTotal;
    private ItemCarritoAdapter carritoAdapter;
    private MaterialButton btnContinuarCompra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        btnVolverCarrito = findViewById(R.id.btnVolverCarrito);
        recyclerCarrito = findViewById(R.id.recyclerCarrito);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        btnVolverCarrito.setOnClickListener(v -> finish());
        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));
        carritoAdapter = new ItemCarritoAdapter(CarritoManager.getItems(), this::actualizarTotales);
        recyclerCarrito.setAdapter(carritoAdapter);

        actualizarTotales();

        btnContinuarCompra = findViewById(R.id.btnContinuarCompra);
        btnContinuarCompra.setOnClickListener(v -> {

            if (CarritoManager.getItems().isEmpty()) {
                return;
            }

            Intent intent = new Intent(CarritoActivity.this, DireccionEntregaActivity.class);
            startActivity(intent);
        });
    }

    private void actualizarTotales() {

        double subtotal = CarritoManager.calcularSubtotal();

        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        tvSubtotal.setText(formato.format(subtotal));

        double total = subtotal;

        tvTotal.setText(formato.format(total));
    }
}