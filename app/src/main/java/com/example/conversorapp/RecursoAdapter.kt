package com.example.conversorapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.conversorapp.databinding.ItemRecursoBinding

data class Recurso(val moeda: String, val valor: Float)

class RecursoAdapter(private val recursos: List<Recurso>) :
    RecyclerView.Adapter<RecursoAdapter.RecursoViewHolder>() {

    inner class RecursoViewHolder(private val binding: ItemRecursoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recurso: Recurso) {
            binding.textViewMoeda.text = recurso.moeda
            binding.textViewValor.text = String.format("%.5f", recurso.valor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecursoViewHolder {
        val binding = ItemRecursoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecursoViewHolder, position: Int) {
        holder.bind(recursos[position])
    }

    override fun getItemCount(): Int = recursos.size
}
