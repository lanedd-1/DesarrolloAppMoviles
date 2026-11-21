package com.example.rinconsur.data

import com.example.rinconsur.data.remote.DashboardApi
import com.example.rinconsur.data.remote.DistribucionResponse
import com.example.rinconsur.data.remote.ResumenDiarioResponse
import com.example.rinconsur.data.remote.RetrofitClient

class DashboardRepository(
    private val dashboardApi: DashboardApi = RetrofitClient.dashboardApi
) {

    suspend fun fechasDisponibles(): List<String> =
        llamarApi { dashboardApi.fechas() }

    suspend fun resumenDiario(fecha: String): ResumenDiarioResponse =
        llamarApi { dashboardApi.resumen(fecha) }

    suspend fun distribucion(fecha: String, acceso: String): DistribucionResponse =
        llamarApi { dashboardApi.distribucion(fecha, acceso) }
}