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

        // ★ RECYCLERVIEW (nueva funcionalidad del compañero)
        rvCloset = findViewById(R.id.rvCloset)
        rvCloset.layoutManager = GridLayoutManager(this, 2)
        cargarDatosDePrueba()
        closetAdapter = ClosetAdapter(listaPrendas) { prendaSeleccionada ->
            mostrarDialogoDetalle(prendaSeleccionada)
        }
        rvCloset.adapter = closetAdapter

        // ★ BOTTOM NAVIGATION (funcional completa del compañero)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_closet -> true
                R.id.nav_chat_ia -> {
                    startActivity(Intent(this, ChatIaActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_add_cloth -> {
                    // TODO: Añadir foto/galería
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // ★ DIALOGO DETALLE (nueva funcionalidad del compañero)
    private fun mostrarDialogoDetalle(prenda: Prenda) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_prenda)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val ivImagen = dialog.findViewById<ImageView>(R.id.ivDetalleImagen)
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

        Glide.with(this)
            .load(prenda.imagenUrl)
            .centerCrop()
            .into(ivImagen)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        val window = dialog.window
        window?.setLayout(
            resources.displayMetrics.widthPixels,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // ★ DATOS DE PRUEBA (nueva funcionalidad del compañero)
    private fun cargarDatosDePrueba() {
        listaPrendas.clear()
        for (i in 1..10) {
            listaPrendas.add(
                Prenda(
                    id = "$i",
                    nombre = "Prenda $i",
                    imagenUrl = "https://picsum.photos/300/300?random=$i",
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
