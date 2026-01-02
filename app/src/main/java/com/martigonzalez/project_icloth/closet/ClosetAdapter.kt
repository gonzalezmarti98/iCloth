package com.martigonzalez.project_icloth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Importación necesaria para usar Glide
import com.martigonzalez.project_icloth.model.Prenda

/**
 * Adaptador para el RecyclerView que muestra la colección de prendas en el armario (Closet).
 *
 * @property prendas Lista de objetos [Prenda] que se mostrarán.
 * @property onClick Lambda que se ejecuta cuando el usuario hace clic en una prenda.
 */
class ClosetAdapter(
    // La lista de prendas que el adaptador manejará. La hacemos 'var' para poder actualizarla.
    private var prendas: List<Prenda>,
    // Una función que se pasará desde la actividad para manejar los clics en los ítems.
    private val onClick: (Prenda) -> Unit
) : RecyclerView.Adapter<ClosetAdapter.ViewHolder>() {

    /**
     * ViewHolder contiene las referencias a las vistas de cada ítem del RecyclerView.
     * En este caso, solo contiene la referencia al ImageView donde se carga la imagen de la prenda.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPrenda: ImageView = view.findViewById(R.id.ivPrenda)
    }

    /**
     * Se llama cuando el RecyclerView necesita un nuevo [ViewHolder].
     * Infla el layout del ítem (`item_prenda.xml`) y crea una instancia del ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prenda, parent, false)
        return ViewHolder(view)
    }

    /**
     * Se llama para mostrar los datos en una posición específica.
     * Vincula los datos del objeto [Prenda] con las vistas del [ViewHolder].
     *
     * @param holder El ViewHolder que debe ser actualizado.
     * @param position La posición del ítem en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtenemos la prenda actual de la lista.
        val prenda = prendas[position]

        // Configuramos el listener para el clic en todo el ítem.
        holder.itemView.setOnClickListener {
            onClick(prenda)
        }

        // --- Carga de la imagen con Glide ---
        // Se comprueba que la URL no esté vacía y que sea una URL web (empiece por "https").
        if (prenda.imagenUrl.isNotEmpty() && prenda.imagenUrl.startsWith("https")) {
            Glide.with(holder.itemView.context) // Usamos el contexto del ViewHolder.
                .load(prenda.imagenUrl)           // Cargamos la URL de la imagen.
                .centerCrop()                     // Escala la imagen para que ocupe toda la vista, recortando si es necesario.
                .placeholder(R.color.grey_placeholder) // Muestra un color mientras carga la imagen.
                .error(R.drawable.ic_error_placeholder) // Muestra una imagen de error si la carga falla.
                .into(holder.ivPrenda)            // El ImageView de destino.
        } else {
            // Si la URL es inválida, se muestra una imagen de error por defecto.
            holder.ivPrenda.setImageResource(R.drawable.ic_error_placeholder)
        }
    } // <- Faltaba una llave de cierre aquí.

    /**
     * Devuelve el número total de ítems en la lista de datos.
     */
    override fun getItemCount(): Int = prendas.size

    /**
     * Método público para actualizar la lista de prendas del adaptador desde la actividad.
     * Es más eficiente que crear un nuevo adaptador cada vez que los datos cambian.
     *
     * @param nuevasPrendas La nueva lista de prendas a mostrar.
     */
    fun updatePrendas(nuevasPrendas: List<Prenda>) {
        this.prendas = nuevasPrendas
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado y debe redibujarse.
    }
}
