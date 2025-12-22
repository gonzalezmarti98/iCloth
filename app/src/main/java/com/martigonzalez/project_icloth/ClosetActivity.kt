package com.martigonzalez.project_icloth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.martigonzalez.project_icloth.adapter.ClosetAdapter
import com.martigonzalez.project_icloth.model.Prenda

class ClosetActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var rvCloset: RecyclerView
    private lateinit var closetAdapter: ClosetAdapter
    private var listaPrendas: MutableList<Prenda> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        rvCloset = findViewById(R.id.rvCloset)
        
        // Configuramos GridLayoutManager con 2 columnas
        rvCloset.layoutManager = GridLayoutManager(this, 2)
        
        // Datos de prueba (puedes eliminarlos luego cuando conectes con Firebase/Database)
        cargarDatosDePrueba()

        // Al instanciar el adapter, pasamos la función lambda entre llaves {}
        closetAdapter = ClosetAdapter(listaPrendas) { prendaSeleccionada ->
            mostrarDialogoDetalle(prendaSeleccionada)
        }
        rvCloset.adapter = closetAdapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // aquí manejarás los ítems del menú inferior
            }
            true
        }
    }

    private fun mostrarDialogoDetalle(prenda: Prenda) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_prenda)

        // Esto hace que el fondo del dialog sea transparente para que se vean las esquinas redondeadas del CardView
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // Vinculamos las vistas del layout del diálogo
        val ivImagen = dialog.findViewById<ImageView>(R.id.ivDetalleImagen)
        val tvNombre = dialog.findViewById<TextView>(R.id.tvDetalleNombre)
        val tvCategoria = dialog.findViewById<TextView>(R.id.tvDetalleCategoria)
        val tvTemporada = dialog.findViewById<TextView>(R.id.tvDetalleTemporada)
        val btnCerrar = dialog.findViewById<ImageView>(R.id.btnCerrar)

        // Asignamos los datos
        tvNombre.text = prenda.nombre
        tvCategoria.text = "Categoría: ${prenda.categoria}"
        tvTemporada.text = "Temporada: ${prenda.temporada}"

        // Cargamos la imagen con Glide
        Glide.with(this)
            .load(prenda.imagenUrl)
            .centerCrop()
            .into(ivImagen)

        // Botón para cerrar
        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        //COnfiguración tamaño DIALOG
        val window = dialog.window
        // Configuramos el ancho al 95% de la pantalla (MATCH_PARENT suele ser muy ancho, mejor un poco menos)
        // O si prefieres todo el ancho, usa ViewGroup.LayoutParams.MATCH_PARENT
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(), // Ancho: 90% de la pantalla
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT       // Alto: Se ajusta al contenido
        )
    }

    private fun cargarDatosDePrueba() {
        listaPrendas.clear() //limpiamos x si se ha llamado + veces
        for (i in 1..10) {
            listaPrendas.add(
                Prenda(
                    id = "$i",
                    nombre = "Prenda $i",
                    imagenUrl = "https://picsum.photos/300/300?random=$i", //imagen random
                    categoria = "Camiseta",
                    colorPpal = "Blanco",
                    colorSec = listOf(),
                    formalLvl = 0,
                    deporteLvl = 0,
                    temporada = "Verano",
                    ajuste = "Regular"
                )
            )
        }
    }


}
