package com.martigonzalez.project_icloth.closet

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class StorageManager {
    private val storage = Firebase.storage
    private val auth = Firebase.auth

    fun uploadImage(uri: Uri, onComplete: (String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(null)
            return
        }
        val fileRef = storage.reference.child("users/${user.uid}/clothes/${System.currentTimeMillis()}.jpg")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                // --- ¡ESTE ES EL CAMBIO CLAVE! ---
                // Tras subir la imagen, obtenemos su URL de descarga (https://...)
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // La subida y la obtención de la URL fueron exitosas.
                    // downloadUri.toString() es la URL https:// que necesitamos.
                    onComplete(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    // La subida fue bien, pero falló al obtener la URL.
                    Log.e("StorageManager", "Error al obtener la URL de descarga", exception)
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                // La subida del archivo falló.
                Log.e("StorageManager", "Error al subir la imagen", exception)
                onComplete(null)
            }
    }
}
