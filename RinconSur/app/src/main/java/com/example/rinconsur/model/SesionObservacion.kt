package com.example.rinconsur.model

// =====================================================================
//  SesionObservacion.kt — MODELO DE DATOS DE UNA SESIÓN
//  ---------------------------------------------------------------------
//  Estructura que viene del backend o de la base de datos local Room.
// =====================================================================

data class SesionObservacion(
    val idSesion: Int,
    val codigo: String,          // Ej: "S-0001"
    val accesoCodigo: String,    // Ej: "A4"
    val accesoNombre: String,    // Ej: "Conexión Ferretería"
    val horarioProgramado: String, // Ej: "13:05 – 13:50"
    val fecha: String,           // Ej: "15-09-2026"
    val estado: String           // "PROGRAMADA", "FINALIZADA"
)