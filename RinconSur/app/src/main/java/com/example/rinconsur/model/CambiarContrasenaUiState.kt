package com.example.rinconsur.model

data class CambiarContrasenaUiState(
    val contrasenaNueva: String = "",
    val confirmarContrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val errorContrasenaNueva: String? = null,
    val errorConfirmarContrasena: String? = null,
    val cambioExitoso: Boolean = false,
    val cargando: Boolean = false
)