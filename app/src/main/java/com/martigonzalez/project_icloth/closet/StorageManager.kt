package com.martigonzalez.project_icloth.closet

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class StorageManager {

    /**
     * Sube una imagen a Firebase Storage dentro de una carpeta específica para el usuario actual.
     * Al completarse, devuelve la URL de almacenamiento completa en formato 'gs://'.
     *
     * @param uri La URI local del archivo de imagen que se va a subir.
     * @param onComplete Una función lambda que se ejecuta al terminar. Recibe la URL gs:// como String,
     *                   o null si la subida falla.
     */
    fun uploadImage(uri: Uri, onComplete: (String?) -> Unit) {
        // 1. Obtener el ID del usuario actualmente autenticado.
        val userId = Firebase.auth.currentUser?.uid

        // Si no hay un usuario logueado, no se puede subir la imagen.
        if (userId == null) {
            onComplete(null) // Notifica el fallo.
            return
        }

        // 2. Crear una referencia en Firebase Storage.
        // La ruta es: /users/{ID_DEL_USUARIO}/clothes/{MARCA_DE_TIEMPO}.jpg
        // Esto organiza las imágenes de cada usuario en su propia carpeta privada.
        val imageRef = Firebase.storage.reference.child("users/$userId/clothes/${System.currentTimeMillis()}.jpg")

        // 3. Iniciar la subida del archivo.
        imageRef.putFile(uri)
            .addOnSuccessListener {
                // 4. ¡ÉXITO! La imagen se ha subido correctamente.
                // Ahora construimos la URL de almacenamiento (gs://) que necesita Glide.
                val bucket = imageRef.bucket
                val path = imageRef.path
                val storageUrl = "gs://$bucket$path" // Formato: "gs://tu-proyecto.appspot.com/users/..."

                // 5. Devolver la URL completa a través de la función de callback.
                onComplete(storageUrl)
            }
            .addOnFailureListener {
                // 6. FALLO. La subida ha fallado.
                // Devolvemos null para indicar el error.
                onComplete(null)
            }
    }
}
