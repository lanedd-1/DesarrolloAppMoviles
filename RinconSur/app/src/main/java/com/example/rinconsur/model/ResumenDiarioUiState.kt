package com.example.rinconsur.model

import com.example.rinconsur.data.FlujoAcceso

data class ResumenDiarioUiState(
    val iniciales: String = "",
    val nombreUsuario: String = "",
    val rolUsuario: String = "",
    val fechaSeleccionada: String = "",
    val labelFecha: String = "",
    val fechasDisponibles: List<String> = emptyList(),
    val totalIngresos: Int = 0,
    val totalAccesos: Int = 0,
    val flujos: List<FlujoAcceso> = emptyList(),
    val cargando: Boolean = false,
    val mensajeError: String? = null
)