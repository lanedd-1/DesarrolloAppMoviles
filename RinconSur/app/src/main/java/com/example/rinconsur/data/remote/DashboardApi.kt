package com.example.rinconsur.data.remote

import com.example.rinconsur.data.FlujoAcceso
import retrofit2.http.GET
import retrofit2.http.Query

data class ItemDistribucionResponse(
    val etiqueta: String,
    val cantidad: Int
)

data class ResumenDiarioResponse(
    val fecha: String,
    val totalMuestras: Int,
    val sesiones: Int,
    val flujoPorAcceso: List<ItemDistribucionResponse>
)

data class DistribucionResponse(
    val fecha: String?,
    val acceso: String?,
    val total: Int,
    val porClasificacion: List<ItemDistribucionResponse>,
    val porRangoEtario: List<ItemDistribucionResponse>,
    val porCompania: List<ItemDistribucionResponse>,
    val porBolsas: List<ItemDistribucionResponse>
)

data class RegistroDetalleResponse(
    val id: Long,
    val sesionId: Long,
    val acceso: String,
    val fecha: String,
    val hora: String,
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val bolsas: Boolean,
    val destino: String?
)

interface DashboardApi {

    @GET("api/dashboard/fechas")
    suspend fun fechas(): List<String>

    @GET("api/dashboard/resumen")
    suspend fun resumen(@Query("fecha") fecha: String): ResumenDiarioResponse

    @GET("api/dashboard/distribucion")
    suspend fun distribucion(
        @Query("fecha") fecha: String?,
        @Query("acceso") acceso: String?
    ): DistribucionResponse

    @GET("api/dashboard/registros")
    suspend fun registros(
        @Query("fecha") fecha: String?,
        @Query("acceso") acceso: String?
    ): List<RegistroDetalleResponse>
}

fun ResumenDiarioResponse.aFlujosAcceso(): List<FlujoAcceso> =
    flujoPorAcceso
        .sortedByDescending { it.cantidad }
        .map {
            FlujoAcceso(
                acceso = it.etiqueta,
                cantidad = it.cantidad,
                estado = if (it.cantidad >= 20) "Alto Tráfico" else "Normal"
            )
        }