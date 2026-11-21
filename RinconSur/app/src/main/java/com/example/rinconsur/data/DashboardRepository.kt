package com.example.rinconsur.data

import com.example.rinconsur.data.remote.DashboardApi
import com.example.rinconsur.data.remote.DistribucionResponse
import com.example.rinconsur.data.remote.ResumenDiarioResponse
import com.example.rinconsur.data.remote.RetrofitClient
import com.example.rinconsur.model.RegistroDetalleItem

class DashboardRepository(
    private val dashboardApi: DashboardApi = RetrofitClient.dashboardApi
) {

    suspend fun fechasDisponibles(): List<String> =
        llamarApi { dashboardApi.fechas() }

    suspend fun resumenDiario(fecha: String): ResumenDiarioResponse =
        llamarApi { dashboardApi.resumen(fecha) }

    suspend fun distribucion(fecha: String, acceso: String): DistribucionResponse =
        llamarApi { dashboardApi.distribucion(fecha, acceso) }

    suspend fun registros(fecha: String, acceso: String): List<RegistroDetalleItem> =
        llamarApi { dashboardApi.registros(fecha, acceso) }
            .sortedByDescending { it.id }
            .map {
                RegistroDetalleItem(
                    id = it.id,
                    hora = it.hora,
                    clasificacion = it.clasificacion,
                    rangoEtario = it.rangoEtario,
                    compania = it.compania,
                    bolsas = it.bolsas,
                    destino = it.destino
                )
            }
}