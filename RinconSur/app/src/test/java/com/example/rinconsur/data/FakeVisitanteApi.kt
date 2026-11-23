package com.example.rinconsur.data

import com.example.rinconsur.data.remote.CrearSesionRequest
import com.example.rinconsur.data.remote.EstadisticasResponse
import com.example.rinconsur.data.remote.GuardarRegistroRequest
import com.example.rinconsur.data.remote.RegistroResponse
import com.example.rinconsur.data.remote.SesionResponse
import com.example.rinconsur.data.remote.VisitanteApi
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeVisitanteApi : VisitanteApi {

    private val sesiones = listOf(
        SesionResponse(1L, "SES-001", "Acceso Norte", "2026-11-22", "En curso", 10L, 4),
        SesionResponse(2L, "SES-002", "Tránsito", "2026-11-21", "Finalizada", 10L, 12)
    )

    private val registros = listOf(
        RegistroResponse(100L, "09:30", "Cliente", "Adulto", "Solo", true, null),
        RegistroResponse(101L, "09:45", "Trabajador", "Adulto", "En grupo", false, "Oficinas")
    )

    var sinConexion = false
    var errorHttp: Pair<Int, String>? = null

    var ultimoCrearSesion: CrearSesionRequest? = null
    var ultimaSesionFinalizadaId: Long? = null
    var ultimoGuardarRegistro: Pair<Long, GuardarRegistroRequest>? = null
    var ultimaAnulacionSesionId: Long? = null

    private fun verificarErrorSimulado() {
        if (sinConexion) throw IOException("Servidor apagado")
        errorHttp?.let { (codigo, mensaje) ->
            val json = """{"message":"$mensaje"}"""
            val cuerpoError = ResponseBody.create(MediaType.parse("application/json"), json)
            throw HttpException(Response.error<Any>(codigo, cuerpoError))
        }
    }

    override suspend fun listarSesiones(usuarioId: Long): List<SesionResponse> {
        verificarErrorSimulado()
        return sesiones.filter { it.usuarioId == usuarioId }
    }

    override suspend fun estadisticas(usuarioId: Long): EstadisticasResponse {
        verificarErrorSimulado()
        return EstadisticasResponse(totalSesiones = 2, sesionesCompletadas = 1, muestrasRegistradas = 16)
    }

    override suspend fun obtenerSesion(id: Long): SesionResponse {
        verificarErrorSimulado()
        return sesiones.first { it.id == id }
    }

    override suspend fun listarActivas(): List<SesionResponse> {
        verificarErrorSimulado()
        return sesiones.filter { it.estado == "En curso" }
    }

    override suspend fun crearSesion(datos: CrearSesionRequest): SesionResponse {
        verificarErrorSimulado()
        ultimoCrearSesion = datos
        return SesionResponse(3L, "SES-003", datos.acceso, "2026-11-22", "En curso", datos.usuarioId, 0)
    }

    override suspend fun finalizarSesion(id: Long): SesionResponse {
        verificarErrorSimulado()
        ultimaSesionFinalizadaId = id
        return sesiones.first { it.id == id }.copy(estado = "Finalizada")
    }

    override suspend fun listarRegistros(id: Long): List<RegistroResponse> {
        verificarErrorSimulado()
        return registros
    }

    override suspend fun guardarRegistro(id: Long, datos: GuardarRegistroRequest): RegistroResponse {
        verificarErrorSimulado()
        ultimoGuardarRegistro = id to datos
        return RegistroResponse(
            id = 102L,
            hora = datos.hora,
            clasificacion = datos.clasificacion,
            rangoEtario = datos.rangoEtario,
            compania = datos.compania,
            bolsas = datos.bolsas,
            destino = datos.destino
        )
    }

    override suspend fun anularUltimoRegistro(id: Long) {
        verificarErrorSimulado()
        ultimaAnulacionSesionId = id
    }
}