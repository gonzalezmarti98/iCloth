package com.martigonzalez.project_icloth.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Comprueba si hay un usuario actualmente autenticado.
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * LOGIN USER
     * Realiza el proceso de login con email y contraseña.
     * @param onResult Una función que se llamará con el resultado:
     * - Boolean: true si el login fue exitoso, false si no.
     * - String?: Un mensaje de error si lo hubo, o null si fue exitoso.
     */
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false, "Email y contraseña son obligatorios")
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null) // Éxito
                } else {
                    // Error, devolvemos el mensaje de la excepción
                    onResult(false, "Error al iniciar sesión: ${task.exception?.localizedMessage}")
                }
            }
    }

    /**
     * REGISTRAR USER
     * Registra un nuevo usuario con email y contraseña.
     * @param onResult Una función que se llamará con el resultado:
     *                 - Boolean: true si el registro fue exitoso, false si no.
     *                 - String?: Un mensaje de error si lo hubo, o null si fue exitoso.
     */
    fun signUpUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false, "Email y contraseña son obligatorios")
            return
        }

        if (password.length < 6) {
            onResult(false, "La contraseña debe tener al menos 6 caracteres")
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null) // Éxito
                } else {
                    // Error, devolvemos el mensaje de la excepción
                    onResult(false, "Error al registrar: ${task.exception?.localizedMessage}")
                }
            }
    }
    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }

}
