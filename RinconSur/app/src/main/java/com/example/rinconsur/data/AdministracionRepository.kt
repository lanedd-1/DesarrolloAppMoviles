package com.example.rinconsur.data

import com.example.rinconsur.data.remote.AdministracionApi
import com.example.rinconsur.data.remote.CambiarDisponibilidadRequest
import com.example.rinconsur.data.remote.RetrofitClient

class AdministracionRepository(
    private val administracionApi: AdministracionApi = RetrofitClient.administracionApi
) {
    
    suspend fun estadoAccesos(): Map<String, Boolean> =
        llamarApi { administracionApi.listarAccesos(null) }
            .associate { it.nombre to it.disponible }

    suspend fun accesosCerrados(): List<String> =
        llamarApi { administracionApi.listarAccesos(false) }
            .map { it.nombre }

    suspend fun cambiarDisponibilidad(nombre: String, disponible: Boolean): Boolean =
        llamarApi { administracionApi.cambiarDisponibilidad(nombre, CambiarDisponibilidadRequest(disponible)) }
            .disponible
}