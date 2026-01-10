# iCloth 👕 - Tu Estilista Personal con IA

iCloth es una aplicación Android nativa que reinventa la forma en que interactúas con tu armario. Usando la potencia de **Google Gemini**, la app te permite catalogar tus prendas y recibir sugerencias de outfits inteligentes y personalizados. ¿No sabes qué ponerte? ¡Pregúntale a iCloth!

## 📜 Descripción del Proyecto

El objetivo de iCloth es solucionar el eterno dilema de "¿qué me pongo hoy?". La app permite a los usuarios:
-   **Digitalizar su armario:** Añadir prendas mediante fotos y detallar sus características (color, categoría, ocasión, etc.).
-   **Chatear con un Estilista IA:** Mantener una conversación con una IA (Gemini) para pedirle recomendaciones de outfits para cualquier situación.
-   **Recibir Propuestas Visuales:** Obtener combinaciones de ropa de su propio armario, mostradas visualmente y con una explicación sobre por qué es una buena elección.

## ✨ Características Principales

*   **Autenticación de Usuarios:** Sistema de registro y login seguro con Firebase Authentication.
*   **Armario Virtual:** Galería visual de todas las prendas del usuario en una cuadrícula.
*   **Gestión de Prendas (CRUD):**
    *   Añadir prendas desde la cámara o la galería.
    *   Ver los detalles de cada prenda en un diálogo.
    *   Eliminar prendas que ya no tienes.
*   **Chat con IA (Google Gemini):**
    *   Interfaz de chat intuitiva para hacer peticiones en lenguaje natural.
    *   Sistema de filtrado por etiquetas para afinar las búsquedas.
    *   Generación de outfits en tiempo real basados en el inventario del usuario.

## 🛠️ Tecnologías y Arquitectura

Este proyecto está construido sobre un stack moderno de desarrollo para Android, aprovechando los servicios de Firebase como backend.

*   **Lenguaje:** Kotlin
*   **Base de Datos:**
    *   **Cloud Firestore:** Para almacenar los metadatos de las prendas y la información del usuario.
    *   **Firebase Storage:** Para almacenar las imágenes de las prendas.
*   **Autenticación:** Firebase Authentication (Email y Contraseña).
*   **Inteligencia Artificial:** Google AI - Gemini 2.5 Flash.
*   **Librerías Principales:**
    *   `com.google.android.material:material` para componentes de UI.
    *   `androidx.recyclerview:recyclerview` para listas dinámicas.
    *   `com.github.bumptech.glide:glide` para la carga eficiente de imágenes.
    *   `com.google.ai.client.generativeai:generativeai` para la integración con Gemini.

## 🚀 Cómo Empezar

Para clonar y ejecutar este proyecto en tu propio entorno, necesitarás seguir estos pasos.

### 1. Prerrequisitos

-   Android Studio.
-   Una cuenta de Google para acceder a Firebase.

### 2. Configuración de Firebase

Este proyecto depende de Firebase para funcionar.

1.  Ve a la [Consola de Firebase](https://console.firebase.google.com/) y crea un nuevo proyecto.
2.  Dentro de tu proyecto, añade una nueva **Aplicación Android**.
3.  Usa el nombre de paquete: `com.martigonzalez.project_icloth`.
4.  Descarga el archivo `google-services.json` que te proporcionará Firebase y **pégalo dentro de la carpeta `app/` de tu proyecto**.
5.  En la consola de Firebase, ve a la sección **Authentication** y habilita el proveedor **Email/Contraseña**.
6.  Ve a la sección **Firestore Database** y crea una base de datos (puedes empezar en modo de prueba).
7.  Ve a la sección **Storage** y configúralo también.

### 3. Configuración de la API de Gemini 🔑

La IA de la aplicación necesita una clave de API para poder generar las respuestas.

1.  Ve a [Google AI Studio](https://aistudio.google.com/api-keys).
2.  Haz clic en **"Create API key"** para generar una nueva clave.
3.  Copia la clave generada.
4.  En Android Studio, navega hasta el archivo:
    `app/src/main/java/com/martigonzalez/project_icloth/data/GeminiManager.kt`
5.  Localiza en la línea 11 esta sección de código y cambia ```GeminiApiKey``` por tu API key:
    ```
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = GeminiApiKey  // <--- ¡AQUÍ!
    )
    ```

## 📸 Screenshots


    
