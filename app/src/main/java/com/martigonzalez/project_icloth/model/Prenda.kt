package com.martigonzalez.project_icloth.model

// Modelo de datos que SÍ corresponde con tu layout
data class Prenda(
    var id: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",
    val categoria: String = "", // He aumentado bastante el array
    val colorPpal: String = "", // Para el color seleccionado en el RecyclerView
    val ocasion: String = "",
    val temporada: String = "",
    val nivelFormalidad: String = "",
    val ajuste: String = "",
    val patron: String = ""

) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}

/*
ATRIBUTOS que podríamos MODIFICAR:
- val categoria --> que sea una lista que según lo q elijas, luego salga "val tipo: String"
    ROPA_SUPERIOR - val tipo - camiseta corta, camiseta larga, camisa, chaqueta, jersey, polo, sudadera, térmica, top
    ROPA_INFERIOR - val tipo - falda, leggins, Pantalón corto, Pantalón largo
    CALZADO       - val tipo - botas, chanclas, sandalias, zapatos, zapatillas
    ACCESORIOS    - val tipo - bufanda, bolso, cinturón, corbata, gorra, mochila, sombrero
    INTERIOR      - val tipo - bragas, calzoncillos, sujetador
    DE UNA PIEZA  - val tipo - mono, vestido

ATRIBUTOS A AÑADIR:
- colorSec: ninguno, (los colores del ppal)
 */