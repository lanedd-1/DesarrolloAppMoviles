package com.example.rinconsur.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object OlvideContrasena : Ruta("olvide_contrasena")

    data object CambiarContrasena : Ruta("cambiar_contrasena/{correo}") {
        fun crear(correo: String) = "cambiar_contrasena/$correo"
    }
    data object ResumenDiario : Ruta("resumen_diario")

    // ESTA ES LA NUEVA RUTA DEL GRÁFICO
    data object VerResumen : Ruta("ver_resumen/{acceso}/{fecha}") {
        fun crear(acceso: String, fecha: String) = "ver_resumen/$acceso/$fecha"
    }

    data object DetalleResumen : Ruta("detalle_resumen/{acceso}/{fecha}") {
        fun crear(acceso: String, fecha: String) = "detalle_resumen/$acceso/$fecha"
    }

    data object GestionAccesos : Ruta("gestion_accesos")
    data object MisSesiones : Ruta("mis_sesiones")
    data object Formulario : Ruta("formulario")

    data object SesionEnCurso : Ruta("sesion_en_curso/{id}/{acceso}") {
        fun crear(id: Long, acceso: String) = "sesion_en_curso/$id/$acceso"
    }
}