package com.example.rinconsur.model

// =====================================================================
//  SesionEnCursoUiState.kt — ESTADO DE LA PANTALLA DE REGISTRO EN VIVO
//  ---------------------------------------------------------------------
//  Mantiene todo lo que la vista necesita para dibujarse y actualizarse
//  inmediatamente al tocar un botón en el modo "one-tap".
// =====================================================================

data class SesionEnCursoUiState(
    val accesoNombre: String = "Acceso A4",
    val tiempoTranscurridosMin: Int = 14,

    // Opciones seleccionadas actualmente en el formulario táctil
    val clasificacion: ClasificacionPersona = ClasificacionPersona.TRANSITO,
    val rangoEtario: RangoEtarioEnum = RangoEtarioEnum.EDAD_26_40,
    val compania: TipoCompaniaEnum = TipoCompaniaEnum.SOLO,
    val traeBolsasOtraTienda: Boolean = false,
    val destinoTrandito: AccesoDestinoEnum = AccesoDestinoEnum.A1,

    // Estadísticas rápidas en pantalla
    val totalRegistrosSesion: Int = 42,
    val ultimoRegistroTexto: String = "Tránsito · 26–40 · Solo",
    val guardandoLocalOffline: Boolean = true
)