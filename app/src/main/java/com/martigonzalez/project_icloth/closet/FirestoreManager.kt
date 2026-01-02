package com.martigonzalez.project_icloth.closet

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.martigonzalez.project_icloth.model.Prenda // <-- IMPORT CORRECTO

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // ▼▼▼ FUNCIÓN MODIFICADA PARA ACEPTAR UN OBJETO Prenda ▼▼▼
    fun saveClothItem(prenda: Prenda, onComplete: (Boolean) -> Unit) {
        if (userId == null) {
            onComplete(false)
            return
        }
        db.collection("users").document(userId).collection("clothes")
            .add(prenda) // Firestore guardará el objeto directamente
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ▼▼▼ FUNCIÓN MODIFICADA PARA DEVOLVER UNA LISTA DE Prenda ▼▼▼
    fun getAllClothes(onResult: (List<Prenda>) -> Unit) {
        if (userId == null) {
            onResult(emptyList())
            return
        }

        db.collection("users").document(userId).collection("clothes")
            .get()
            .addOnSuccessListener { documents ->
                // Convierte cada documento en un objeto Prenda
                val prendas = documents.toObjects(Prenda::class.java)
                onResult(prendas)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
