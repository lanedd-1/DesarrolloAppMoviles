package com.example.rinconsur.data

import com.example.rinconsur.data.remote.DashboardApi
import com.example.rinconsur.data.remote.DistribucionResponse
import com.example.rinconsur.data.remote.ItemDistribucionResponse
import com.example.rinconsur.data.remote.RegistroDetalleResponse
import com.example.rinconsur.data.remote.ResumenDiarioResponse
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeDashboardApi : DashboardApi {

    var sinConexion = false
    var errorHttp: Pair<Int, String>? = null

    var ultimaFechaResumen: String? = null
    var ultimaConsultaDistribucion: Pair<String?, String?>? = null
    var ultimaConsultaRegistros: Pair<String?, String?>? = null

    private val listaFechas = listOf("2026-11-22", "2026-11-21")

    private val resumenFalso = ResumenDiarioResponse(
        fecha = "2026-11-22",
        totalMuestras = 45,
        sesiones = 3,
        flujoPorAcceso = listOf(
            ItemDistribucionResponse("Acceso Norte", 25),
            ItemDistribucionResponse("Tránsito", 20)
        )
    )

    private val distribucionFalsa = DistribucionResponse(
        fecha = "2026-11-22",
        acceso = "Acceso Norte",
        total = 25,
        porClasificacion = listOf(ItemDistribucionResponse("Cliente", 20)),
        porRangoEtario = listOf(ItemDistribucionResponse("Adulto", 18)),
        porCompania = listOf(ItemDistribucionResponse("Solo", 15)),
        porBolsas = listOf(ItemDistribucionResponse("Con bolsas", 10))
    )

    // Desordenados a propósito para comprobar que DashboardRepository los ordena por id descendente
    private val registrosFalsos = listOf(
        RegistroDetalleResponse(1L, 10L, "Tránsito", "2026-11-22", "10:15", "Cliente", "Adulto", "Solo", false, "Patio"),
        RegistroDetalleResponse(5L, 10L, "Tránsito", "2026-11-22", "11:30", "Proveedor", "Adulto", "En grupo", true, "Bodega")
    )

    private fun verificarErrorSimulado() {
        if (sinConexion) throw IOException("Servidor apagado")
        errorHttp?.let { (codigo, mensaje) ->
            val json = """{"message":"$mensaje"}"""
            val cuerpoError = ResponseBody.create(MediaType.parse("application/json"), json)
            throw HttpException(Response.error<Any>(codigo, cuerpoError))
        }
    }

    override suspend fun fechas(): List<String> {
        verificarErrorSimulado()
        return listaFechas
    }

    override suspend fun resumen(fecha: String): ResumenDiarioResponse {
        verificarErrorSimulado()
        ultimaFechaResumen = fecha
        return resumenFalso.copy(fecha = fecha)
    }

    override suspend fun distribucion(fecha: String?, acceso: String?): DistribucionResponse {
        verificarErrorSimulado()
        ultimaConsultaDistribucion = fecha to acceso
        return distribucionFalsa.copy(fecha = fecha, acceso = acceso)
    }

    override suspend fun registros(fecha: String?, acceso: String?): List<RegistroDetalleResponse> {
        verificarErrorSimulado()
        ultimaConsultaRegistros = fecha to acceso
        return registrosFalsos
    }
}