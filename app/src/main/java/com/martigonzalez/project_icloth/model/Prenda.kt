package com.martigonzalez.project_icloth.model

data class Prenda (
    val id: String,
    val nombre: String,
    val imagenUrl:String,
    val categoria: String, // camiseta, pantalones, zapato, etc
    val colorPpal: String,
    val colorSec: List<String>,
    val estampado: String, //rallas, topos, degradado
    val marca: String,
    val formalLvl: Int, // 0-10 o de 0-5, como veas
    val deporteLvl: Int, // "         "
    val temporada: String, // primavera, verano, otoño, invierno
    val ajuste: String // 4 posibles: estrecho, regular, ancho, oversize
)
