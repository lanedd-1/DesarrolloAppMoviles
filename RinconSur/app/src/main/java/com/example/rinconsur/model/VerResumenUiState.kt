package com.example.rinconsur.model

data class VerResumenUiState(
    val acceso: String = "",
    val fecha: String = "",
    val totalMuestras: Int = 0,

    val filtrosDisponibles: List<String> = listOf("Clasificación", "Rango Etario", "Compañía", "Bolsas de otra tienda"),
    val filtroSeleccionado: String = "Clasificación",
    val datosGrafico: List<Pair<String, Int>> = emptyList(),

    val distribucionClasificacion: List<Pair<String, Int>> = emptyList(),
    val distribucionEdades: List<Pair<String, Int>> = emptyList(),
    val distribucionCompania: List<Pair<String, Int>> = emptyList(),
    val distribucionBolsas: List<Pair<String, Int>> = emptyList(),

    val cargando: Boolean = false,
    val mensajeError: String? = null
)