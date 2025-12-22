package com.martigonzalez.project_icloth.closet

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreManager {

    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Guarda la información de una nueva prenda en la colección "clothes" de Firestore.
     *
     * @param imageUrl La URL de la imagen que se obtuvo de Firebase Storage.
     * @param onResult Un callback que se llama cuando la operación termina.
     *                 Devuelve 'true' si fue exitoso, 'false' en caso de error.
     */
    fun saveClothItem(imageUrl: String, onResult: (Boolean) -> Unit) {
        // Comprobación de seguridad: nos aseguramos de que hay un usuario logueado.
        if (userId == null) {
            println("Error: Usuario no autenticado, no se puede guardar la prenda.")
            onResult(false)
            return
        }

        // Creamos un "mapa" o "diccionario" con los datos de la prenda.
        // Este mapa se convertirá en los campos del documento en Firestore.
        val clothData = hashMapOf(
            "userId" to userId,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
            // Futuro: Aquí podrías añadir más datos, como:
            // "category" to "T-Shirt",
            // "color" to "Blue"
        )

        // Añadimos un nuevo documento a la colección "clothes".
        // Firestore generará un ID único para este documento automáticamente.
        db.collection("clothes")
            .add(clothData)
            .addOnSuccessListener {
                // Éxito: El documento se guardó correctamente.
                println("Documento de prenda guardado con éxito con ID: ${it.id}")
                onResult(true)
            }
            .addOnFailureListener { e ->
                // Error: Algo salió mal.
                println("Error al guardar el documento de la prenda: $e")
                onResult(false)
            }
    }
}
