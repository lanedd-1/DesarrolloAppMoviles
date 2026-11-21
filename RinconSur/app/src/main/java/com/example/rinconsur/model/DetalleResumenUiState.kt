package com.example.rinconsur.model

import com.example.rinconsur.data.RegistroDetalleItem

data class DetalleResumenUiState(
    val acceso: String = "",
    val fecha: String = "",
    val registros: List<RegistroDetalleItem> = emptyList(),
    val totalRegistros: Int = 0,
    val cargando: Boolean = false,
    val mensajeError: String? = null
)