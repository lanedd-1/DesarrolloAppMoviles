package com.example.rinconsur.model


data class OlvideContrasenaUiState(
    val correo: String = "",
    val codigoCorreo: String = "",
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val codigoEnviado: Boolean = false,
    val mensajeExito: String? = null,
    val mostrarDialogoSuspendido: Boolean = false
)