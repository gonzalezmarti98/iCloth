package com.martigonzalez.project_icloth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.martigonzalez.project_icloth.auth.AuthManager // <-- ¡IMPORTAMOS NUESTRA CLASE!

class SignUpActivity : AppCompatActivity() {

    // 1. Declaramos nuestro gestor de lógica
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 2. Inicializamos el gestor
        authManager = AuthManager()

        val etEmail = findViewById<EditText>(R.id.et_email_sign_up)
        val etPassword = findViewById<EditText>(R.id.et_password_sign_up)
        val btnConfirm = findViewById<Button>(R.id.btn_sign_up_confirm)

        btnConfirm.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 3. Llamamos al método de AuthManager y esperamos el resultado
            authManager.signUpUser(email, password) { success, errorMessage ->
                if (success) {
                    // Éxito: la lógica de la UI se queda aquí
                    Toast.makeText(this, "Usuario creado. Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                    finish() // Vuelve a MainActivity
                } else {
                    // Error: la lógica de la UI se queda aquí
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
