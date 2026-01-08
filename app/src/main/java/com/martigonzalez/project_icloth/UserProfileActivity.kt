package com.martigonzalez.project_icloth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

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
        // BOTÓN CERRAR SESIÓN CON DIÁLOGO DE CONFIRMACIÓN
        btnLogout.setOnClickListener {
            // 1. Crear el constructor del diálogo
            MaterialAlertDialogBuilder(this)
                // 2. Configurar el diálogo
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar la sesión?")

                // 3. Añadir el botón de acción positiva ("Sí")
                .setPositiveButton("Sí") { dialog, which ->
                    // Código que se ejecuta si el usuario pulsa "Sí"
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                // 4. Añadir el botón de acción negativa ("No")
                .setNegativeButton("No") { dialog, which ->
                    // Simplemente cierra el diálogo, no hace falta hacer nada más
                    dialog.dismiss()
                }
                // 5. Mostrar el diálogo
                .show()
        }
    }
}