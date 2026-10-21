package com.example.rinconsur.navigation

// =====================================================================
//  Rutas.kt — Rincón Sur
//  ---------------------------------------------------------------------
//  Sealed class con data objects para rutas type-safe en Compose.
// =====================================================================

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object MisSesiones : Ruta("mis_sesiones")
    data object Formulario : Ruta("formulario")

    // Ruta parametrizada por código de acceso (ej: A1, A2, A3, A4)
    data object SesionEnCurso : Ruta("sesion_en_curso/{acceso}") {
        fun crear(acceso: String) = "sesion_en_curso/$acceso"
    }
}