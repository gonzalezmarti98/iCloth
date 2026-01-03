package com.martigonzalez.project_icloth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        // Marcamos visualmente el botón correcto
        bottomNav.selectedItemId = R.id.nav_profile

        // SELECTOR BOTONES BARRA DE NAVEGACIÓN
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_closet -> { // IR AL ARMARIO
                    startActivity(Intent(this, ClosetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_chat_ia -> { // IR AL CHAT IA
                    startActivity(Intent(this, ChatIaActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_add_cloth -> { // (+) AÑADIR
                    ClosetActivity().showImageSourceDialog()
                    false // Devuelve false para que el ítem no se mantenga seleccionado (es una acción puntual).
                    true
                }
                R.id.nav_news -> { // NEWS
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> { // ESTAMOS AQUÍ
                    true
                }
                else -> false
            }
        }
    }
}