package com.martigonzalez.project_icloth.model

// Modelo de datos que SÍ corresponde con tu layout
data class Prenda(
    var id: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",
    val categoria: String = "",
    val colorPpal: String = "", // Para el color seleccionado en el RecyclerView
    val ocasion: String = ""

) {
    // Constructor vacío requerido por Firestore. No lo borres.
    constructor() : this("", "", "", "", "", "")
}
