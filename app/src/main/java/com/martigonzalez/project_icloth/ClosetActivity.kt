package com.martigonzalez.project_icloth

// --- IMPORTS COMPLETOS Y CORREGIDOS ---
import com.martigonzalez.project_icloth.GlideApp
import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.martigonzalez.project_icloth.auth.AuthManager
import com.martigonzalez.project_icloth.closet.ColorOption
import com.martigonzalez.project_icloth.closet.ColorPickerAdapter
import com.martigonzalez.project_icloth.closet.FirestoreManager
import com.martigonzalez.project_icloth.closet.StorageManager
import com.martigonzalez.project_icloth.model.Prenda
import java.io.File


class ClosetActivity : AppCompatActivity() {

    // --- PROPIEDADES DE LA CLASE ---
    // Vista que muestra la cuadrícula de prendas.
    private lateinit var rvCloset: RecyclerView
    // Adaptador que conecta los datos (listaPrendas) con la vista (rvCloset).
    private lateinit var closetAdapter: ClosetAdapter
    // Lista mutable que contiene los objetos de tipo Prenda que se mostrarán.
    private var listaPrendas = mutableListOf<Prenda>()

    // Gestores para interactuar con los servicios de Firebase.
    private lateinit var storageManager: StorageManager // Gestiona la subida de imágenes a Firebase Storage.
    private lateinit var firestoreManager: FirestoreManager // Gestiona las operaciones con la base de datos Firestore.
    private lateinit var authManager: AuthManager // Gestiona la autenticación de usuarios.

    // URI temporal para almacenar la imagen tomada con la cámara antes de subirla.
    private var tempImageUri: Uri? = null

    // --- LANZADORES DE ACTIVIDADES (Activity Result Launchers) ---
    // Se usan para manejar los resultados de otras actividades (cámara, galería, permisos).

    // Lanzador para solicitar el permiso de la cámara.
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }
    // Lanzador para seleccionar una imagen de la galería.
    private val selectImageFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadImage(it) } // Si el usuario selecciona una imagen (uri no es nulo), la sube.
    }
    // Lanzador para tomar una foto con la cámara.
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempImageUri?.let { uploadImage(it) } // Si la foto se tomó con éxito, sube la imagen guardada en tempImageUri.
    }

    /**
     * Función principal que se ejecuta al crear la actividad.
     * Es el punto de entrada para la configuración de la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet)

        // Inicialización de los gestores y componentes de la UI.
        storageManager = StorageManager()
        firestoreManager = FirestoreManager()
        authManager = AuthManager()

        setupRecyclerView()
        setupBottomNavigation()
        loadClothesFromFirestore()
    }

    /**
     * Configura el RecyclerView, que es la cuadrícula donde se muestran las prendas.
     */
    private fun setupRecyclerView() {
        rvCloset = findViewById(R.id.rvCloset)
        // Inicializa el adaptador con la lista de prendas y una acción a ejecutar al hacer clic en una.
        closetAdapter = ClosetAdapter(listaPrendas) { prenda ->
            mostrarDialogoDetalle(prenda) // Al hacer clic, muestra el diálogo con los detalles.
        }
        rvCloset.layoutManager = GridLayoutManager(this, 3) // Organiza los ítems en una cuadrícula de 3 columnas.
        rvCloset.adapter = closetAdapter
    }

    /**
     * Configura la barra de navegación inferior (BottomNavigationView).
     */
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.selectedItemId = R.id.nav_closet // Marca el ícono del armario como seleccionado.

        // Define las acciones para cada ítem de la barra de navegación.
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_cloth -> {
                    showImageSourceDialog() // Muestra el diálogo para elegir entre cámara o galería.
                    false // Devuelve false para que el ítem no se mantenga seleccionado.
                }
                R.id.nav_chat_ia -> {
                    startActivity(Intent(this, ChatIaActivity::class.java))
                    overridePendingTransition(0, 0) // Sin animación de transición.
                    finish() // Cierra la actividad actual para no apilarla.
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    true
                }
                R.id.nav_closet -> true // Ya estamos aquí, no hace nada.
                else -> false
            }
        }
    }

    /**
     * Carga las prendas del usuario actual desde Firestore y actualiza la UI.
     */
    private fun loadClothesFromFirestore() {
        firestoreManager.getAllClothes { prendas ->
            listaPrendas.clear() // Limpia la lista actual para evitar duplicados.
            listaPrendas.addAll(prendas) // Añade todas las prendas recuperadas.
            closetAdapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado para que redibuje la lista.
        }
    }

    /**
     * Muestra un diálogo para que el usuario elija entre tomar una foto o seleccionarla de la galería.
     */
    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto", "Elegir de galería")
        AlertDialog.Builder(this)
            .setTitle("Añadir prenda")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) // Inicia el flujo de la cámara.
                    1 -> selectImageFromGalleryLauncher.launch("image/*") // Inicia el flujo de la galería.
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Inicia la cámara para tomar una foto.
     * Primero crea una URI temporal donde se guardará la imagen.
     */
    private fun openCamera() {
        tempImageUri = createImageUri()
        takePictureLauncher.launch(tempImageUri!!)
    }

    /**
     * Crea una URI de archivo local para guardar la foto de la cámara.
     * Utiliza un FileProvider para garantizar la seguridad y compatibilidad.
     */
    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", image)
    }

    /**
     * Sube una imagen (dada su URI local) a Firebase Storage.
     * Al completarse, muestra el diálogo para añadir los detalles de la prenda.
     */
    private fun uploadImage(uri: Uri) {
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()
        storageManager.uploadImage(uri) { imageUrl ->
            if (imageUrl != null) {
                // Si la subida es exitosa, muestra el siguiente diálogo con la URL de la imagen.
                showAddClothDetailsDialog(imageUrl)
            } else {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Muestra un diálogo con un formulario para añadir los detalles de la nueva prenda.
     * @param imageUrl La URL de la imagen en Firebase Storage (formato gs://).
     */
    private fun showAddClothDetailsDialog(imageUrl: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cloth, null)
        val etClothName = dialogView.findViewById<EditText>(R.id.etClothName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerOccasion = dialogView.findViewById<Spinner>(R.id.spinnerOccasion)
        val rvColorPicker = dialogView.findViewById<RecyclerView>(R.id.rvColorPicker)

        // Define la lista de colores disponibles para seleccionar.
        val colorList = listOf(
            ColorOption("Blanco", "#FFFFFF"), ColorOption("Gris Claro", "#F2F2F2"),
            ColorOption("Gris", "#808080"), ColorOption("Gris Oscuro", "#4A4A4A"),
            ColorOption("Negro", "#000000"), ColorOption("Rojo", "#FF0000"),
            ColorOption("Rosa", "#FFC0CB"), ColorOption("Fucsia", "#FF00FF"),
            ColorOption("Vino", "#800000"), ColorOption("Naranja", "#FFA500"),
            ColorOption("Amarillo", "#FFFF00"), ColorOption("Mostaza", "#FFDB58"),
            ColorOption("Verde", "#00FF00"), ColorOption("Verde Menta", "#98FF98"),
            ColorOption("Verde Oliva", "#808000"), ColorOption("Verde Bosque", "#228B22"),
            ColorOption("Azul", "#0000FF"), ColorOption("Azul Cielo", "#87CEEB"),
            ColorOption("Azul Marino", "#000080"), ColorOption("Turquesa", "#40E0D0"),
            ColorOption("Lila", "#C8A2C8"), ColorOption("Morado", "#800080"),
            ColorOption("Lavanda", "#E6E6FA"), ColorOption("Beige", "#F5F5DC"),
            ColorOption("Marrón", "#964B00"), ColorOption("Caqui", "#F0E68C"),
            ColorOption("Terracota", "#E2725B")
        )
        // Configura el adaptador de colores.
        val colorAdapter = ColorPickerAdapter(this, colorList)
        rvColorPicker.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvColorPicker.adapter = colorAdapter

        // Crea y muestra el diálogo de alerta.
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etClothName.text.toString().trim()
                val categoria = spinnerCategory.selectedItem.toString()
                val ocasion = spinnerOccasion.selectedItem.toString()
                val colorSeleccionado = colorAdapter.getSelectedColor()

                if (nombre.isNotEmpty()) {
                    // Crea un nuevo objeto Prenda con los datos del formulario.
                    val nuevaPrenda = Prenda(
                        nombre = nombre,
                        imagenUrl = imageUrl,
                        categoria = categoria,
                        ocasion = ocasion,
                        colorPpal = colorSeleccionado.hexCode
                    )
                    // Guarda la nueva prenda en Firestore.
                    firestoreManager.saveClothItem(nuevaPrenda) { success ->
                        if (success) {
                            Toast.makeText(this, "Prenda guardada", Toast.LENGTH_SHORT).show()
                            loadClothesFromFirestore() // Recarga la lista para mostrar la nueva prenda.
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
            .show()
    }

    /**
     * Muestra un diálogo con los detalles de una prenda existente.
     * @param prenda El objeto Prenda en el que se ha hecho clic.
     */
    private fun mostrarDialogoDetalle(prenda: Prenda) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_prenda)

        val ivImagen = dialog.findViewById<ImageView>(R.id.ivDetalleImagen)
        val tvNombre = dialog.findViewById<TextView>(R.id.tvNombrePrenda)
        val tvCategoria = dialog.findViewById<TextView>(R.id.tvDetalleCategoria)
        val tvColor = dialog.findViewById<TextView>(R.id.tvColorPpal)
        val tvOcasion = dialog.findViewById<TextView>(R.id.tvDetalleOcasion)

        tvNombre?.text = prenda.nombre
        tvCategoria?.text = "Categoría: ${prenda.categoria}"
        tvColor?.text = "Color: ${prenda.colorPpal}"
        tvOcasion?.text = "Ocasión: ${prenda.ocasion}"

        // Carga la imagen de la prenda desde Firebase Storage de forma segura
        if (ivImagen != null) {
            if (prenda.imagenUrl.isNotEmpty()) {
                try {
                    // --- ¡MISMA CORRECCIÓN QUE EN EL ADAPTADOR! ---
                    // 1. Convierte la URL 'gs://...' en una referencia de Storage
                    val storageReference = Firebase.storage.getReferenceFromUrl(prenda.imagenUrl)

                    // 2. Pasa la referencia directamente a GlideApp.
                    GlideApp.with(this)
                        .load(storageReference)
                        .centerCrop()
                        .placeholder(R.color.grey_placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .into(ivImagen)
                } catch (e: Exception) {
                    ivImagen.setImageResource(R.drawable.ic_error_placeholder)
                    Log.e("DialogoDetalle", "Error al cargar la imagen con Glide", e)
                }
            } else {
                ivImagen.setImageResource(R.drawable.ic_error_placeholder)
            }
        }
        dialog.show()
    }

    /**
     * Función de utilidad para navegar de vuelta a la actividad principal (MainActivity).
     * Limpia la pila de actividades para que el usuario no pueda volver atrás.
     */
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
