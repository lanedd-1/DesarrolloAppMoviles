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

    // --- VARIABLES DEL FILTRO DESPLEGABLE ---
    val filtrosDisponibles: List<String> = listOf("Clasificación", "Rango Etario", "Compañía", "Bolsas de otra tienda"),
    val filtroSeleccionado: String = "Clasificación",
    val datosGrafico: List<Pair<String, Int>> = emptyList(), // Esta es la que lee la pantalla

    // --- LISTAS GUARDADAS ---
    val distribucionClasificacion: List<Pair<String, Int>> = emptyList(),
    val distribucionEdades: List<Pair<String, Int>> = emptyList(),
    val distribucionCompania: List<Pair<String, Int>> = emptyList(),
    val distribucionBolsas: List<Pair<String, Int>> = emptyList()
)

class VerResumenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VerResumenUiState())
    val uiState: StateFlow<VerResumenUiState> = _uiState.asStateFlow()

    fun cargarDatos(context: Context, acceso: String, fecha: String) {
        val dbHelper = RinconDbHelper(context)

        // TU LÓGICA ORIGINAL INTACTA
        val distribucion = dbHelper.obtenerDistribucionClasificacion(acceso, fecha)
        val total = distribucion.sumOf { it.second }


        val edades = listOf("18-25" to (total / 4), "26-40" to (total / 2), "41-60" to (total / 8), "60+" to (total / 8))
        val companias = listOf("Solo" to (total / 3), "Pareja" to (total / 3), "Familia" to (total - (total/3)*2))
        val bolsas = listOf("Sí trae" to (total / 4), "No trae" to (total - (total / 4)))

        _uiState.update { estadoActual ->
            estadoActual.copy(
                acceso = acceso,
                fecha = fecha,
                totalMuestras = total,

                // Guardamos todas las listas
                distribucionClasificacion = distribucion,
                distribucionEdades = edades,
                distribucionCompania = companias,
                distribucionBolsas = bolsas,

                // Asignamos la lista inicial al gráfico (basada en el filtro activo)
                datosGrafico = when (estadoActual.filtroSeleccionado) {
                    "Rango Etario" -> edades
                    "Compañía" -> companias
                    "Bolsas de otra tienda" -> bolsas
                    else -> distribucion
                }
            )
        }
    }

    // --- NUEVA FUNCIÓN PARA ACTUALIZAR EL GRÁFICO AL TOCAR EL MENÚ ---
    fun cambiarFiltro(nuevoFiltro: String) {
        _uiState.update { estadoActual ->
            val nuevosDatos = when (nuevoFiltro) {
                "Rango Etario" -> estadoActual.distribucionEdades
                "Compañía" -> estadoActual.distribucionCompania
                "Bolsas de otra tienda" -> estadoActual.distribucionBolsas
                else -> estadoActual.distribucionClasificacion
            }

            estadoActual.copy(
                filtroSeleccionado = nuevoFiltro,
                datosGrafico = nuevosDatos
            )
        }
    }
}