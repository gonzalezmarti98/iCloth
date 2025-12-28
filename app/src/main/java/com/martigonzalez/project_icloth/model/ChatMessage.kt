package com.martigonzalez.project_icloth.model

data class ChatMessage(
    val text: String,
    val type: ChatType,
    val tags: List<String> = emptyList() // Solo se usa si es tipo AI_TAGS
)

enum class ChatType {
    USER,       // Mensaje tuyo (derecha)
    AI_TEXT,    // Mensaje IA normal (izquierda)
    AI_TAGS     // Mensaje IA con etiquetas (recuadro grande)
}
