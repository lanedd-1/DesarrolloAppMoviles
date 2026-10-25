package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestionUsuariosUiState(
    val usuarios: List<Usuario> = emptyList(),
    val mensajeExito: String? = null
)

class GestionUsuariosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    fun cargarUsuarios(context: Context) {
        val dbHelper = RinconDbHelper(context)
        _uiState.update { it.copy(usuarios = dbHelper.obtenerTodosLosUsuarios()) }
    }

    fun cambiarEstadoUsuario(context: Context, usuario: Usuario, nuevoEstado: Boolean, mensajePlantilla: String) {
        val dbHelper = RinconDbHelper(context)
        val exito = dbHelper.cambiarEstadoUsuario(usuario.id, nuevoEstado)

        if (exito) {
            cargarUsuarios(context) // Recargar lista

            // Mostrar el mensaje en el Banner y ocultarlo después de 3 segundos
            _uiState.update { it.copy(mensajeExito = String.format(mensajePlantilla, usuario.nombre)) }
            viewModelScope.launch {
                delay(3000)
                _uiState.update { it.copy(mensajeExito = null) }
            }
        }
    }
    fun cambiarRolUsuario(contexto: Context, usuario: Usuario, nuevoRolId: Long, mensajePlantilla: String) {
        val dbHelper = RinconDbHelper(contexto)
        val exito = dbHelper.actualizarRolUsuario(usuario.id, nuevoRolId)

        if (exito) {
            cargarUsuarios(contexto) // Recargar lista con el nuevo rol

            _uiState.update { it.copy(mensajeExito = String.format(mensajePlantilla, usuario.nombre)) }
            viewModelScope.launch {
                delay(3000)
                _uiState.update { it.copy(mensajeExito = null) }
            }
        }
    }
}