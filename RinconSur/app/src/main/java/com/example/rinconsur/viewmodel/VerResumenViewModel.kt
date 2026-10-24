package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VerResumenUiState(
    val acceso: String = "",
    val fecha: String = "",
    val totalMuestras: Int = 0,
    val distribucionClasificacion: List<Pair<String, Int>> = emptyList()
)

class VerResumenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VerResumenUiState())
    val uiState: StateFlow<VerResumenUiState> = _uiState.asStateFlow()

    fun cargarDatos(context: Context, acceso: String, fecha: String) {
        val dbHelper = RinconDbHelper(context)
        val distribucion = dbHelper.obtenerDistribucionClasificacion(acceso, fecha)
        val total = distribucion.sumOf { it.second }

        _uiState.update {
            it.copy(
                acceso = acceso,
                fecha = fecha,
                totalMuestras = total,
                distribucionClasificacion = distribucion
            )
        }
    }
}