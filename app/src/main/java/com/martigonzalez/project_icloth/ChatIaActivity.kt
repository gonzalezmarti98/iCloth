package com.martigonzalez.project_icloth

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

class ChatIaActivity : AppCompatActivity() {

    // Variables para la lógica del chat
    private lateinit var rvChat: RecyclerView
    private lateinit var etPrompt: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_ia)

        // --- 1. CONFIGURACIÓN DEL CHAT ---
        rvChat = findViewById(R.id.rvChatIa)
        etPrompt = findViewById(R.id.etPrompt)
        btnSend = findViewById(R.id.btnSend)

        setupRecyclerView()

        // Acción al pulsar el botón ENVIAR
        btnSend.setOnClickListener {
            val text = etPrompt.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        // --- 2. CONFIGURACIÓN DE LA NAVEGACIÓN (Lo que ya tenías) ---
        // Nota: Asegúrate de que R.id.bottom_navigation es el ID dentro de tu include_bottom_nav.xml
        // Si te da error, prueba con R.id.include_bottom_nav
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
                    //TODO --> Agregar que pida hacer foto o seleccionar de galería
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
        // Añadir mensaje del usuario a la lista visual
        val userMsg = ChatMessage(text, ChatType.USER)
        messageList.add(userMsg)

        // Avisar al adapter que hay un item nuevo
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1) // Bajar scroll al final

        etPrompt.text.clear() // Limpiar campo de texto

        // Paso 2: Simular "pensando..." de la IA (Retraso de 1 segundo)
        Handler(Looper.getMainLooper()).postDelayed({
            showAiTagResponse()
        }, 1000)
    }

    // Paso 3: La IA responde mostrando el selector de etiquetas
    private fun showAiTagResponse() {
        // Estas son las etiquetas que saldrán en los chips.
        // En el futuro, esto podría venir de la respuesta real de Gemini.
        val tags = listOf("Formal", "De Noche", "Invierno", "Cena", "Informal", "Verano", "Elegante")

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
        var finalText = "¡Oído cocina! Buscando prendas para: ${tags.joinToString(", ")}. . .\nGenerando outfits. . ."
        if (tags.isEmpty()) {
            finalText = "¡Oído cocina! Generando outfits. . ."
        }

        // Mensaje de feedback de la IA

        val aiLoadingMsg = ChatMessage(finalText, ChatType.AI_TEXT)

        messageList.add(aiLoadingMsg)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)

        // TODO: AQUÍ ES DONDE LUEGO CAMBIAREMOS DE ACTIVITY O MOSTRAREMOS RESULTADOS
        // Por ahora, solo es visual.
    }
}
