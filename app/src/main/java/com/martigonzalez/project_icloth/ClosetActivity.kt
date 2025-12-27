package com.martigonzalez.project_icloth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
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

        // SELECTOR BOTONES BARRA DE NAVEGACIÓN
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) { // ESTAMOS AQUÍ
                R.id.nav_closet -> {
                    true
                }
                R.id.nav_chat_ia -> { // CHAT IA
                    val intent = Intent(this, ChatIaActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_add -> { // (+) AÑADIR
                    //TODO --> Agregar que pida hacer foto o seleccionar de galería
                    true
                }
                R.id.nav_news -> { // NEWS
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> { // PERFIL USUARIO
                    val intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }

        }
    }

    private fun mostrarDialogoDetalle(prenda: Prenda) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_prenda)

        // Esto hace que el fondo del dialog sea transparente para que se vean las esquinas redondeadas del CardView
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Quitar el oscurecimiento (dimming) del fondo al abrirse el dialog
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)


        // Vinculamos las vistas del layout del diálogo
        val ivImagen = dialog.findViewById<ImageView>(R.id.ivDetalleImagen)
        //val tvNombre = dialog.findViewById<TextView>(R.id.tvDetalleNombre)
        val tvId = dialog.findViewById<TextView>(R.id.tvId)
        val tvCategoria = dialog.findViewById<TextView>(R.id.tvDetalleCategoria)
        val tvColorPpal = dialog.findViewById<TextView>(R.id.tvColorPpal)
        val tvColorSec = dialog.findViewById<TextView>(R.id.tvColorSec)
        val tvEstampado = dialog.findViewById<TextView>(R.id.tvEstampado)
        val tvAjuste = dialog.findViewById<TextView>(R.id.tvAjuste)
        val tvMarca = dialog.findViewById<TextView>(R.id.tvMarca)
        val tvFormal = dialog.findViewById<TextView>(R.id.tvFormal)
        val tvDeportivo = dialog.findViewById<TextView>(R.id.tvDeportivo)
        val tvTemporada = dialog.findViewById<TextView>(R.id.tvTemporada)
        //val btnCerrar = dialog.findViewById<ImageView>(R.id.btnCerrar)

        // Asignamos los datos
        //tvNombre.text = prenda.nombre
        tvId.text = "Id: ${prenda.id}"
        tvCategoria.text = "Categoría: ${prenda.categoria}"
        tvColorPpal.text = "Color principal: ${prenda.colorPpal}"
        tvColorSec.text = "Color secundario: ${prenda.colorSec}"
        tvEstampado.text = "Estampado: ${prenda.estampado}"
        tvAjuste.text = "Ajuste: ${prenda.ajuste}"
        tvMarca.text = "Marca: ${prenda.marca}"
        tvFormal.text = "Formalidad: ${prenda.formalLvl}"
        tvDeportivo.text = "Deportivo: ${prenda.deporteLvl}"
        tvTemporada.text = "Temporada: ${prenda.temporada}"



        // Cargamos la imagen con Glide
        Glide.with(this)
            .load(prenda.imagenUrl)
            .centerCrop()
            .into(ivImagen)


        dialog.setCanceledOnTouchOutside(true) //si pulsas fuera del Dialog, se cierra
        dialog.setCancelable(true) //dándole a "atrás" por defecto, se cierra

        dialog.show()

        //COnfiguración tamaño DIALOG
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels).toInt(), // Ancho de la pantalla
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT // Alto: Se ajusta al contenido
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
                    estampado = "Ninguno",
                    marca = "Desconocida",
                    formalLvl = 7,
                    deporteLvl = 0,
                    temporada = "Verano",
                    ajuste = "Regular"
                )
            )
        }
    }


}
