package com.example.rinconsur.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val id: Long = 0,
    val rolId: Long = Rol.ID_OBSERVADOR,
    val rolNombre: String = "Observador de Pasillo"
)