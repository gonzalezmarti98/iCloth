package com.martigonzalez.project_icloth.closet

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class StorageManager {
    private val storage = Firebase.storage
    private val auth = Firebase.auth

    /**
     * Sube un fichero a Firebase Storage y devuelve la URL de descarga pública (https://...).
     * @param uri La URI local del fichero a subir.
     * @param onComplete Lambda que se ejecuta al terminar, devolviendo la URL https:// o null si hay un error.
     */
    fun uploadImage(uri: Uri, onComplete: (String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(null)
            return
        }
        val fileRef = storage.reference.child("users/${user.uid}/clothes/${System.currentTimeMillis()}.jpg")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                // Una vez subida la imagen, pedimos la URL de descarga.
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Éxito: downloadUri.toString() contiene la URL "https://..."
                    onComplete(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    // La subida fue bien, pero falló al obtener la URL.
                    Log.e("StorageManager", "Error al obtener la URL de descarga", exception)
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                // La subida del archivo falló desde el principio.
                Log.e("StorageManager", "Error al subir la imagen", exception)
                onComplete(null)
            }
    }
}
