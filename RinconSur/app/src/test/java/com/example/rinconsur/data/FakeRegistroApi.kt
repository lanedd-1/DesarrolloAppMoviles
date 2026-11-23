package com.example.rinconsur.data

import com.example.rinconsur.data.remote.EditarRegistroRequest
import com.example.rinconsur.data.remote.RegistroApi
import com.example.rinconsur.data.remote.RegistroDetalleResponse
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeRegistroApi : RegistroApi {

    var sinConexion = false
    var errorHttp: Pair<Int, String>? = null

    var ultimoEditar: Pair<Long, EditarRegistroRequest>? = null
    var ultimoEliminarId: Long? = null

    private fun verificarErrorSimulado() {
        if (sinConexion) throw IOException("Servidor apagado")
        errorHttp?.let { (codigo, mensaje) ->
            val json = """{"message":"$mensaje"}"""
            val cuerpoError = ResponseBody.create(MediaType.parse("application/json"), json)
            throw HttpException(Response.error<Any>(codigo, cuerpoError))
        }
    }

    override suspend fun editar(id: Long, datos: EditarRegistroRequest): RegistroDetalleResponse {
        verificarErrorSimulado()
        ultimoEditar = id to datos
        return RegistroDetalleResponse(
            id = id,
            sesionId = 1L,
            acceso = "Acceso Norte",
            fecha = "2026-11-22",
            hora = "12:00",
            clasificacion = datos.clasificacion,
            rangoEtario = datos.rangoEtario,
            compania = datos.compania,
            bolsas = datos.bolsas,
            destino = datos.destino
        )
    }

    override suspend fun eliminar(id: Long) {
        verificarErrorSimulado()
        ultimoEliminarId = id
    }
}