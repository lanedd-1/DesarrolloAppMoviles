package com.example.rinconsur.navigation

import android.net.Uri

// =====================================================================
//  Ruta.kt — Identificadores de pantalla para el NavHost
// =====================================================================

sealed class Ruta(val ruta: String) {

    // Pantalla inicial de inicio de sesión
    data object Login : Ruta("login")

    // Pantalla principal del observador (con el menú hamburguesa)
    data object MisSesiones : Ruta("mis_sesiones")

    // Pantalla para crear un nuevo usuario/colaborador
    data object Formulario : Ruta("formulario")

    // Recuperación de contraseña (correo + código SMS)
    data object OlvideContrasena : Ruta("olvide_contrasena")

    // Nueva contraseña: recibe el correo de la cuenta a actualizar
    data object CambiarContrasena : Ruta("cambiar_contrasena/{correo}") {
        fun crear(correo: String) = "cambiar_contrasena/${Uri.encode(correo)}"
    }

    // Pantalla de conteo que recibe qué puerta/acceso se seleccionó
    data object SesionEnCurso : Ruta("sesion_en_curso/{acceso}") {
        // Función helper para construir la ruta con el parámetro real
        fun crear(acceso: String) = "sesion_en_curso/$acceso"
    }
}