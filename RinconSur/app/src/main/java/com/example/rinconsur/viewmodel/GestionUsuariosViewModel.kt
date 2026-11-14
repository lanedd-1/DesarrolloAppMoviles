package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.GestionUsuariosUiState
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GestionUsuariosViewModel(
    private val repositorio: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    fun cargarUsuarios(contexto: Context) {
        _uiState.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            try {
                val usuarios = repositorio.listar()
                _uiState.update { it.copy(usuarios = usuarios, cargando = false, mensajeError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, mensajeError = mensajeDeError(e, contexto), mensajeExito = null)
                }
            }
        }
    }

    fun cambiarEstadoUsuario(context: Context, usuario: Usuario, nuevoEstado: Boolean, mensajePlantilla: String) {
        actualizarUsuario(context, usuario, mensajePlantilla) {
            repositorio.cambiarEstado(usuario.id, nuevoEstado)
        }
    }

    fun cambiarRolUsuario(contexto: Context, usuario: Usuario, nuevoRolId: Long, mensajePlantilla: String) {
        actualizarUsuario(contexto, usuario, mensajePlantilla) {
            repositorio.cambiarRol(usuario.id, nuevoRolId)
        }
    }

    private fun actualizarUsuario(
        contexto: Context,
        usuario: Usuario,
        mensajePlantilla: String,
        peticion: suspend () -> Usuario
    ) {
        viewModelScope.launch {
            try {
                val actualizado = peticion()
                _uiState.update { estado ->
                    estado.copy(
                        usuarios = estado.usuarios.map { if (it.id == actualizado.id) actualizado else it }
                    )
                }
                mostrarExito(String.format(mensajePlantilla, usuario.nombre))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarError(mensajeDeError(e, contexto))
            }
        }
    }

    private fun mensajeDeError(e: Exception, contexto: Context): String =
        if (e is ApiException) e.textoParaUsuario(contexto) else contexto.getString(R.string.error_inesperado)

    private fun mostrarExito(mensaje: String) {
        _uiState.update { it.copy(mensajeExito = mensaje, mensajeError = null) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(mensajeExito = null) }
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(mensajeError = mensaje, mensajeExito = null) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(mensajeError = null) }
        }
    }
}