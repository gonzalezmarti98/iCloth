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
import java.io.File
import android.content.Context
import android.view.inputmethod.InputMethodManager

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

    // Dialog para subir prenda -> FIRESTORE + STORAGE
    private fun showAddClothDialog(imageUrl: String) {
        val colorOptions = listOf(
            // ... tu lista de colores completa va aquí ...
            ColorOption("Negro", "#212121"),
            ColorOption("Blanco", "#FFFFFF"),
            ColorOption("Gris Oscuro", "#5f6368"),
            ColorOption("Gris Claro", "#bdc1c6"),
            ColorOption("Beige", "#d2b48c"),
            ColorOption("Marfil", "#fffff0"),
            ColorOption("Crema", "#f5f5dc"),
            ColorOption("Marrón", "#795548"),
            ColorOption("Caqui", "#c3b091"),
            ColorOption("Terracota", "#e2725b"),
            ColorOption("Oliva", "#808000"),
            ColorOption("Azul Marino", "#000080"),
            ColorOption("Azul Rey", "#4169e1"),
            ColorOption("Azul Cielo", "#87ceeb"),
            ColorOption("Vaquero (Denim)", "#1560bd"),
            ColorOption("Rojo", "#d32f2f"),
            ColorOption("Burdeos", "#800020"),
            ColorOption("Rosa Palo", "#f4c2c2"),
            ColorOption("Fucsia", "#ff00ff"),
            ColorOption("Verde Bosque", "#228b22"),
            ColorOption("Verde Menta", "#98ff98"),
            ColorOption("Verde Militar", "#556b2f"),
            ColorOption("Amarillo", "#fdd835"),
            ColorOption("Mostaza", "#ffdb58"),
            ColorOption("Naranja", "#fb8c00"),
            ColorOption("Morado", "#8e44ad"),
            ColorOption("Lila", "#c8a2c8"),
            ColorOption("Lavanda", "#e6e6fa"),
            ColorOption("Dorado", "#ffd700"),
            ColorOption("Plata", "#c0c0c0")
        )

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cloth, null)
        val etClothName = dialogView.findViewById<EditText>(R.id.etClothName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val rvColorPicker = dialogView.findViewById<RecyclerView>(R.id.rvColorPicker)
        val spinnerOccasion = dialogView.findViewById<Spinner>(R.id.spinnerOccasion)

        val colorAdapter = ColorPickerAdapter(this, colorOptions)
        rvColorPicker.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvColorPicker.adapter = colorAdapter

        val dialog = AlertDialog.Builder(this) // <<< 2. Creamos el builder pero no lo mostramos aún
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
                        "imageUrl" to imageUrl,
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

        dialog.setOnShowListener {
            etClothName.requestFocus() // Pone el cursor en el campo del nombre
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etClothName, InputMethodManager.SHOW_IMPLICIT) // Muestra el teclado de forma suave
        }
        dialog.setOnDismissListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etClothName.windowToken, 0) // Oculta el teclado al cerrar
        }
        dialog.show() // Finalmente, mostramos el diálogo
    }
}
