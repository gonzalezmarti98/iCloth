package com.martigonzalez.project_icloth.data

//import androidx.compose.ui.semantics.text
import com.google.ai.client.generativeai.GenerativeModel
import com.martigonzalez.project_icloth.model.Prenda

class GeminiManager {

    private val apiKey = "AIzaSyAZL4VQOjIAKEp3Dd2KLVqejhl2SLyf5yA" //TODO: por ahora usar la de cada uno

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun getOutfitProposal(
        userPrompt: String,
        filteredClothes: List<Prenda>
    ): String {

        // 1. Convertimos la lista de objetos Prenda a un Texto que Gemini entienda.
        // Omitimos imagenUrl para ahorrar tokens y ruido.
        val wardrobeText = filteredClothes.joinToString(separator = "\n") { prenda ->
            android.util.Log.d("DEBUG_GEMINI", "Prenda: ${prenda.nombre} - ID: ${prenda.id}")
            """
            - ID: ${prenda.id}
              Nombre: ${prenda.nombre}
              Categoría: ${prenda.categoria}
              Color: ${prenda.colorPpal}
              Ocasión: ${prenda.ocasion}
              Temporada: ${prenda.temporada}
              Nivel de Formalidad: ${prenda.nivelFormalidad}
              Ajuste: ${prenda.ajuste}
              Patrón: ${prenda.patron}
            """.trimIndent()
        }

        // 2. Construimos el Prompt Maestro
        val prompt = """
            Actúa como un estilista personal experto.
            
            CONTEXTO:
            El usuario quiere: "$userPrompt".
            
            INVENTARIO DISPONIBLE (Lista filtrada de su armario):
            $wardrobeText
            
            TAREA:
            Crea 1 o 2 outfits completos usando SOLO las prendas del inventario de arriba.
            
            FORMATO DE RESPUESTA REQUERIDO:
            Por favor, responde usando este formato exacto para que yo pueda leerlo después:
            
            OUTFIT 1:
            - IDs: [id_prenda_1, id_prenda_2, ...]
            - Explicación: (Breve razón de por qué queda bien)
            
            OUTFIT 2 (si aplica):
            - IDs: [id_prenda_1, id_prenda_2, ...]
            - Explicación: ...
            
            Si no encuentras ropa suficiente para combinar, dímelo claramente.
        """.trimIndent()

        // 3. Enviamos a la IA
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "No he podido generar una respuesta."
        } catch (e: Exception) {
            "Error al conectar con Gemini: ${e.message}"
        }
    }
}
