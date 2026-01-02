package com.martigonzalez.project_icloth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.martigonzalez.project_icloth.model.Prenda

class ClosetAdapter(
    private val prendas: List<Prenda>,
    private val onClick: (Prenda) -> Unit
) : RecyclerView.Adapter<ClosetAdapter.PrendaViewHolder>() {

    // Crea la vista para cada ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prenda, parent, false)
        return PrendaViewHolder(view)
    }

    // Conecta los datos con la vista
    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {
        val prenda = prendas[position]
        holder.bind(prenda, onClick)
    }

    // Devuelve el número total de ítems
    override fun getItemCount(): Int = prendas.size

    // Clase interna que representa la vista de cada ítem
    class PrendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivPrenda)

        fun bind(prenda: Prenda, onClick: (Prenda) -> Unit) {
            Glide.with(itemView.context)
                .load(prenda.imagenUrl)
                .centerCrop()
                .into(imageView)

            itemView.setOnClickListener { onClick(prenda) }
        }
    }
}
