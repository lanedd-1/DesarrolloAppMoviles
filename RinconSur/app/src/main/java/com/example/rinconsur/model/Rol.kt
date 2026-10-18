package com.example.rinconsur.model

// =====================================================================
//  Rol.kt — Rincón Sur
//  ---------------------------------------------------------------------
//  Representa una fila de la tabla "roles".
//  Un rol tiene MUCHOS usuarios; cada usuario tiene UN rol (relación 1 a N).
// =====================================================================

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        // Constantes de IDs de roles precargados en la BD de Rincón Sur
        const val ID_OBSERVADOR = 1L
        const val ID_JEFE_TIENDA = 2L
        const val ID_ADMINISTRADOR = 3L
    }
}