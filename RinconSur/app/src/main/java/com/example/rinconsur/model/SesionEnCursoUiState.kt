package com.example.rinconsur.model

data class SesionEnCursoUiState(
    val idSesionActual: Long = 1L,
    val accesoNombre: String = "Acceso A4",
    val ubicacionAcceso: String = "Conexión Ferretería",
    val tiempoTranscurridoMin: Int = 14,
    val observadorNombre: String = "Tamara R.",
    val fecha: String = "15-09-2026",
    val guardadoLocalActivo: Boolean = true,

    val clasificacionSeleccionada: ClasificacionPersona = ClasificacionPersona.TRANSITO,
    val rangoEtarioSeleccionado: RangoEtarioEnum = RangoEtarioEnum.EDAD_26_40,
    val companiaSeleccionada: TipoCompaniaEnum = TipoCompaniaEnum.SOLO,
    val traeBolsasOtraTienda: Boolean = false,
    val destinoSeleccionado: AccesoDestinoEnum = AccesoDestinoEnum.A1,

    val totalRegistrosSesion: Int = 0,
    val ultimoRegistroResumen: String = "No hay Registros",
    val permiteAnular: Boolean = true,

    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val sesionFinalizada: Boolean = false
)