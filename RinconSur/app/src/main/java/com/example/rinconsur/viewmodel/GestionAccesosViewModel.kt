package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.GestionAccesosUiState
import com.example.rinconsur.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GestionAccesosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GestionAccesosUiState())
    val uiState: StateFlow<GestionAccesosUiState> = _uiState.asStateFlow()

    fun cargarDatos(contexto: Context) {
        val dbHelper = RinconDbHelper(contexto)
        val idUsuario = SesionPreferencias(contexto).obtenerIdUsuario()
        val usuario = dbHelper.buscarPorId(idUsuario)

        val esJefe = usuario?.rolId == Rol.ID_JEFE_TIENDA

        if (esJefe) {
            val mapa = dbHelper.obtenerEstadoTodosAccesos()
            _uiState.update {
                it.copy(
                    estadoAccesosMap = mapa,
                    esJefeTienda = true
                )
            }
        } else {
            _uiState.update {
                it.copy(esJefeTienda = false)
            }
        }
    }

    fun onCambiarEstadoAcceso(contexto: Context, accesoNombre: String, nuevoEstado: Boolean) {
        val dbHelper = RinconDbHelper(contexto)
        val actualizado = dbHelper.cambiarDisponibilidadAcceso(accesoNombre, nuevoEstado)

        if (actualizado) {
            val mapaActualizado = dbHelper.obtenerEstadoTodosAccesos()
            _uiState.update {
                it.copy(
                    estadoAccesosMap = mapaActualizado,
                    mensajeExito = contexto.getString(R.string.gestion_accesos_mensaje_exito, accesoNombre)
                )
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeExito = null) }
    }
}