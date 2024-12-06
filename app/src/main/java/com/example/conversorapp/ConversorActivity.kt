package com.example.conversorapp

import android.content.ContentValues
import android.os.Bundle
import android.widget.*
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
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conversor)
        progressBar = findViewById(R.id.progressBar)
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
                    verificarRecursosSuficientes(moedaOrigem, valor) { temSuficiente ->
                        if (temSuficiente) {
                            progressBar.visibility = ProgressBar.VISIBLE
                            converterMoedas(moedaOrigem, moedaDestino, valor)
                        } else {
                            Toast.makeText(this, "Saldo insuficiente na moeda de origem", Toast.LENGTH_SHORT).show()
                        }
                    }
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

    private fun verificarRecursosSuficientes(moedaOrigem: String, valor: Float, callback: (Boolean) -> Unit) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT valor FROM recursos WHERE moeda = ?", arrayOf(moedaOrigem))

        if (cursor.moveToFirst()) {
            val saldo = cursor.getFloat(cursor.getColumnIndexOrThrow("valor"))
            callback(saldo >= valor)
        } else {
            callback(false)
        }
        cursor.close()
    }
    private fun requestAPI(moedaOrigem: String, moedaDestino: String, callback: (Float?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(AwesomeApiService::class.java)

        val call = service.getCotacao("$moedaOrigem-$moedaDestino")

        call.enqueue(object : Callback<Map<String, CotacaoResponse>> {
            override fun onResponse(call: Call<Map<String, CotacaoResponse>>, response: Response<Map<String, CotacaoResponse>>) {
                if (response.isSuccessful) {
                    val cotacaoResponse = response.body()?.values?.firstOrNull()
                    val bid = cotacaoResponse?.bid?.toFloatOrNull()
                    callback(bid)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Map<String, CotacaoResponse>>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun converterMoedas(moedaOrigem: String, moedaDestino: String, valor: Float) {
        if ((moedaOrigem == "BTC" || moedaOrigem == "ETH") && (moedaDestino == "BTC" || moedaDestino == "ETH")) {
            requestAPI(moedaOrigem, "BRL") { bidOrigem ->
                if (bidOrigem != null) {
                    requestAPI(moedaDestino, "BRL") { bidDestino ->
                        if (bidDestino != null) {
                            val valorEmBRL = valor*bidOrigem
                            val valorEmDestino = valorEmBRL/bidDestino
                            atualizarSaldo(moedaOrigem, moedaDestino, valor, valorEmDestino)
                            Toast.makeText(
                                this@ConversorActivity,
                                "Convertido para $valorEmDestino $moedaDestino",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@ConversorActivity,
                                "Falha ao obter cotação da moeda de destino",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@ConversorActivity,
                        "Falha ao obter cotação da moeda de origem",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            if (moedaDestino == "BTC" || moedaDestino == "ETH") {
                requestAPI(moedaDestino, moedaOrigem) { bidDestino ->
                    if (bidDestino != null) {
                        val valorConvertido = valor / bidDestino
                        atualizarSaldo(moedaOrigem, moedaDestino, valor, valorConvertido)
                        Toast.makeText(
                            this@ConversorActivity,
                            "Convertido para $valorConvertido $moedaDestino",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this@ConversorActivity, "Falha ao obter cotação da moeda de destino", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                requestAPI(moedaOrigem, moedaDestino) { bid ->
                    if (bid != null) {
                        val valorConvertido = valor * bid
                        atualizarSaldo(moedaOrigem, moedaDestino, valor, valorConvertido)
                        Toast.makeText(
                            this@ConversorActivity,
                            "Convertido para $valorConvertido $moedaDestino",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this@ConversorActivity, "Falha ao obter cotação", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        progressBar.visibility = ProgressBar.GONE    }


    private fun atualizarSaldo(moedaOrigem: String, moedaDestino: String, valorOrigem: Float, valorDestino: Float) {
        val db = dbHelper.writableDatabase
        db.execSQL("UPDATE recursos SET valor = valor - ? WHERE moeda = ?", arrayOf(valorOrigem, moedaOrigem))
        db.execSQL("UPDATE recursos SET valor = valor + ? WHERE moeda = ?", arrayOf(valorDestino, moedaDestino))
    }


}