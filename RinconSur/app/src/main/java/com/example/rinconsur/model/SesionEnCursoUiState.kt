package com.example.rinconsur.model

data class SesionEnCursoUiState(
    val accesoNombre: String = "Acceso A4",
    val ubicacionAcceso: String = "Conexión Ferretería",
    val tiempoTranscurridoMin: Int = 14,
    val observadorNombre: String = "Tamara R.",
    val fecha: String = "15-09-2026",
    val guardadoLocalActivo: Boolean = true,

    // Selecciones actuales (one-tap)
    val clasificacionSeleccionada: ClasificacionPersona = ClasificacionPersona.TRANSITO,
    val rangoEtarioSeleccionado: RangoEtarioEnum = RangoEtarioEnum.EDAD_26_40,
    val companiaSeleccionada: TipoCompaniaEnum = TipoCompaniaEnum.SOLO,
    val traeBolsasOtraTienda: Boolean = false,
    val destinoSeleccionado: AccesoDestinoEnum = AccesoDestinoEnum.A1,

    // Historial y métricas de la sesión activa
    val totalRegistrosSesion: Int = 42,
    val ultimoRegistroResumen: String = "Tránsito · 26–40 · Solo",
    val permiteAnular: Boolean = true
)