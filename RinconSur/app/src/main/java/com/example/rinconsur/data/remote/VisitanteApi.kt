package com.example.rinconsur.data.remote

import com.example.rinconsur.data.RegistroDetalleItem
import com.example.rinconsur.data.SesionItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class SesionResponse(
    val id: Long,
    val codigo: String,
    val acceso: String,
    val fecha: String,
    val estado: String,
    val usuarioId: Long,
    val cantidadRegistros: Int
)

data class RegistroResponse(
    val id: Long,
    val hora: String,
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val bolsas: Boolean,
    val destino: String?
)

data class EstadisticasResponse(
    val totalSesiones: Int,
    val sesionesCompletadas: Int,
    val muestrasRegistradas: Int
)

data class CrearSesionRequest(
    val usuarioId: Long,
    val acceso: String
)

data class GuardarRegistroRequest(
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val bolsas: Boolean,
    val destino: String?,
    val hora: String
)

interface VisitanteApi {

    @GET("api/sesiones")
    suspend fun listarSesiones(@Query("usuarioId") usuarioId: Long): List<SesionResponse>

    @GET("api/sesiones/estadisticas")
    suspend fun estadisticas(@Query("usuarioId") usuarioId: Long): EstadisticasResponse

    @GET("api/sesiones/{id}")
    suspend fun obtenerSesion(@Path("id") id: Long): SesionResponse

    @POST("api/sesiones")
    suspend fun crearSesion(@Body datos: CrearSesionRequest): SesionResponse

    @PUT("api/sesiones/{id}/finalizar")
    suspend fun finalizarSesion(@Path("id") id: Long): SesionResponse

    @GET("api/sesiones/{id}/registros")
    suspend fun listarRegistros(@Path("id") id: Long): List<RegistroResponse>

    @POST("api/sesiones/{id}/registros")
    suspend fun guardarRegistro(@Path("id") id: Long, @Body datos: GuardarRegistroRequest): RegistroResponse

    @DELETE("api/sesiones/{id}/registros/ultimo")
    suspend fun anularUltimoRegistro(@Path("id") id: Long)
}

fun SesionResponse.aSesionItem() = SesionItem(
    id = id,
    codigo = codigo,
    acceso = acceso,
    fecha = fecha,
    estado = estado,
    cantidadRegistros = cantidadRegistros
)

fun RegistroResponse.aRegistroDetalleItem() = RegistroDetalleItem(
    id = id,
    hora = hora,
    clasificacion = clasificacion,
    rangoEtario = rangoEtario,
    compania = compania,
    bolsas = bolsas,
    destino = destino
)