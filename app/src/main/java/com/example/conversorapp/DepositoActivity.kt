package com.example.conversorapp

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.conversorapp.data.DataBaseHelper

class DepositoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_deposito)

        val dbHelper = DataBaseHelper(this)
        val db = dbHelper.writableDatabase;
        val btnDepositar = findViewById<Button>(R.id.confirmarButton)
        val editTextDeposito = findViewById<EditText>(R.id.editTextDeposito)

        btnDepositar.setOnClickListener {
            val valor = editTextDeposito.text.toString().toFloatOrNull()
            if (valor != null && valor > 0) {
                val db = dbHelper.writableDatabase
                db.execSQL("UPDATE recursos SET valor = valor + ? WHERE moeda = 'BRL'", arrayOf(valor))
                finish()
            } else {
                Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}