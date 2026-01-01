package com.martigonzalez.project_icloth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class NewsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        bottomNav.selectedItemId = R.id.nav_news

        // SELECTOR BOTONES BARRA DE NAVEGACIÓN
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_closet -> { // ARMARIO
                    startActivity(Intent(this, ClosetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_chat_ia -> { // CHAT IA
                    startActivity(Intent(this, ChatIaActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_add_cloth -> { // (+) AÑADIR
                    //TODO --> Agregar que pida hacer foto o seleccionar de galería
                    true
                }
                R.id.nav_profile -> { // PERFIL USUARIO
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_news -> { // ESTAMOS AQUÍ
                    true
                }
                else -> false
            }
        }
    }
}