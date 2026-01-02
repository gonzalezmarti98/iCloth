package com.martigonzalez.project_icloth.closet

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class StorageManager {

    // CORREGIDO: 'storage' ahora es la referencia raíz a tu bucket de almacenamiento.
    private val storage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Sube una imagen al directorio del usuario actual y devuelve su URI de Firebase Storage (gs://).
     */
    fun uploadImage(imageUri: Uri, onComplete: (String?) -> Unit) {
        if (userId == null) {
            onComplete(null)
            return
        }

        // Creamos una ruta única para cada imagen para evitar sobreescribir archivos
        val fileName = "${UUID.randomUUID()}.jpg"

        // CORREGIDO: Se llama a .child() directamente sobre 'storage', que ya es una referencia.
        val storageRef = storage.child("users/$userId/clothes/$fileName")

        // Subimos el archivo
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Obtenemos la referencia URI del archivo (gs://).
                val gsUri = "gs://${storageRef.bucket}/${storageRef.path}"
                onComplete(gsUri)
            }
            .addOnFailureListener { exception ->
                // Opcional: Imprime el error para poder depurar si algo falla
                android.util.Log.e("StorageManager", "Error al subir imagen", exception)
                onComplete(null)
            }
    }
}
