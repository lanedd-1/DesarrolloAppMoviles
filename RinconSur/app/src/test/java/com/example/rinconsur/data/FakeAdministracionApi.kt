package com.example.rinconsur.data

import com.example.rinconsur.data.remote.AccesoResponse
import com.example.rinconsur.data.remote.AdministracionApi
import com.example.rinconsur.data.remote.CambiarDisponibilidadRequest
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeAdministracionApi : AdministracionApi {

    private val accesos = listOf(
        AccesoResponse(nombre = "Acceso Norte", disponible = true),
        AccesoResponse(nombre = "Acceso Sur", disponible = false),
        AccesoResponse(nombre = "Tránsito", disponible = true)
    )

    var sinConexion = false
    var errorHttp: Pair<Int, String>? = null

    var ultimoFiltroDisponible: Boolean? = true
    var ultimoCambio: Pair<String, CambiarDisponibilidadRequest>? = null

    private fun verificarErrorSimulado() {
        if (sinConexion) throw IOException("Servidor apagado")
        errorHttp?.let { (codigo, mensaje) ->
            val json = """{"message":"$mensaje"}"""
            val cuerpoError = ResponseBody.create(MediaType.parse("application/json"), json)
            throw HttpException(Response.error<Any>(codigo, cuerpoError))
        }
    }

    override suspend fun listarAccesos(disponible: Boolean?): List<AccesoResponse> {
        verificarErrorSimulado()
        ultimoFiltroDisponible = disponible
        return if (disponible == null) {
            accesos
        } else {
            accesos.filter { it.disponible == disponible }
        }
    }

    override suspend fun cambiarDisponibilidad(
        nombre: String,
        datos: CambiarDisponibilidadRequest
    ): AccesoResponse {
        verificarErrorSimulado()
        ultimoCambio = nombre to datos
        return AccesoResponse(nombre = nombre, disponible = datos.disponible)
    }
}