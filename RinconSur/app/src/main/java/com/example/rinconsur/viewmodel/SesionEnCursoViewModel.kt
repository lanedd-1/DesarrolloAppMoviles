package com.example.rinconsur.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rinconsur.model.AccesoDestinoEnum
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RangoEtarioEnum
import com.example.rinconsur.model.SesionEnCursoUiState
import com.example.rinconsur.model.TipoCompaniaEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SesionEnCursoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SesionEnCursoUiState())
    val uiState: StateFlow<SesionEnCursoUiState> = _uiState.asStateFlow()

    // Historial temporal en memoria para permitir anular el último registro
    private val historialRegistros = mutableListOf<String>()

    init {
        // Inicializa con el resumen por defecto
        historialRegistros.add("Tránsito · 26–40 · Solo")
    }

    fun onClasificacionSeleccionada(opcion: ClasificacionPersona) {
        _uiState.update { it.copy(clasificacionSeleccionada = opcion) }
    }

    fun onRangoEtarioSeleccionado(rango: RangoEtarioEnum) {
        _uiState.update { it.copy(rangoEtarioSeleccionado = rango) }
    }

    fun onCompaniaSeleccionada(compania: TipoCompaniaEnum) {
        _uiState.update { it.copy(companiaSeleccionada = compania) }
    }

    fun onBolsasOtraTiendaCambia(traeBolsas: Boolean) {
        _uiState.update { it.copy(traeBolsasOtraTienda = traeBolsas) }
    }

    fun onDestinoSeleccionado(destino: AccesoDestinoEnum) {
        _uiState.update { it.copy(destinoSeleccionado = destino) }
    }

    fun onGuardarRegistro() {
        val estado = _uiState.value
        val resumen = "${estado.clasificacionSeleccionada.etiqueta} · ${estado.rangoEtarioSeleccionado.etiqueta} · ${estado.companiaSeleccionada.etiqueta}"

        historialRegistros.add(resumen)

        _uiState.update {
            it.copy(
                totalRegistrosSesion = it.totalRegistrosSesion + 1,
                ultimoRegistroResumen = resumen,
                permiteAnular = true
            )
        }
    }

    fun onAnularUltimoRegistro() {
        if (historialRegistros.isNotEmpty()) {
            historialRegistros.removeAt(historialRegistros.size - 1)
            val resumenAnterior = historialRegistros.lastOrNull() ?: "Sin registros"

            _uiState.update {
                it.copy(
                    totalRegistrosSesion = (it.totalRegistrosSesion - 1).coerceAtLeast(0),
                    ultimoRegistroResumen = resumenAnterior,
                    permiteAnular = historialRegistros.isNotEmpty()
                )
            }
        }
    }
}