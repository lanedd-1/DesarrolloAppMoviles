package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class AccesoResponse(
    val nombre: String,
    val disponible: Boolean
)

data class CambiarDisponibilidadRequest(
    val disponible: Boolean
)

interface AdministracionApi {

    @GET("api/accesos")
    suspend fun listarAccesos(@Query("disponible") disponible: Boolean?): List<AccesoResponse>

    @PUT("api/accesos/{nombre}/disponibilidad")
    suspend fun cambiarDisponibilidad(
        @Path("nombre") nombre: String,
        @Body datos: CambiarDisponibilidadRequest
    ): AccesoResponse
}