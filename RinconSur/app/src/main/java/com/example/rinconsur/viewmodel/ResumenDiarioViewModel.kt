package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.FlujoAcceso
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ResumenDiarioUiState(
    val iniciales: String = "",
    val nombreUsuario: String = "",
    val rolUsuario: String = "",
    val fechaSeleccionada: String = "",
    val labelFecha: String = "",
    val fechasDisponibles: List<String> = emptyList(),
    val totalIngresos: Int = 0,
    val totalAccesos: Int = 0,
    val flujos: List<FlujoAcceso> = emptyList()
)

class ResumenDiarioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ResumenDiarioUiState())
    val uiState: StateFlow<ResumenDiarioUiState> = _uiState.asStateFlow()

    private var usuarioMemoria: Usuario? = null

    fun cargarDatos(context: Context, usuario: Usuario?) {
        this.usuarioMemoria = usuario
        val dbHelper = RinconDbHelper(context)
        val fechaHoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        var fechas = dbHelper.obtenerFechasDisponibles()
        if (fechas.isEmpty()) {
            fechas = listOf(fechaHoy)
        }

        val fechaActiva = if (_uiState.value.fechaSeleccionada.isEmpty()) fechas.first() else _uiState.value.fechaSeleccionada

        actualizarMetricasPorFecha(context, fechaActiva, fechas, fechaHoy)
    }

    fun seleccionarFecha(context: Context, nuevaFecha: String) {
        val fechaHoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        actualizarMetricasPorFecha(context, nuevaFecha, _uiState.value.fechasDisponibles, fechaHoy)
    }

    private fun actualizarMetricasPorFecha(context: Context, fechaConsulta: String, listaFechas: List<String>, fechaHoy: String) {
        val dbHelper = RinconDbHelper(context)
        val total = dbHelper.obtenerTotalMuestrasPorFecha(fechaConsulta)
        val flujos = dbHelper.obtenerFlujoPorAcceso(fechaConsulta)

        val inicialesStr = if (usuarioMemoria != null) {
            val partes = usuarioMemoria!!.nombre.split(" ")
            if (partes.size >= 2) "${partes[0].first()}${partes[1].first()}".uppercase()
            else usuarioMemoria!!.nombre.take(2).uppercase()
        } else "RS"

        val nombreAbreviado = if (usuarioMemoria != null) {
            val partes = usuarioMemoria!!.nombre.split(" ")
            if (partes.size >= 2) "${partes[0]} ${partes[1].first()}."
            else usuarioMemoria!!.nombre
        } else "Cargando..."

        val rolStr = if (usuarioMemoria?.rolId == Rol.ID_ADMINISTRADOR) {
            context.getString(R.string.rol_resumen_admin)
        } else {
            context.getString(R.string.rol_resumen_jefatura)
        }

        val label = if (fechaConsulta == fechaHoy) {
            context.getString(R.string.resumen_diario_label_hoy, fechaConsulta)
        } else {
            fechaConsulta
        }

        _uiState.update {
            it.copy(
                iniciales = inicialesStr,
                nombreUsuario = nombreAbreviado,
                rolUsuario = rolStr,
                fechaSeleccionada = fechaConsulta,
                labelFecha = label,
                fechasDisponibles = listaFechas,
                totalIngresos = total,
                totalAccesos = flujos.size,
                flujos = flujos
            )
        }
    }
}