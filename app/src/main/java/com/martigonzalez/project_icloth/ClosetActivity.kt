package com.martigonzalez.project_icloth

// --- IMPORTS CORREGIDOS Y OPTIMIZADOS ---
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
import com.bumptech.glide.Glide // <-- CORRECCIÓN: Usamos el import de Glide estándar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martigonzalez.project_icloth.auth.AuthManager
import com.martigonzalez.project_icloth.closet.ColorOption
import com.martigonzalez.project_icloth.closet.ColorPickerAdapter
import com.martigonzalez.project_icloth.closet.FirestoreManager
import com.martigonzalez.project_icloth.closet.StorageManager
import com.martigonzalez.project_icloth.model.Prenda
import java.io.File

/**
 * Actividad principal del armario (Closet). Muestra la colección de prendas del usuario,
 * y permite añadir nuevas prendas o navegar a otras secciones de la app.
 */
class ClosetActivity : AppCompatActivity() {

    // --- PROPIEDADES DE LA CLASE ---

    private lateinit var rvCloset: RecyclerView
    private lateinit var closetAdapter: ClosetAdapter
    private var listaPrendas = mutableListOf<Prenda>()

    // Gestores para interactuar con los servicios de Firebase.
    private lateinit var storageManager: StorageManager
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var authManager: AuthManager

    // URI temporal para almacenar la imagen tomada con la cámara.
    private var tempImageUri: Uri? = null

    // --- LANZADORES DE ACTIVIDADES (Activity Result Launchers) ---
    // El método moderno en Android para manejar resultados de otras actividades y permisos.

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }
    private val selectImageFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadImage(it) } // Si el usuario selecciona una imagen, la sube.
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempImageUri?.let { uploadImage(it) } // Si la foto se tomó con éxito, sube la imagen.
    }

    /**
     * Punto de entrada principal de la actividad. Se ejecuta al crear la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet)

        // Inicialización de gestores y componentes de la UI.
        storageManager = StorageManager()
        firestoreManager = FirestoreManager()
        authManager = AuthManager()

        setupRecyclerView()
        setupBottomNavigation()
        loadClothesFromFirestore()
    }

    /**
     * Configura el RecyclerView (la cuadrícula) que muestra las prendas.
     */
    private fun setupRecyclerView() {
        rvCloset = findViewById(R.id.rvCloset)
        // Inicializa el adaptador con una lista vacía y define la acción de clic.
        closetAdapter = ClosetAdapter(listaPrendas) { prenda ->
            mostrarDialogoDetalle(prenda) // Al hacer clic en una prenda, muestra sus detalles.
        }
        rvCloset.layoutManager = GridLayoutManager(this, 3) // Organiza los ítems en una cuadrícula de 3 columnas.
        rvCloset.adapter = closetAdapter
    }

    /**
     * Configura la barra de navegación inferior.
     */
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.selectedItemId = R.id.nav_closet // Marca el ícono del armario como seleccionado.

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_cloth -> {
                    showImageSourceDialog()
                    false // Devuelve false para que el ítem no se mantenga seleccionado (es una acción puntual).
                }
                R.id.nav_chat_ia -> {
                    startActivity(Intent(this, ChatIaActivity::class.java))
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
     * Carga la lista de prendas del usuario actual desde Firestore y actualiza el adaptador.
     */
    private fun loadClothesFromFirestore() {
        firestoreManager.getAllClothes { prendas ->
            // --- MEJORA: Usar la función de actualización del adaptador ---
            // En lugar de modificar la lista directamente, pasamos los nuevos datos al adaptador.
            // Esto es más limpio y sigue las mejores prácticas.
            closetAdapter.updatePrendas(prendas)
        }
    }

    /**
     * Muestra un diálogo para que el usuario elija entre la cámara o la galería.
     */
    fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto", "Elegir de galería")
        AlertDialog.Builder(this)
            .setTitle("Añadir prenda")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    1 -> selectImageFromGalleryLauncher.launch("image/*")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Inicia el intent de la cámara para tomar una foto.
     */
    private fun openCamera() {
        tempImageUri = createImageUri()
        takePictureLauncher.launch(tempImageUri!!)
    }

    /**
     * Crea una URI de archivo local segura para guardar temporalmente la foto de la cámara.
     * @return La URI del archivo temporal creado.
     */
    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photo.png")
        // Usa un FileProvider para compartir la URI de forma segura.
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", image)
    }

    /**
     * Sube una imagen a Firebase Storage y, si tiene éxito, muestra el siguiente diálogo.
     * @param uri La URI local de la imagen a subir (de la cámara o galería).
     */
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

    /**
     * Muestra el diálogo final con el formulario para añadir los detalles de la prenda.
     * @param imageUrl La URL pública (https://) de la imagen ya subida a Firebase.
     */
    private fun showAddClothDetailsDialog(imageUrl: String) {
        // Infla la vista personalizada para el diálogo.
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cloth, null)

        // Obtener referencias a todas las vistas dentro del diálogo.
        val etClothName = dialogView.findViewById<EditText>(R.id.etClothName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val rvColorPicker = dialogView.findViewById<RecyclerView>(R.id.rvColorPicker)
        val spinnerOccasion = dialogView.findViewById<Spinner>(R.id.spinnerOccasion)
        val spinnerTemporada = dialogView.findViewById<Spinner>(R.id.spinnerTemporada)
        val spinnerNivelFormalidad = dialogView.findViewById<Spinner>(R.id.spinnerNivelFormalidad)
        val spinnerAjuste = dialogView.findViewById<Spinner>(R.id.spinnerAjuste)
        val spinnerPatron = dialogView.findViewById<Spinner>(R.id.spinnerPatron)


        // Configuramos el RecyclerView de colores.

        val colorOptions = listOf(
            ColorOption("Negro", "#212121"), ColorOption("Blanco", "#FFFFFF"),
            ColorOption("Gris Oscuro", "#5f6368"), ColorOption("Gris Claro", "#bdc1c6"),
            ColorOption("Beige", "#d2b48c"), ColorOption("Marfil", "#fffff0"),
            ColorOption("Crema", "#f5f5dc"), ColorOption("Marrón", "#795548"),
            ColorOption("Caqui", "#c3b091"), ColorOption("Terracota", "#e2725b"),
            ColorOption("Oliva", "#808000"), ColorOption("Azul Marino", "#000080"),
            ColorOption("Azul Rey", "#4169e1"), ColorOption("Azul Cielo", "#87ceeb"),
            ColorOption("Vaquero (Denim)", "#1560bd"), ColorOption("Rojo", "#d32f2f"),
            ColorOption("Burdeos", "#800020"), ColorOption("Rosa Palo", "#f4c2c2"),
            ColorOption("Fucsia", "#ff00ff"), ColorOption("Verde Bosque", "#228b22"),
            ColorOption("Verde Menta", "#98ff98"), ColorOption("Verde Militar", "#556b2f"),
            ColorOption("Amarillo", "#fdd835"), ColorOption("Mostaza", "#ffdb58"),
            ColorOption("Naranja", "#fb8c00"), ColorOption("Morado", "#8e44ad"),
            ColorOption("Lila", "#c8a2c8"), ColorOption("Lavanda", "#e6e6fa"),
            ColorOption("Dorado", "#ffd700"), ColorOption("Plata", "#c0c0c0")
        )

        val colorAdapter = ColorPickerAdapter(this,colorOptions)
        rvColorPicker.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvColorPicker.adapter = colorAdapter

        // Crea y muestra el diálogo de alerta.
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Añadir Prenda")
            .setPositiveButton("Guardar") { _, _ ->
                // Ahora sí, obtenemos los valores de las vistas justo al pulsar "Guardar".
                val nombre = etClothName.text.toString().trim()
                val categoria = spinnerCategory.selectedItem.toString()
                val selectedColor = colorAdapter.getSelectedColor()?.name
                val ocasion = spinnerOccasion.selectedItem.toString()
                val temporada = spinnerTemporada.selectedItem.toString()
                val nivelFormalidad = spinnerNivelFormalidad.selectedItem.toString()
                val ajuste = spinnerAjuste.selectedItem.toString()
                val patron = spinnerPatron.selectedItem.toString()


                // Validamos que los campos obligatorios no estén vacíos.
                if (nombre.isNotEmpty() && selectedColor != null) {
                    // Creamos el objeto Prenda con todos los datos recogidos.
                    val nuevaPrenda = Prenda(
                        nombre = nombre,
                        categoria = categoria,
                        colorPpal = selectedColor,
                        ocasion = ocasion,
                        imagenUrl = imageUrl, // La URL que recibimos como parámetro.
                        temporada = temporada,
                        nivelFormalidad = nivelFormalidad,
                        ajuste = ajuste,
                        patron = patron
                    )

                    // Guardamos la nueva prenda en Firestore.
                    firestoreManager.saveClothItem(nuevaPrenda) { success ->
                        if (success) {
                            Toast.makeText(this, "Prenda guardada", Toast.LENGTH_SHORT).show()
                            loadClothesFromFirestore() // Recarga la lista para mostrar la nueva prenda.
                        } else {
                            Toast.makeText(this, "Error al guardar la prenda", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Si falta el nombre o el color, avisamos al usuario.
                    Toast.makeText(this, "El nombre y el color son obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
        // --- FIN DE LA CORRECCIÓN ---
    }

    /**
     * Muestra un diálogo con los detalles de una prenda existente.
     * @param prenda El objeto Prenda en el que se ha hecho clic.
     */
    private fun mostrarDialogoDetalle(prenda: Prenda) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_prenda)

        // Esto nos quita el fondo blanco que salía por defecto.
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val ivImagen = dialog.findViewById<ImageView>(R.id.ivDetalleImagen)
        val tvNombre = dialog.findViewById<TextView>(R.id.tvNombrePrenda)
        val tvCategoria = dialog.findViewById<TextView>(R.id.tvDetalleCategoria)
        val tvColor = dialog.findViewById<TextView>(R.id.tvColorPpal)
        val tvOcasion = dialog.findViewById<TextView>(R.id.tvDetalleOcasion)
        val tvTemporada = dialog.findViewById<TextView>(R.id.tvTemporada)
        val tvNivelFormalidad = dialog.findViewById<TextView>(R.id.tvNivelFormalidad)
        val tvAjuste = dialog.findViewById<TextView>(R.id.tvAjuste)
        val tvPatron = dialog.findViewById<TextView>(R.id.tvPatron)

        /**
        DEBUG
         **/
        val tvUrlDebug = dialog.findViewById<TextView>(R.id.tvUrl)
        // Mostramos la URL que estamos a punto de usar en el TextView de depuración
        tvUrlDebug?.text = "Debug URL: ${prenda.imagenUrl}"
        /**
        DEBUG
         **/

        tvNombre?.text = prenda.nombre
        tvCategoria?.text = "Categoría: ${prenda.categoria}"
        tvColor?.text = "Color: ${prenda.colorPpal}"
        tvOcasion?.text = "Ocasión: ${prenda.ocasion}"
        tvTemporada?.text = "Temporada: ${prenda.temporada}"
        tvNivelFormalidad?.text = "Nivel de Formalidad: ${prenda.nivelFormalidad}"
        tvAjuste?.text = "Ajuste: ${prenda.ajuste}"
        tvPatron?.text = "Patrón: ${prenda.patron}"


        if (ivImagen != null) {
            // Se comprueba que la URL no esté vacía y que sea una URL web.
            if (prenda.imagenUrl.isNotEmpty() && prenda.imagenUrl.startsWith("https")) {
                // Usamos Glide estándar, que es más simple y robusto.
                Glide.with(this)
                    .load(prenda.imagenUrl) // Carga la URL https:// directamente.
                    .centerCrop()
                    .placeholder(R.color.grey_placeholder) // Imagen provisional mientras carga.
                    .error(R.drawable.ic_error_placeholder) // Imagen si ocurre un error.
                    .into(ivImagen)
            } else {
                ivImagen.setImageResource(R.drawable.ic_error_placeholder)
                Log.e("DialogoDetalle", "URL de imagen inválida o vacía: ${prenda.imagenUrl}")
            }
        }

        //Botón borrar prenda
        val btnDelete = dialog.findViewById<android.widget.Button>(R.id.btnDeleteCloth)

        btnDelete?.setOnClickListener {
            // Confirmación de seguridad
            AlertDialog.Builder(this)
                .setTitle("¿Borrar prenda?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Borrar") { _, _ ->

                    // Llamada a Firestore
                    firestoreManager.deleteCloth(
                        prenda.id,
                        onSuccess = {
                            Toast.makeText(this, "Prenda eliminada", Toast.LENGTH_SHORT).show()

                            // 3. Usamos 'dialog.dismiss()' (no alertDialog)
                            dialog.dismiss()

                            // Recargar la lista
                            loadClothesFromFirestore()
                        },
                        onFailure = {
                            Toast.makeText(this, "Error al borrar", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        dialog.show()
    }

}
