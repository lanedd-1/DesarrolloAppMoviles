package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.DashboardRepository
import com.example.rinconsur.data.remote.ItemDistribucionResponse
import com.example.rinconsur.model.VerResumenUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerResumenViewModel(
    private val dashboardRepositorio: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerResumenUiState())
    val uiState: StateFlow<VerResumenUiState> = _uiState.asStateFlow()

    fun cargarDatos(contexto: Context, acceso: String, fecha: String) {
        if (_uiState.value.cargando) return

        _uiState.update { it.copy(acceso = acceso, fecha = fecha, cargando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                val respuesta = dashboardRepositorio.distribucion(fecha, acceso)

                _uiState.update { estadoActual ->
                    val conDatos = estadoActual.copy(
                        totalMuestras = respuesta.total,
                        distribucionClasificacion = respuesta.porClasificacion.aPares(),
                        distribucionEdades = respuesta.porRangoEtario.aPares(),
                        distribucionCompania = respuesta.porCompania.aPares(),
                        distribucionBolsas = respuesta.porBolsas.aPares()
                    )
                    conDatos.copy(
                        datosGrafico = datosDelFiltro(conDatos, conDatos.filtroSeleccionado),
                        cargando = false,
                        mensajeError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, contexto)) }
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                filtroSeleccionado = nuevoFiltro,
                datosGrafico = datosDelFiltro(estadoActual, nuevoFiltro)
            )
        }
    }

    private fun datosDelFiltro(estado: VerResumenUiState, filtro: String): List<Pair<String, Int>> =
        when (filtro) {
            "Rango Etario" -> estado.distribucionEdades
            "Compañía" -> estado.distribucionCompania
            "Bolsas de otra tienda" -> estado.distribucionBolsas
            else -> estado.distribucionClasificacion
        }

    private fun List<ItemDistribucionResponse>.aPares(): List<Pair<String, Int>> =
        map { it.etiqueta to it.cantidad }

    private fun textoDeError(e: Exception, contexto: Context): String =
        if (e is ApiException) e.textoParaUsuario(contexto) else contexto.getString(R.string.error_inesperado)
}