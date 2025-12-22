package com.martigonzalez.project_icloth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.martigonzalez.project_icloth.R
import com.martigonzalez.project_icloth.model.Prenda

class ClosetAdapter(
    private val listaPrendas: List<Prenda>,
    private val onItemClick: (Prenda) -> Unit // para detectar el "click" a la imagen
) : RecyclerView.Adapter<ClosetAdapter.PrendaViewHolder>() {

    class PrendaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPrenda: ImageView = view.findViewById(R.id.ivPrenda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prenda, parent, false)
        return PrendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {
        val prenda = listaPrendas[position]
        // Carga de imagen con Glide
        Glide.with(holder.itemView.context)
            .load(prenda.imagenUrl) // La URL que viene en tu objeto Prenda
            .placeholder(R.drawable.ic_launcher_background) // Imagen mientras carga (puedes crear un drawable gris)
            .error(android.R.drawable.ic_menu_close_clear_cancel) // Imagen si falla la carga o la URL es vacía
            .centerCrop() // Ajusta la imagen para llenar el ImageView sin deformarse
            .into(holder.ivPrenda)

        // 2. Configurar el click en el elemento entero (itemView)
        holder.itemView.setOnClickListener {
            onItemClick(prenda) // Ejecutamos la función pasando la prenda clickeada
        }
    }

    override fun getItemCount() = listaPrendas.size
}