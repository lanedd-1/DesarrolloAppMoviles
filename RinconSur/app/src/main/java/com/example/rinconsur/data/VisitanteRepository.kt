package com.example.rinconsur.data

import com.example.rinconsur.data.remote.CrearSesionRequest
import com.example.rinconsur.data.remote.EstadisticasResponse
import com.example.rinconsur.data.remote.GuardarRegistroRequest
import com.example.rinconsur.data.remote.RetrofitClient
import com.example.rinconsur.data.remote.VisitanteApi
import com.example.rinconsur.data.remote.aRegistroDetalleItem
import com.example.rinconsur.data.remote.aSesionItem
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

class VisitanteRepository(
    private val visitanteApi: VisitanteApi = RetrofitClient.visitanteApi
) {

    suspend fun listarSesiones(usuarioId: Long): List<SesionItem> =
        llamar { visitanteApi.listarSesiones(usuarioId) }.map { it.aSesionItem() }

    suspend fun estadisticas(usuarioId: Long): EstadisticasResponse =
        llamar { visitanteApi.estadisticas(usuarioId) }

    suspend fun obtenerSesion(id: Long): SesionItem =
        llamar { visitanteApi.obtenerSesion(id) }.aSesionItem()

    suspend fun crearSesion(usuarioId: Long, acceso: String): SesionItem =
        llamar { visitanteApi.crearSesion(CrearSesionRequest(usuarioId, acceso)) }.aSesionItem()

    suspend fun finalizarSesion(id: Long): SesionItem =
        llamar { visitanteApi.finalizarSesion(id) }.aSesionItem()

    suspend fun listarRegistros(sesionId: Long): List<RegistroDetalleItem> =
        llamar { visitanteApi.listarRegistros(sesionId) }.map { it.aRegistroDetalleItem() }

    suspend fun guardarRegistro(
        sesionId: Long,
        clasificacion: String,
        rangoEtario: String,
        compania: String,
        bolsas: Boolean,
        destino: String?,
        hora: String
    ): RegistroDetalleItem =
        llamar {
            visitanteApi.guardarRegistro(
                sesionId,
                GuardarRegistroRequest(clasificacion, rangoEtario, compania, bolsas, destino, hora)
            )
        }.aRegistroDetalleItem()

    suspend fun anularUltimoRegistro(sesionId: Long) =
        llamar { visitanteApi.anularUltimoRegistro(sesionId) }

    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw ApiException(e.code(), mensajeDelServidor(e))
    } catch (e: IOException) {
        throw ApiException(0, "")
    }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        return try {
            JsonParser.parseString(cuerpo ?: "").asJsonObject.get("message")?.asString ?: ""
        } catch (ex: Exception) {
            ""
        }
    }
}