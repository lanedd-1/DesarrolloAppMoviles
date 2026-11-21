package com.example.rinconsur.model

data class SesionItem(
    val id: Long,
    val codigo: String,
    val acceso: String,
    val fecha: String,
    val estado: String,
    val cantidadRegistros: Int = 0
)