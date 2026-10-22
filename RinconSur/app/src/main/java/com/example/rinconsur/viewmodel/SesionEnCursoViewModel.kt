package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.model.AccesoDestinoEnum
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RangoEtarioEnum
import com.example.rinconsur.model.SesionEnCursoUiState
import com.example.rinconsur.model.TipoCompaniaEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SesionEnCursoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SesionEnCursoUiState())
    val uiState: StateFlow<SesionEnCursoUiState> = _uiState.asStateFlow()

    private val historialRegistros = mutableListOf<String>()

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

    fun onGuardarRegistro(context: Context) {
        val estado = _uiState.value
        val resumen = "${estado.clasificacionSeleccionada.etiqueta} · ${estado.rangoEtarioSeleccionado.etiqueta} · ${estado.companiaSeleccionada.etiqueta}"
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val destinoFinal = if (estado.clasificacionSeleccionada == ClasificacionPersona.TRANSITO) {
            estado.destinoSeleccionado.codigo
        } else {
            null
        }

        val dbHelper = RinconDbHelper(context)
        dbHelper.guardarRegistroConteo(
            sesionId = estado.idSesionActual,
            clasificacion = estado.clasificacionSeleccionada.name,
            rangoEtario = estado.rangoEtarioSeleccionado.name,
            compania = estado.companiaSeleccionada.name,
            bolsas = estado.traeBolsasOtraTienda,
            destino = destinoFinal,
            hora = horaActual
        )

        historialRegistros.add(resumen)

        _uiState.update {
            it.copy(
                totalRegistrosSesion = it.totalRegistrosSesion + 1,
                ultimoRegistroResumen = resumen,
                permiteAnular = true
            )
        }
    }

    fun finalizarSesionConteo(context: Context) {
        val estado = _uiState.value
        val dbHelper = RinconDbHelper(context)
        dbHelper.finalizarSesion(estado.idSesionActual)
    }

    fun onAnularUltimoRegistro(context: Context) {
        if (historialRegistros.isNotEmpty()) {
            val estado = _uiState.value
            val dbHelper = RinconDbHelper(context)

            val eliminadoEnBD = dbHelper.eliminarUltimoRegistro(estado.idSesionActual)

            if (eliminadoEnBD) {
                historialRegistros.removeAt(historialRegistros.size - 1)
                val resumenAnterior = historialRegistros.lastOrNull() ?: "Sin registros previos"

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
}