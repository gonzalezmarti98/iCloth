package com.martigonzalez.project_icloth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)

        // Esperar X segundos y luego ir a la pantalla de inicio
        Handler(Looper.getMainLooper()).postDelayed({
            // Ir al Login (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Matar el splash para no poder volver atrás
        }, 1250)
    }
}
