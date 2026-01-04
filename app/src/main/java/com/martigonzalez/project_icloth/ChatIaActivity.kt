package com.martigonzalez.project_icloth

import com.martigonzalez.project_icloth.closet.FirestoreManager
import com.martigonzalez.project_icloth.model.Prenda
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martigonzalez.project_icloth.adapter.ChatAdapter
import com.martigonzalez.project_icloth.model.ChatMessage
import com.martigonzalez.project_icloth.model.ChatType
import kotlin.text.lowercase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.martigonzalez.project_icloth.data.GeminiManager


class ChatIaActivity : AppCompatActivity() {

    // Variables para la lógica del chat
    private lateinit var rvChat: RecyclerView
    private lateinit var etPrompt: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private val firestoreManager = FirestoreManager() // Para conectar con la BDD
    private val geminiManager = GeminiManager() // Para conectar con Gemini

    private var allUserClothes: List<Prenda> = emptyList() // Aquí guardaremos la ropa cargada
    private var currentPrompt: String = "" // Memoria del prompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_ia)

        // --- 1. CONFIGURACIÓN DEL CHAT ---
        rvChat = findViewById(R.id.rvChatIa)
        etPrompt = findViewById(R.id.etPrompt)
        btnSend = findViewById(R.id.btnSend)

        setupRecyclerView()

        loadClothes()

        // Acción al pulsar el botón ENVIAR
        btnSend.setOnClickListener {
            val text = etPrompt.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        // --- 2. CONFIGURACIÓN DE LA NAVEGACIÓN  ---
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view) ?: findViewById(R.id.include_bottom_nav)

        bottomNav.selectedItemId = R.id.nav_chat_ia

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_closet -> { // ARMARIO
                    startActivity(Intent(this, ClosetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_chat_ia -> { // ESTAMOS AQUÍ
                    true
                }
                R.id.nav_add_cloth -> { // (+) AÑADIR
                    true
                }
                R.id.nav_news -> { // NEWS
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> { // PERFIL USUARIO
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // --- MÉTODOS PRIVADOS DEL CHAT ---
    private fun loadClothes() {
        firestoreManager.getAllClothes { prendas ->
            allUserClothes = prendas
            // Opcional: Log para comprobar
            // android.util.Log.d("ChatIA", "Ropa cargada: ${allUserClothes.size} prendas")
        }
    }

    private fun setupRecyclerView() {
        // Inicializamos el adapter pasándole la lista y la función lambda para cuando se confirmen etiquetas
        chatAdapter = ChatAdapter(messageList) { selectedTags ->
            handleTagsSelection(selectedTags)
        }
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter
    }

    // Paso 1: El usuario envía un mensaje
    private fun sendMessage(text: String) {
        currentPrompt = text // <------------- guardamos la pregunta aquí

        // Añadir mensaje del usuario a la lista visual
        val userMsg = ChatMessage(text, ChatType.USER)
        messageList.add(userMsg)

        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)

        etPrompt.text.clear()

        // Simular pensamiento
        Handler(Looper.getMainLooper()).postDelayed({
            showAiTagResponse()
        }, 1000)
    }

    // Paso 3: La IA responde mostrando el selector de etiquetas
    private fun showAiTagResponse() {
        // Estas son las etiquetas que saldrán en los chips.
        // En el futuro, esto podría venir de la respuesta real de Gemini.
        val tags = listOf("Casual", "Trabajo/Oficina", "Deporte", "Fiesta/Noche", "Playa/Verano",
            "Entretiempo", "Invierno/Frío", "Verano/Calor",
            "Muy Formal", "Medio Formal", "Nada Formal",
            "Regular", "Ceñido", "Oversize")

        val aiMsg = ChatMessage(
            text = "dummy", // El texto va dentro del layout de tags, aquí no se usa
            type = ChatType.AI_TAGS,
            tags = tags
        )
        messageList.add(aiMsg)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)
    }

    // Paso 4: El usuario ha seleccionado etiquetas y pulsado "Confirmar"
    private fun handleTagsSelection(tags: List<String>) {
        // 1. Mensaje de feedback visual ("Buscando...")
        var finalText = "¡Oído cocina! Buscando prendas para: ${tags.joinToString(", ")}. . .\nGenerando outfits. . ."
        if (tags.isEmpty()) {
            finalText = "¡Oído cocina! Generando outfits con todo el armario. . ."
        }

        val aiLoadingMsg = ChatMessage(finalText, ChatType.AI_TEXT)
        messageList.add(aiLoadingMsg)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)


        // 2. LÓGICA DE FILTRADO
        // ---------------------------------------------------
        val filteredClothes = if (tags.isEmpty()) {
            allUserClothes // Si no hay etiquetas, usamos todo
        } else {
            allUserClothes.filter { prenda ->
                // La prenda se queda si cumple AL MENOS UNA de las etiquetas seleccionadas
                tags.any { tagSeleccionado ->
                    cumpleCriterio(prenda, tagSeleccionado)
                }
            }
        }
        // ---------------------------------------------------

        // Comprobación de seguridad por si el filtro deja 0 prendas
        val finalClothesList = if (filteredClothes.isEmpty()) {
            // Opcional: Podrías añadir un mensaje tipo "No encontré nada exacto..."
            allUserClothes
        } else {
            filteredClothes
        }

        // 3. LLAMADA A GEMINI
        // Usamos una corrutina para no bloquear la pantalla mientras la IA piensa
        lifecycleScope.launch {
            try {
                // Llamamos a tu Manager pasándole el prompt original y la lista ya filtrada
                val respuestaGemini = geminiManager.getOutfitProposal(
                    userPrompt = currentPrompt,
                    filteredClothes = finalClothesList
                )

                // 4. MOSTRAR RESULTADOS EN EL CHAT (IMPLEMENTADO)
                // Creamos el mensaje con la respuesta de la IA
                val aiResponseMsg = ChatMessage(respuestaGemini, ChatType.AI_TEXT)

                // Lo añadimos a la lista y actualizamos
                messageList.add(aiResponseMsg)
                chatAdapter.notifyItemInserted(messageList.size - 1)
                rvChat.scrollToPosition(messageList.size - 1)

            } catch (e: Exception) {
                // Si algo falla (internet, api key...), avisamos al usuario
                val errorMsg = ChatMessage("Ups, tuve un problema conectando con el servidor de IA: ${e.message}", ChatType.AI_TEXT)
                messageList.add(errorMsg)
                chatAdapter.notifyItemInserted(messageList.size - 1)
                rvChat.scrollToPosition(messageList.size - 1)
            }
        }
    }


    /**
     * TRADUCTOR: Convierte las etiquetas "bonitas" del chat a reglas de la BDD
     */
    private fun cumpleCriterio(prenda: com.martigonzalez.project_icloth.model.Prenda, etiqueta: String): Boolean {
        // Convertimos a minúsculas para evitar errores
        val formalidad = prenda.nivelFormalidad.lowercase()
        val temporada = prenda.temporada.lowercase()
        val ocasion = prenda.ocasion.lowercase()
        val ajuste = prenda.ajuste.lowercase()

        return when (etiqueta) {
            // --- FORMALIDAD ---
            "Muy Formal" -> formalidad == "alto"
            "Medio Formal" -> formalidad == "medio"
            "Nada Formal" -> formalidad == "bajo"

            // --- TEMPORADA ---
            "Invierno/Frío" -> temporada.contains("invierno") || temporada.contains("otoño")
            "Verano/Calor" -> temporada.contains("verano")
            "Entretiempo" -> temporada.contains("primavera") || temporada.contains("otoño")
            "Playa/Verano" -> temporada.contains("verano") || ocasion.contains("playa")

            // --- OCASIONES / ESTILOS ---
            "Trabajo/Oficina" -> ocasion.contains("trabajo") || ocasion.contains("oficina") || formalidad == "medio" || formalidad == "alto"
            "Fiesta/Noche" -> ocasion.contains("fiesta") || ocasion.contains("noche") || ocasion.contains("cena")
            "Deporte" -> prenda.categoria.lowercase() == "deporte" || ocasion.contains("deporte")
            "Casual" -> formalidad == "bajo" || formalidad == "medio" || ocasion.contains("casual")

            // --- POR DEFECTO ---
            // Si la etiqueta no es especial, buscamos la palabra tal cual en cualquier campo
            else -> {
                prenda.categoria.contains(etiqueta, ignoreCase = true) ||
                        prenda.ocasion.contains(etiqueta, ignoreCase = true) ||
                        prenda.colorPpal.contains(etiqueta, ignoreCase = true)
            }
        }
    }

}
