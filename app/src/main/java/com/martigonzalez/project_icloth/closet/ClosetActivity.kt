package com.martigonzalez.project_icloth.closet

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martigonzalez.project_icloth.R
import com.martigonzalez.project_icloth.closet.ColorOption
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

        storageManager = StorageManager()
        firestoreManager = FirestoreManager()
        bottomNav = findViewById(R.id.bottom_navigation_view)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_cloth -> {
                    showImageSourceDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto con la cámara", "Elegir de la galería")
        AlertDialog.Builder(this)
            .setTitle("Añadir nueva prenda")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission.launch(Manifest.permission.CAMERA)
                    1 -> selectImageFromGallery.launch("image/*")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openCamera() {
        val imageUri = createImageUri()
        tempImageUri = imageUri
        takePicture.launch(imageUri)
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photo_temp.png")
        return FileProvider.getUriForFile(
            this,
            "com.martigonzalez.project_icloth.fileprovider",
            image
        )
    }

    private fun uploadImage(imageUri: Uri) {
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()
        storageManager.uploadImage(imageUri) { imageUrl ->
            if (imageUrl != null) {
                showAddClothDialog(imageUrl)
            } else {
                Toast.makeText(this, "Error al subir la imagen.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAddClothDialog(imageUrl: String) {
        // 1. Define tu lista de colores
        val colorOptions = listOf(
            ColorOption("Negro", "#000000"),
            ColorOption("Blanco", "#FFFFFF"),
            ColorOption("Gris", "#808080"),
            ColorOption("Rojo", "#FF0000"),
            ColorOption("Azul", "#0000FF"),
            ColorOption("Verde", "#008000"),
            ColorOption("Amarillo", "#FFFF00"),
            ColorOption("Marrón", "#A52A2A"),
            ColorOption("Beige", "#F5F5DC")
        )

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cloth, null)
        val etClothName = dialogView.findViewById<EditText>(R.id.etClothName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val rvColorPicker = dialogView.findViewById<RecyclerView>(R.id.rvColorPicker)
        val spinnerOccasion = dialogView.findViewById<Spinner>(R.id.spinnerOccasion)

        val colorAdapter = ColorPickerAdapter(this, colorOptions)
        rvColorPicker.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvColorPicker.adapter = colorAdapter

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Añadir Prenda")
            .setPositiveButton("Guardar") { _, _ ->
                val name = etClothName.text.toString().trim()
                val category = spinnerCategory.selectedItem.toString()
                val selectedColor = colorAdapter.getSelectedColor().name
                val occasion = spinnerOccasion.selectedItem.toString()

                if (name.isNotEmpty()) {
                    val clothData = mapOf(
                        "name" to name,
                        "category" to category,
                        "color" to selectedColor,
                        "occasion" to occasion,
                        "imageUrl" to imageUrl
                    )

                    firestoreManager.saveClothItem(clothData) { success ->
                        if (success) {
                            Toast.makeText(this, "Prenda guardada con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al guardar la prenda", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Por favor, añade un nombre a la prenda", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }
}
