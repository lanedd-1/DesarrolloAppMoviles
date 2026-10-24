package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RegistroDetalleItem
import com.example.rinconsur.data.RinconDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetalleResumenUiState(
    val acceso: String = "",
    val fecha: String = "",
    val registros: List<RegistroDetalleItem> = emptyList(),
    val totalRegistros: Int = 0
)

class DetalleResumenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleResumenUiState())
    val uiState: StateFlow<DetalleResumenUiState> = _uiState.asStateFlow()

    fun cargarDetalles(context: Context, acceso: String, fecha: String) {
        val dbHelper = RinconDbHelper(context)
        val lista = dbHelper.obtenerRegistrosPorAccesoYFecha(acceso, fecha)

        _uiState.update {
            it.copy(
                acceso = acceso,
                fecha = fecha,
                registros = lista,
                totalRegistros = lista.size
            )
        }
    }

    fun eliminarRegistro(context: Context, idRegistro: Long) {
        val dbHelper = RinconDbHelper(context)
        dbHelper.eliminarRegistroPorId(idRegistro)

        // Recargamos los datos para actualizar la UI
        val current = _uiState.value
        cargarDetalles(context, current.acceso, current.fecha)
    }

    fun actualizarRegistro(
        context: Context,
        idRegistro: Long,
        clasificacion: String,
        rangoEtario: String,
        compania: String,
        bolsas: Boolean,
        destino: String?
    ) {
        val dbHelper = RinconDbHelper(context)
        dbHelper.actualizarRegistroEspecifico(idRegistro, clasificacion, rangoEtario, compania, bolsas, destino)

        // Recargamos los datos para actualizar la UI
        val current = _uiState.value
        cargarDetalles(context, current.acceso, current.fecha)
    }
}