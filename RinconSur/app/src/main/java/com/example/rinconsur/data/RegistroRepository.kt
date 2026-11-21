package com.example.rinconsur.data

import com.example.rinconsur.data.remote.EditarRegistroRequest
import com.example.rinconsur.data.remote.RegistroApi
import com.example.rinconsur.data.remote.RetrofitClient

class RegistroRepository(
    private val registroApi: RegistroApi = RetrofitClient.registroApi
) {

    suspend fun editar(
        id: Long,
        clasificacion: String,
        rangoEtario: String,
        compania: String,
        bolsas: Boolean,
        destino: String?
    ) {
        llamarApi {
            registroApi.editar(
                id,
                EditarRegistroRequest(clasificacion, rangoEtario, compania, bolsas, destino)
            )
        }
    }

    suspend fun eliminar(id: Long) {
        llamarApi { registroApi.eliminar(id) }
    }
}