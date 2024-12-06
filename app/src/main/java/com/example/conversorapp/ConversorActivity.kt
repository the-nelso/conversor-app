package com.example.conversorapp

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.conversorapp.data.AwesomeApiService
import com.example.conversorapp.data.CotacaoResponse
import com.example.conversorapp.data.DataBaseHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConversorActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversor)

        dbHelper = DataBaseHelper(this)
        val db = dbHelper.writableDatabase
        val btnConverter = findViewById<Button>(R.id.conversaoButton);
        val spinnerMoedaOrigem = findViewById<Spinner>(R.id.spinnerOrigem);
        val spinnerMoedaDestino = findViewById<Spinner>(R.id.spinnerDestino);
        val editTextValorConverter = findViewById<EditText>(R.id.editTextValor);

        val moedas = listOf("BRL", "USD", "EUR", "ETH", "BTC")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moedas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerMoedaOrigem.adapter = adapter
        spinnerMoedaDestino.adapter = adapter

        btnConverter.setOnClickListener {
            val moedaOrigem = spinnerMoedaOrigem.selectedItem.toString()
            val moedaDestino = spinnerMoedaDestino.selectedItem.toString()
            val valor = editTextValorConverter.text.toString().toFloatOrNull()

            if (valor != null && valor > 0) {
                if (moedaOrigem == moedaDestino) {
                    Toast.makeText(this, "Selecione moedas diferentes", Toast.LENGTH_SHORT).show()
                } else {
                    converterMoedas(moedaOrigem, moedaDestino, valor)
                }
            } else {
                Toast.makeText(this, "Informe um valor válido", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun converterMoedas(moedaOrigem: String, moedaDestino: String, valor: Float) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(AwesomeApiService::class.java)
        val call = service.getCotacao("$moedaOrigem-$moedaDestino")

        call.enqueue(object : Callback<Map<String, CotacaoResponse>> {
            override fun onResponse(
                call: Call<Map<String, CotacaoResponse>>,
                response: Response<Map<String, CotacaoResponse>>
            ) {
                if (response.isSuccessful) {
                    val cotacaoResponse = response.body()?.values?.firstOrNull()
                    if (cotacaoResponse != null) {
                        val bid = cotacaoResponse.bid.toFloatOrNull()
                        if (bid != null) {
                            val valorConvertido = valor * bid
                            atualizarSaldo(moedaOrigem, moedaDestino, valor, valorConvertido)
                            Toast.makeText(this@ConversorActivity, "Convertido para $valorConvertido $moedaDestino", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ConversorActivity, "Falha ao interpretar a cotação", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ConversorActivity, "Resposta inválida da API", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ConversorActivity, "Erro ao obter cotação: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, CotacaoResponse>>, t: Throwable) {
                Toast.makeText(this@ConversorActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
        finish()
    }

    private fun atualizarSaldo(moedaOrigem: String, moedaDestino: String, valorOrigem: Float, valorDestino: Float) {
        val db = dbHelper.writableDatabase
        db.execSQL("UPDATE recursos SET valor = valor - ? WHERE moeda = ?", arrayOf(valorOrigem, moedaOrigem))
        db.execSQL("UPDATE recursos SET valor = valor + ? WHERE moeda = ?", arrayOf(valorDestino, moedaDestino))
    }
}