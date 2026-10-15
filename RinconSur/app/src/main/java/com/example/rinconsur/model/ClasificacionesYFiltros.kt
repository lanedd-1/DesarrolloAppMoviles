package com.example.rinconsur.model

// =====================================================================
//  ClasificacionesYFiltros.kt — OPCIONES PARA LOS BOTONES TÁCTILES
//  ---------------------------------------------------------------------
//  Representa los valores fijos permitidos por las reglas de negocio
//  (RN-06, RN-07, RN-08) para el muestreo de pasillo.
// =====================================================================

enum class ClasificacionPersona(val etiqueta: String) {
    TRANSITO("Tránsito"),
    VISITA("Visita"),
    NO_DETERMINADO("No det.")
}

enum class RangoEtarioEnum(val etiqueta: String) {
    MENOR_18("< 18"),
    EDAD_18_25("18–25"),
    EDAD_26_40("26–40"),
    EDAD_41_60("41–60"),
    MAYOR_61("61+")
}

enum class TipoCompaniaEnum(val etiqueta: String) {
    SOLO("Solo"),
    PAREJA("Pareja"),
    CON_NINOS("Con niños"),
    GRUPO("Grupo 3+")
}

enum class AccesoDestinoEnum(val codigo: String) {
    A1("A1"),
    A2("A2"),
    A3("A3"),
    A4("A4"),
}