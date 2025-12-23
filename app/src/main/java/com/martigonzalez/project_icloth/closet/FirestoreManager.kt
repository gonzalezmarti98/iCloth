package com.martigonzalez.project_icloth.closet

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreManager {

    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Guarda la información de una nueva prenda en la colección "clothes".
     *
     * @param clothData Un mapa con todos los datos de la prenda.
     * @param onResult Callback que se llama cuando la operación termina.
     */
    fun saveClothItem(clothData: Map<String, Any>, onResult: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            println("Error: Usuario no autenticado.")
            onResult(false)
            return
        }

        // Añadimos el userId a los datos antes de guardar
        val completeClothData = clothData.toMutableMap()
        completeClothData["userId"] = userId
        completeClothData["timestamp"] = System.currentTimeMillis()

        db.collection("clothes")
            .add(completeClothData) // Guardamos el mapa completo
            .addOnSuccessListener {
                println("Documento de prenda guardado con éxito con ID: ${it.id}")
                onResult(true)
            }
            .addOnFailureListener { e ->
                println("Error al guardar el documento de la prenda: $e")
                onResult(false)
            }
    }

}
