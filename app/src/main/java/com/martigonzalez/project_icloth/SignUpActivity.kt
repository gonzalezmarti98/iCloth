package com.martigonzalez.project_icloth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUpConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)   // tu layout de registro

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.et_email_sign_up)
        etPassword = findViewById(R.id.et_password_sign_up)
        btnSignUpConfirm = findViewById(R.id.btn_sign_up_confirm)

        btnSignUpConfirm.setOnClickListener { doSignUp() }
    }

    private fun doSignUp() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email y contraseña obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Usuario creado correctamente", Toast.LENGTH_SHORT).show()
                    finish()    // vuelve al login
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Error al crear usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}