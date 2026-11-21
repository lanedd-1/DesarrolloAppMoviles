package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.DashboardRepository
import com.example.rinconsur.data.RegistroRepository
import com.example.rinconsur.model.DetalleResumenUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleResumenViewModel(
    private val dashboardRepositorio: DashboardRepository = DashboardRepository(),
    private val registroRepositorio: RegistroRepository = RegistroRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleResumenUiState())
    val uiState: StateFlow<DetalleResumenUiState> = _uiState.asStateFlow()

    fun cargarDetalles(contexto: Context, acceso: String, fecha: String) {
        _uiState.update { it.copy(acceso = acceso, fecha = fecha) }
        ejecutar(contexto) { }
    }

    fun eliminarRegistro(contexto: Context, idRegistro: Long) {
        ejecutar(contexto) { registroRepositorio.eliminar(idRegistro) }
    }

    fun actualizarRegistro(
        contexto: Context,
        idRegistro: Long,
        clasificacion: String,
        rangoEtario: String,
        compania: String,
        bolsas: Boolean,
        destino: String?
    ) {
        ejecutar(contexto) {
            registroRepositorio.editar(idRegistro, clasificacion, rangoEtario, compania, bolsas, destino)
        }
    }

    private fun ejecutar(contexto: Context, accion: suspend () -> Unit) {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                accion()

                val estado = _uiState.value
                val lista = dashboardRepositorio.registros(estado.fecha, estado.acceso)

                _uiState.update {
                    it.copy(
                        registros = lista,
                        totalRegistros = lista.size,
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

    private fun textoDeError(e: Exception, contexto: Context): String =
        if (e is ApiException) e.textoParaUsuario(contexto) else contexto.getString(R.string.error_inesperado)
}