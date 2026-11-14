package com.example.rinconsur.model

data class GestionUsuariosUiState(
    val usuarios: List<Usuario> = emptyList(),
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val cargando: Boolean = false
)