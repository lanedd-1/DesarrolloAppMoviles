package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

data class EditarRegistroRequest(
    val clasificacion: String,
    val rangoEtario: String,
    val compania: String,
    val bolsas: Boolean,
    val destino: String?
)

interface RegistroApi {

    @PUT("api/registros/{id}")
    suspend fun editar(
        @Path("id") id: Long,
        @Body datos: EditarRegistroRequest
    ): RegistroDetalleResponse

    @DELETE("api/registros/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}