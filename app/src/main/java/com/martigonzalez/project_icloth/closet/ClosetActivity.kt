package com.martigonzalez.project_icloth.closet

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martigonzalez.project_icloth.R
import java.io.File

class ClosetActivity : AppCompatActivity() {
    // Variable para guardar la URI temporal de la cámara.
    private var tempImageUri: Uri? = null
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var storageManager: StorageManager

    // LANZADOR PARA LA GALERÍA: Cuando el usuario elige una imagen, la subimos.
    private val selectImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(it)
        }
    }

    // LANZADOR PARA LA CÁMARA: Cuando el usuario toma una foto, la subimos.
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // La URI se genera justo antes de abrir la cámara.
            // Si la foto se tomó con éxito, la URI temporal ya apunta a la imagen.
            tempImageUri?.let {
                uploadImage(it)
            }
        }
    }

    // LANZADOR PARA EL PERMISO DE CÁMARA: Si se concede, abrimos la cámara.
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet)

        //inicializamos los manager
        storageManager = StorageManager()
        firestoreManager = FirestoreManager()

        bottomNav = findViewById(R.id.bottom_navigation_view)

        // Escuchamos el clic en la barra de navegación.
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // ESTE ES EL COMPORTAMIENTO QUE QUIERES
                R.id.nav_add_cloth -> {
                    showImageSourceDialog()
                    true // Evento consumido
                }
                // Aquí puedes añadir el comportamiento para otros botones si quieres
                // R.id.nav_home -> { ... }
                else -> false
            }
        }
    }

    // Muestra el diálogo para elegir entre Cámara o Galería.
    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto con la cámara", "Elegir de la galería")
        AlertDialog.Builder(this)
            .setTitle("Añadir nueva prenda")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission.launch(Manifest.permission.CAMERA) // Opción Cámara
                    1 -> selectImageFromGallery.launch("image/*") // Opción Galería
                }
            }
            .setNegativeButton("Cancelar", null) // Botón para cancelar
            .show()
    }

    // Abre la cámara del dispositivo.
    private fun openCamera() {
        val imageUri = createImageUri()     // 1. Creamos la Uri en una variable local (no nula)
        tempImageUri = imageUri              // 2. La guardamos en la variable de la clase para usarla después
        takePicture.launch(imageUri)    // 3. Le pasamos la variable local (no nula) al lanzador
    }


    // Crea una URI temporal para la foto de la cámara.
    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photo_temp.png")
        return FileProvider.getUriForFile(
            this,
            "com.martigonzalez.project_icloth.fileprovider", // Debe coincidir con tu AndroidManifest.xml
            image
        )
    }

    // Llama al StorageManager para que haga el trabajo de subida.
    private fun uploadImage(uri: Uri) {
        Toast.makeText(this, "Subiendo prenda...", Toast.LENGTH_SHORT).show()

        // La única responsabilidad de la app es subir la imagen.
        // La Cloud Function se encargará de crear el documento en Firestore y analizarlo.
        storageManager.uploadClothImage(uri) { success, result ->
            if (success) {
                Toast.makeText(this, "¡Prenda enviada para análisis!", Toast.LENGTH_LONG).show()
                // No necesitamos hacer nada más aquí. La función en la nube se activa sola.
            } else {
                Toast.makeText(this, "Error al subir la imagen: $result", Toast.LENGTH_LONG).show()
            }
        }
    }

}
