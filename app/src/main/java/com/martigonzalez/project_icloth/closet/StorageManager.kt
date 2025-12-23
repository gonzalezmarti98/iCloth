package com.martigonzalez.project_icloth.closet

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class StorageManager {

    private val storage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Sube una imagen a Firebase Storage y devuelve su URL de descarga.
     *
     * @param imageUri La URI de la imagen seleccionada por el usuario. * @param onResult Callback que se llama con el resultado.
     *                 Devuelve la URL (String) si tiene éxito, o null si falla.
     */
    fun uploadImage(imageUri: Uri, onResult: (String?) -> Unit) {
        // Comprobamos que el usuario está logueado
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            println("Error: Usuario no autenticado, no se puede subir la imagen.")
            onResult(null)
            return
        }

        // Creamos un nombre de archivo único usando la hora actual
        val fileName = "${System.currentTimeMillis()}.jpg"
        // Definimos la ruta completa en Storage: clothes/{userId}/{fileName}
        val imageRef = storage.child("clothes/$userId/$fileName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                // La imagen se ha subido. Ahora obtenemos su URL de descarga.
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        // ¡Éxito! Tenemos la URL.
                        println("Imagen subida con éxito. URL: $uri")
                        onResult(uri.toString()) // Devolvemos la URL como un String
                    }
                    .addOnFailureListener { e ->
                        // Falló la obtención de la URL
                        println("Error al obtener la URL de descarga: $e")
                        onResult(null)
                    }
            }
            .addOnFailureListener { e ->
                // Falló la subida de la imagen
                println("Error al subir la imagen: $e")
                onResult(null)
            }
    }
}
