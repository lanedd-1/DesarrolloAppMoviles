package com.example.rinconsur.model

data class FormularioUiState(
    val nombre: String = "",
    val correo: String = "",
    val rolSeleccionado: String = "Observador de Pasillo",
    val rolesDisponibles: List<String> = listOf(
        "Observador de Pasillo",
        "Jefe de Tienda",
        "Administrador"
    ),
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val contrasenaVisible: Boolean = false,

    // Las 5 variables de error para los componentes de la interfaz
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorRol: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,

    val registroExitoso: Boolean = false,
    val mensajeExito: String? = null
)