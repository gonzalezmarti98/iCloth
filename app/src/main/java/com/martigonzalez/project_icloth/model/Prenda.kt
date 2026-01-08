package com.martigonzalez.project_icloth.model

// Modelo de datos que SÍ corresponde con tu layout
data class Prenda(
    var id: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",
    val categoria: String = "",
    val colorPpal: String = "",
    val ocasion: String = "",
    val temporada: String = "",
    val nivelFormalidad: String = "",
    val ajuste: String = "",
    val patron: String = ""

) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}