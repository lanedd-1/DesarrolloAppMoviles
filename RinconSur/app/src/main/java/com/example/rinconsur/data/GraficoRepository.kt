package com.example.rinconsur.data

import kotlin.random.Random


class GraficoRepository {
    private val flujoPorClasificacion = listOf(
        "CLIENTE" to 45,
        "TRANSITO" to 28,
        "PROVEEDOR" to 12,
        "PERSONAL" to 8
    )

    private val flujoPorCompania = listOf(
        "SOLO" to 30,
        "PAREJA" to 22,
        "GRUPO" to 15,
        "CON_NINOS" to 8
    )

    fun obtenerFlujoPorClasificacion(): List<Pair<String, Int>> = flujoPorClasificacion

    fun obtenerFlujoPorCompania(): List<Pair<String, Int>> = flujoPorCompania

    fun generarFlujoAleatorio(): List<Pair<String, Int>> =
        flujoPorClasificacion.map { (etiqueta, _) ->
            etiqueta to Random.nextInt(5, 60)
        }

    fun generarCompaniaAleatoria(): List<Pair<String, Int>> =
        flujoPorCompania.map { (etiqueta, _) ->
            etiqueta to Random.nextInt(2, 35)
        }
}