package com.martigonzalez.project_icloth

// --- IMPORTS COMPLETOS Y CORREGIDOS ---
import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martigonzalez.project_icloth.auth.AuthManager
import com.martigonzalez.project_icloth.closet.ColorOption
import com.martigonzalez.project_icloth.closet.ColorPickerAdapter
import com.martigonzalez.project_icloth.closet.FirestoreManager
import com.martigonzalez.project_icloth.closet.StorageManager
import com.martigonzalez.project_icloth.model.Prenda
import java.io.File

// ▼▼▼ ¡ESTA LÍNEA ES LA QUE FALTABA! ▼▼▼
class ClosetActivity : AppCompatActivity() {

    // --- PROPIEDADES ---
    private lateinit var rvCloset: RecyclerView
    private lateinit var closetAdapter: ClosetAdapter
    private var listaPrendas = mutableListOf<Prenda>()

    private lateinit var storageManager: StorageManager
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var authManager: AuthManager
    private var tempImageUri: Uri? = null

    // --- LANZADORES ---
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }
    private val selectImageFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadImage(it) }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempImageUri?.let { uploadImage(it) }
    }

    // ▼▼▼ ¡ESTA FUNCIÓN Y LAS DEMÁS TAMBIÉN FALTABAN! ▼▼▼
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet)

        // Inicialización
        storageManager = StorageManager()
        firestoreManager = FirestoreManager()
        authManager = AuthManager()

        setupRecyclerView()
        setupBottomNavigation()
        loadClothesFromFirestore()
    }

    private fun setupRecyclerView() {
        rvCloset = findViewById(R.id.rvCloset)
        closetAdapter = ClosetAdapter(listaPrendas) { prenda ->
            mostrarDialogoDetalle(prenda)
        }
        rvCloset.layoutManager = GridLayoutManager(this, 3)
        rvCloset.adapter = closetAdapter
    }

    //BOTONES DE NAVEGACIÓN
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        // Marca el ítem 'Closet' como seleccionado en ESTA pantalla
        bottomNav.selectedItemId = R.id.nav_closet

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_cloth -> {
                    showImageSourceDialog()
                    // Es importante devolver false para que el ítem no se quede seleccionado
                    false
                }
                R.id.nav_chat_ia -> { // IR AL CHAT IA
                    startActivity(Intent(this, ChatIaActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                //NEWS
                R.id.nav_news -> {
                    val intent = Intent(this, NewsActivity::class.java)
                    startActivity(intent)
                    // overridePendingTransition(0, 0)
                    true
                }
                //PERFIL
                R.id.nav_profile -> {
                    val intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                // Si clicamos en Closet, ya estamos aquí
                R.id.nav_closet -> true
                else -> false
            }
        }
    }

    private fun loadClothesFromFirestore() {
        firestoreManager.getAllClothes { prendas ->
            listaPrendas.clear()
            listaPrendas.addAll(prendas)
            closetAdapter.notifyDataSetChanged()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto", "Elegir de galería")
        AlertDialog.Builder(this).setTitle("Añadir prenda").setItems(options) { _, which ->
            when (which) {
                0 -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                1 -> selectImageFromGalleryLauncher.launch("image/*")
            }
        }.setNegativeButton("Cancelar", null).show()
    }

    private fun openCamera() {
        tempImageUri = createImageUri()
        takePictureLauncher.launch(tempImageUri!!)
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", image)
    }

    private fun uploadImage(uri: Uri) {
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()
        storageManager.uploadImage(uri) { imageUrl ->
            if (imageUrl != null) {
                showAddClothDetailsDialog(imageUrl)
            } else {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ▼▼▼ ESTA ES LA FUNCIÓN QUE CORREGIMOS ▼▼▼
    private fun showAddClothDetailsDialog(imageUrl: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cloth, null)
        val etClothName = dialogView.findViewById<EditText>(R.id.etClothName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerOccasion = dialogView.findViewById<Spinner>(R.id.spinnerOccasion)
        val rvColorPicker = dialogView.findViewById<RecyclerView>(R.id.rvColorPicker)

        // --- Configuración del RecyclerView de Colores USANDO TU ADAPTADOR ---
        val colorList = listOf(
            ColorOption("Blanco", "#FFFFFF"),
            ColorOption("Negro", "#000000"),
            ColorOption("Rojo", "#FF0000"),
            ColorOption("Verde", "#00FF00"),
            ColorOption("Azul", "#0000FF"),
            ColorOption("Amarillo", "#FFFF00"),
            ColorOption("Rosa", "#FFC0CB"),
            ColorOption("Gris", "#808080")
        )
        val colorAdapter = ColorPickerAdapter(this, colorList)
        rvColorPicker.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvColorPicker.adapter = colorAdapter

        // --- Creación y Lógica del Diálogo ---
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etClothName.text.toString().trim()
                val categoria = spinnerCategory.selectedItem.toString()
                val ocasion = spinnerOccasion.selectedItem.toString()
                val colorSeleccionado = colorAdapter.getSelectedColor()

                if (nombre.isNotEmpty()) {
                    val nuevaPrenda = Prenda(
                        nombre = nombre,
                        imagenUrl = imageUrl,
                        categoria = categoria,
                        ocasion = ocasion,
                        colorPpal = colorSeleccionado.hexCode
                    )
                    firestoreManager.saveClothItem(nuevaPrenda) { success ->
                        if (success) {
                            Toast.makeText(this, "Prenda guardada", Toast.LENGTH_SHORT).show()
                            loadClothesFromFirestore()
                        } else {
                            Toast.makeText(this, "Error al guardar la prenda", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
    }

    private fun mostrarDialogoDetalle(prenda: Prenda) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_prenda)
        val ivImagen = dialog.findViewById<ImageView>(R.id.ivPrenda)
        val tvNombre = dialog.findViewById<TextView>(R.id.ivDetalleImagen)

        tvNombre.text = prenda.nombre
        Glide.with(this).load(prenda.imagenUrl).centerCrop().into(ivImagen)
        dialog.show()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

}
