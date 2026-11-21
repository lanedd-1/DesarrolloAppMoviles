package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.DashboardRepository
import com.example.rinconsur.data.remote.ResumenDiarioResponse
import com.example.rinconsur.data.remote.aFlujosAcceso
import com.example.rinconsur.model.ResumenDiarioUiState
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResumenDiarioViewModel(
    private val dashboardRepositorio: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenDiarioUiState())
    val uiState: StateFlow<ResumenDiarioUiState> = _uiState.asStateFlow()

    private var usuarioMemoria: Usuario? = null

    fun cargarDatos(contexto: Context, usuario: Usuario?) {
        usuarioMemoria = usuario
        actualizarPerfil(contexto)

        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                var fechas = dashboardRepositorio.fechasDisponibles()
                if (fechas.isEmpty()) {
                    fechas = listOf(fechaHoy())
                }

                val fechaActiva = _uiState.value.fechaSeleccionada.ifEmpty { fechas.first() }
                val resumen = dashboardRepositorio.resumenDiario(fechaActiva)

                aplicarResumen(contexto, fechaActiva, fechas, resumen)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, contexto)) }
            }
        }
    }

    fun seleccionarFecha(contexto: Context, nuevaFecha: String) {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                val resumen = dashboardRepositorio.resumenDiario(nuevaFecha)
                aplicarResumen(contexto, nuevaFecha, _uiState.value.fechasDisponibles, resumen)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, contexto)) }
            }
        }
    }

    private fun aplicarResumen(
        contexto: Context,
        fecha: String,
        fechas: List<String>,
        resumen: ResumenDiarioResponse
    ) {
        val flujos = resumen.aFlujosAcceso()

        val label = if (fecha == fechaHoy()) {
            contexto.getString(R.string.resumen_diario_label_hoy, fecha)
        } else {
            fecha
        }

        _uiState.update {
            it.copy(
                fechaSeleccionada = fecha,
                labelFecha = label,
                fechasDisponibles = fechas,
                totalIngresos = resumen.totalMuestras,
                totalAccesos = flujos.size,
                flujos = flujos,
                cargando = false,
                mensajeError = null
            )
        }
    }

    private fun actualizarPerfil(contexto: Context) {
        val usuario = usuarioMemoria

        val inicialesStr = if (usuario != null) {
            val partes = usuario.nombre.split(" ")
            if (partes.size >= 2) "${partes[0].first()}${partes[1].first()}".uppercase()
            else usuario.nombre.take(2).uppercase()
        } else "RS"

        val nombreAbreviado = if (usuario != null) {
            val partes = usuario.nombre.split(" ")
            if (partes.size >= 2) "${partes[0]} ${partes[1].first()}."
            else usuario.nombre
        } else "Cargando..."

        val rolStr = if (usuario?.rolId == Rol.ID_ADMINISTRADOR) {
            contexto.getString(R.string.rol_resumen_admin)
        } else {
            contexto.getString(R.string.rol_resumen_jefatura)
        }

        _uiState.update {
            it.copy(
                iniciales = inicialesStr,
                nombreUsuario = nombreAbreviado,
                rolUsuario = rolStr
            )
        }
    }

    private fun fechaHoy(): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    private fun textoDeError(e: Exception, contexto: Context): String =
        if (e is ApiException) e.textoParaUsuario(contexto) else contexto.getString(R.string.error_inesperado)
}