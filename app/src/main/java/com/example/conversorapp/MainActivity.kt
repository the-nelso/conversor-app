package com.example.conversorapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.conversorapp.data.DataBaseHelper
import com.example.conversorapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DataBaseHelper(this)

        binding.depositarButton.setOnClickListener {
            startActivity(Intent(this, DepositoActivity::class.java))
        }

        binding.listarButton.setOnClickListener {
            startActivity(Intent(this, ListarRecursosActivity::class.java))
        }

        binding.converterButton.setOnClickListener {
            startActivity(Intent(this, ConversorActivity::class.java))
        }

        atualizarSaldo()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun atualizarSaldo() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DataBaseHelper.TABLE_RECURSOS,
            arrayOf("valor"),
            "moeda = ?",
            arrayOf("BRL"),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val saldo = cursor.getFloat(cursor.getColumnIndexOrThrow("valor"))
            binding.saldoText.text = String.format("R$ %.2f", saldo)
        }
        cursor.close()
    }

    override fun onResume() {
        super.onResume()
        atualizarSaldo()
    }
}