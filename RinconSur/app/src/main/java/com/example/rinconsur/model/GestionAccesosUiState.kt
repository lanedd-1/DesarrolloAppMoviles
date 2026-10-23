package com.example.rinconsur.model

data class GestionAccesosUiState(
    val estadoAccesosMap: Map<String, Boolean> = emptyMap(),
    val esJefeTienda: Boolean = true,
    val mensajeExito: String? = null
)