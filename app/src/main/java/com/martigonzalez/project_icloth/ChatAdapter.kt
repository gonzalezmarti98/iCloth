package com.martigonzalez.project_icloth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.martigonzalez.project_icloth.R
import com.martigonzalez.project_icloth.model.ChatMessage
import com.martigonzalez.project_icloth.model.ChatType

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val onTagsConfirmed: (List<String>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_AI_TEXT = 1
        private const val TYPE_AI_TAGS = 2
        private const val TYPE_AI_OUTFIT = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            ChatType.USER -> TYPE_USER
            ChatType.AI_TEXT -> TYPE_AI_TEXT
            ChatType.AI_TAGS -> TYPE_AI_TAGS
            ChatType.OUTFIT -> TYPE_AI_OUTFIT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> {
                val view = inflater.inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(view)
            }
            TYPE_AI_OUTFIT ->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_outfit, parent, false)
                OutfitViewHolder(view)
            }
            TYPE_AI_TAGS -> {
                val view = inflater.inflate(R.layout.item_chat_ai_tags, parent, false)
                AiTagsViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_chat_ai_text, parent, false)
                AiTextViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AiTextViewHolder -> holder.bind(message)
            is AiTagsViewHolder -> holder.bind(message, onTagsConfirmed)
            is OutfitViewHolder -> {
                holder.tvExplanation.text = message.text

                // Limpiamos las fotos anteriores (por el reciclaje de vistas)
                holder.llImagesContainer.removeAllViews()

                // Añadimos las fotos
                message.imageUrls.forEach { url ->
                    val imageView = ImageView(holder.itemView.context)

                    // Tamaño de cada foto (ej: 100x100 dp)
                    val size = (100 * holder.itemView.context.resources.displayMetrics.density).toInt()
                    val params = LinearLayout.LayoutParams(size, size)
                    params.setMargins(0, 0, 16, 0) // Margen derecho

                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    // Fondo gris por si tarda en cargar
                    imageView.setBackgroundColor(android.graphics.Color.DKGRAY)

                    // Cargamos la imagen con Glide
                    Glide.with(holder.itemView.context)
                        .load(url)
                        .into(imageView)

                    holder.llImagesContainer.addView(imageView)
                }
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.text
        }
    }

    class AiTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.text
        }
    }

    class OutfitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExplanation: TextView = view.findViewById(R.id.tvExplanation)
        val llImagesContainer: LinearLayout = view.findViewById(R.id.llImagesContainer)
    }


    // Clase interna para el ViewHolder de AiTags
    class AiTagsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroupTags)
        private val btnConfirm: Button = itemView.findViewById(R.id.btnConfirmTags)

        fun bind(msg: ChatMessage, onConfirm: (List<String>) -> Unit) {
            chipGroup.removeAllViews()

            for (tag in msg.tags) {
                val chip = Chip(itemView.context)
                chip.text = tag
                chip.isCheckable = true

                // --- APLICAR ESTILOS A LOS CHIPS (ETIQUETAS) ---

                // 1. Color de Fondo (Selector: Blanco normal / Azul seleccionado)
                chip.chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                    itemView.context,
                    R.color.selector_chip_bg
                )
                // 2. Color de Texto (Selector: Negro normal / Blanco seleccionado)
                chip.setTextColor(
                    androidx.core.content.ContextCompat.getColorStateList(
                        itemView.context,
                        R.color.selector_chip_text
                    )
                )
                // Opcional: Quitar el borde gris por defecto para un look más limpio
                chip.chipStrokeWidth = 0f

                // Añadir al grupo
                chipGroup.addView(chip)
            }

            btnConfirm.setOnClickListener {
                val selectedTags = mutableListOf<String>()
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        selectedTags.add(chip.text.toString())
                    }
                }
                onConfirm(selectedTags)

                // Efecto visual: deshabilitar botón para evitar doble click
                it.isEnabled = false
                it.alpha = 0.5f
            }
        }
    }

}
