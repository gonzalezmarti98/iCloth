package com.martigonzalez.project_icloth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.martigonzalez.project_icloth.auth.AuthManager // <-- ¡IMPORTAMOS LA NUEVA CLASE!
import com.martigonzalez.project_icloth.closet.ClosetActivity

class MainActivity : AppCompatActivity() {

    // Ya no necesitamos la variable 'auth' de Firebase aquí.
    private lateinit var authManager: AuthManager

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicializamos nuestro nuevo gestor de lógica
        authManager = AuthManager()

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_Login)
        btnSignUp = findViewById(R.id.btn_SingUp)

        // 2. La comprobación inicial ahora usa el método de AuthManager
        if (authManager.getCurrentUser() != null) {
            goToCloset()
        }

        btnLogin.setOnClickListener {
            // 3. La función de login ahora es mucho más simple
            loginUser()
        }

        btnSignUp.setOnClickListener {
            goToSignUp()
        }
    }

    //LOGIN
    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // 4. Llamamos a nuestra clase externa y esperamos el resultado.
        // La MainActivity ya no sabe NADA de Firebase.
        authManager.loginUser(email, password) { success, errorMessage ->
            if (success) {
                goToCloset()
            } else {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Las funciones de navegación no cambian
    private fun goToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun goToCloset() {
        val intent = Intent(this, ClosetActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
