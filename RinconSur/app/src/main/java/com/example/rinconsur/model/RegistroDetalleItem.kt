package com.example.rinconsur.model

data class RegistroDetalleItem(
    val id: Long,
    val hora: String,
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val bolsas: Boolean,
    val destino: String?
)