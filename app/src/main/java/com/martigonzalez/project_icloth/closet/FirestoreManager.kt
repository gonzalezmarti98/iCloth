package com.martigonzalez.project_icloth.closet

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.martigonzalez.project_icloth.model.Prenda

class FirestoreManager {
    // Obtenemos la instancia de la base de datos
    private val db = FirebaseFirestore.getInstance()

    // --- ELIMINAMOS la propiedad 'userId' de aquí ---

    /**
     * Guarda un objeto Prenda en la subcolección 'clothes' del usuario actual.
     * Esta versión es robusta: primero obtiene el userId actual y luego se asegura
     * de que el documento del usuario exista antes de guardar.
     */
    fun saveClothItem(prenda: Prenda, onComplete: (Boolean) -> Unit) {
        // Obtenemos el usuario justo en el momento de la llamada.
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Si en este momento no hay usuario, entonces la operación falla.
        if (userId == null) {
            onComplete(false)
            return
        }

        // --- Lógica robusta para crear el documento del usuario si no existe ---
        val userDocRef = db.collection("users").document(userId)

        // Esto crea el documento del usuario si no existe, o no hace nada si ya existe.
        userDocRef.set(emptyMap<String, Any>(), SetOptions.merge())
            .addOnSuccessListener {
                // Ahora que el documento del usuario existe, añadimos la prenda a su subcolección
                userDocRef.collection("clothes")
                    .add(prenda) // Firestore puede manejar el objeto 'prenda' directamente
                    .addOnSuccessListener {
                        // Éxito final: la prenda se ha guardado
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        // Fallo al guardar la prenda
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                // Fallo al crear/verificar el documento del usuario
                onComplete(false)
            }
    }

    /**
     * Recupera todas las prendas de la subcolección 'clothes' del usuario actual.
     */
    fun getAllClothes(onResult: (List<Prenda>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            onResult(emptyList())
            return
        }

        db.collection("users").document(userId).collection("clothes")
            .get()
            .addOnSuccessListener { result ->
                // Creamos una lista manual
                val listaPrendas = mutableListOf<Prenda>()

                // Recorremos cada documento uno a uno
                for (document in result) {
                    // 1. Convertimos los datos de dentro
                    val prenda = document.toObject(Prenda::class.java)

                    // 2. ¡AQUÍ ESTÁ LA MAGIA! Le pegamos el ID del documento
                    prenda.id = document.id

                    listaPrendas.add(prenda)
                }

                // Devolvemos la lista ya con los IDs rellenos
                onResult(listaPrendas)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // Borrar prendas
    fun deleteCloth(prendaId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).collection("clothes")
            .document(prendaId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

}
