package com.martigonzalez.project_icloth.data

//import androidx.compose.ui.semantics.text
import com.google.ai.client.generativeai.GenerativeModel
import com.martigonzalez.project_icloth.model.Prenda

class GeminiManager {

    private val apiKey = "AIzaSa.... etc (PON AQUÍ LA TUYA)"
    //TODO: por ahora usar la de cada uno. BORRAR antes del commit !!!!
    // https://aistudio.google.com/api-keys --> para copiar tu clave

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
            Por favor, responde usando este formato exacto para que yo pueda leerlo después.
            Tu respuesta DEBE tener estrictamente este formato con separadores "|||":
            
            El orden debe ser:
            1. Texto de introducción amigable y breve.
            2. IDs del primer outfit (separados por coma).
            3. Explicación del primer outfit.
            4. IDs del segundo outfit (si lo hay).
            5. Explicación del segundo outfit (si lo hay).
            
            Ejemplo de respuesta válida:
            Hola, he encontrado estas opciones para ti.
            |||
            id_camisa, id_pantalon
            |||
            Este look es ideal porque combina colores tierra...
            |||
            id_vestido, id_zapatos
            |||
            Esta opción es más fresca para el verano...
            
            Si no encuentras ropa suficiente para combinar, dímelo claramente.
        """.trimIndent()

        // 3. Enviamos a la IA
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "No he podido generar una respuesta."
        } catch (e: Exception) {
            "Error al conectar con la API de Gemini: ${e.message}"
        }
    }
}
