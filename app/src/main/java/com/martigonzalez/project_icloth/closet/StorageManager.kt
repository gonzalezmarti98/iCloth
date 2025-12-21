package com.martigonzalez.project_icloth.closet

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class StorageManager {

    private val storage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Sube la imagen de una prenda a Firebase Storage.
     * @param imageUri La URI local de la imagen a subir.
     * @param onResult Se llama con el resultado:
     * - Boolean: true si la subida fue exitosa, false si no.
     * - String?: La URL de descarga de la imagen si fue exitosa, o un mensaje de error.
     */
    fun uploadClothImage(imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        if (userId == null) {
            onResult(false, "No se ha podido identificar al usuario.")
            return
        }

        // Creamos un nombre de archivo único (ej: 1234-abcd-5678.jpg)
        val fileName = "${UUID.randomUUID()}.jpg"
        // Creamos la ruta en Firebase: /images/[ID_DEL_USUARIO]/[NOMBRE_DEL_ARCHIVO]
        val imageRef = storage.child("images/$userId/$fileName")

        // Empezamos la subida
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Si la subida es exitosa, obtenemos la URL de descarga
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onResult(true, downloadUrl.toString())
                }
            }
            .addOnFailureListener { exception ->
                // Si la subida falla
                onResult(false, "Error al subir la imagen: ${exception.localizedMessage}")
            }
    }
}
