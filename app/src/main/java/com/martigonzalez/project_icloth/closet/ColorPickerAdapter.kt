// En ColorPickerAdapter.kt
package com.martigonzalez.project_icloth.closet // Asegúrate de que tu package sea el correcto
import com.martigonzalez.project_icloth.R
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

// Estructura de datos para un color. Se mantiene igual.
data class ColorOption(val name: String, val hexCode: String)

// El ViewHolder ahora es una clase de primer nivel, no una 'inner class'.
// Recibe el itemView como cualquier ViewHolder estándar.
class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val colorDot: View = itemView.findViewById(R.id.colorDot)
    private val selectionTick: ImageView = itemView.findViewById(R.id.ivSelectionTick)

    // La lógica para actualizar la vista de un item.
    fun bind(colorOption: ColorOption, isSelected: Boolean) {
        // Intenta obtener el fondo como un GradientDrawable para cambiar su color.
        val background = colorDot.background
        if (background is GradientDrawable) {
            background.setColor(Color.parseColor(colorOption.hexCode))
        }

        // Muestra u oculta el 'tick' de selección.
        selectionTick.visibility = if (isSelected) View.VISIBLE else View.GONE
    }
}

// El Adapter ahora es más simple, sin una clase anidada.
class ColorPickerAdapter(
    private val context: Context,
    private val colors: List<ColorOption>
) : RecyclerView.Adapter<ColorViewHolder>() { // <-- Apunta a la nueva clase ColorViewHolder

    private var selectedPosition = 0

    // Crea el ViewHolder inflando el layout del item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color_dot, parent, false)
        return ColorViewHolder(view)
    }

    // Vincula los datos con el ViewHolder en una posición específica.
    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val colorOption = colors[position]
        // Llama a la función 'bind' del ViewHolder.
        holder.bind(colorOption, position == selectedPosition)

        // Gestiona el clic en un item.
        holder.itemView.setOnClickListener {
            if (selectedPosition == position) return@setOnClickListener

            // Guarda la posición antigua para poder "desmarcarla".
            val oldSelectedPosition = selectedPosition
            // Actualiza la nueva posición seleccionada.
            selectedPosition = position

            // Notifica al RecyclerView que debe redibujar los dos items que han cambiado.
            notifyItemChanged(oldSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    // Devuelve el número total de items.
    override fun getItemCount(): Int = colors.size

    // Función pública para que la Activity pueda saber qué color se ha seleccionado.
    fun getSelectedColor(): ColorOption {
        return colors[selectedPosition]
    }
}
