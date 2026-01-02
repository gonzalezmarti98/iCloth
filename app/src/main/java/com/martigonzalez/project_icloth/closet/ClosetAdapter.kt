package com.martigonzalez.project_icloth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
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

        holder.itemView.setOnClickListener {
            onClick(prenda)
        }

        if (prenda.imagenUrl.isNotEmpty()) {
            try {
                val storageReference =
                    Firebase.storage.getReferenceFromUrl(prenda.imagenUrl)

                com.bumptech.glide.Glide.with(holder.ivPrenda.context)
                    .load(storageReference)
                    .centerCrop()
                    .placeholder(R.color.grey_placeholder)
                    .error(R.drawable.ic_error_placeholder)
                    .into(holder.ivPrenda)

            } catch (e: Exception) {
                holder.ivPrenda.setImageResource(R.drawable.ic_error_placeholder)
            }
        } else {
            holder.ivPrenda.setImageResource(R.drawable.ic_error_placeholder)
        }
    }

    override fun getItemCount(): Int = prendas.size
}
