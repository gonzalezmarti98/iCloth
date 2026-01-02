package com.martigonzalez.project_icloth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.animation.with
import androidx.compose.ui.semantics.error
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.compose.material.placeholder
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.martigonzalez.project_icloth.model.Prenda

class ClosetAdapter(
    private val prendas: List<Prenda>,
    private val onClick: (Prenda) -> Unit
) : RecyclerView.Adapter<ClosetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPrenda: ImageView = view.findViewById(R.id.ivPrenda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prenda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prenda = prendas[position]
        holder.itemView.setOnClickListener { onClick(prenda) }

        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN CLAVE! ---
        if (prenda.imagenUrl.isNotEmpty()) {
            try {
                // 1. Convierte la URL 'gs://...' en una referencia de Storage
                val storageReference = Firebase.storage.getReferenceFromUrl(prenda.imagenUrl)

                // 2. Pasa la referencia directamente a Glide.
                //    La librería firebase-ui-storage se encarga de la "traducción".
                GlideApp.with(holder.ivPrenda.context)
                    .load(storageReference)
                    .centerCrop()
                    .placeholder(R.color.grey_placeholder)
                    .error(R.drawable.ic_error_placeholder)
                    .into(holder.ivPrenda)
            } catch (e: Exception) {
                // Si la URL es inválida (poco probable pero posible), muestra el error
                holder.ivPrenda.setImageResource(R.drawable.ic_error_placeholder)
            }
        } else {
            // Si la URL está vacía, muestra el error
            holder.ivPrenda.setImageResource(R.drawable.ic_error_placeholder)
        }
    }

    override fun getItemCount() = prendas.size
}
