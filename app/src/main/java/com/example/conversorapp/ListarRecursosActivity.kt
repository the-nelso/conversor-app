package com.example.conversorapp

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conversorapp.data.DataBaseHelper
import com.example.conversorapp.databinding.ActivityListarRecursosBinding

class ListarRecursosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper
    private lateinit var binding: ActivityListarRecursosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListarRecursosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DataBaseHelper(this)

        binding.recyclerViewRecursos.layoutManager = LinearLayoutManager(this)
        val db = dbHelper.readableDatabase

        val recursos = getRecursosFromDatabase()
        val adapter = RecursoAdapter(recursos)
        binding.recyclerViewRecursos.adapter = adapter
    }

    private fun getRecursosFromDatabase(): List<Recurso> {
        val recursos = mutableListOf<Recurso>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DataBaseHelper.TABLE_RECURSOS,
            arrayOf(DataBaseHelper.COLUMN_MOEDA, DataBaseHelper.COLUMN_VALOR),
            null, null, null, null, null
        )

        while (cursor.moveToNext()) {
            val moeda = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_MOEDA))
            val valor = cursor.getFloat(cursor.getColumnIndexOrThrow(DataBaseHelper.COLUMN_VALOR))
            recursos.add(Recurso(moeda, valor))
        }
        cursor.close()
        return recursos
    }
}