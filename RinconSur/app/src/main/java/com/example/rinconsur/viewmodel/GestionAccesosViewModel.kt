package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.AdministracionRepository
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.GestionAccesosUiState
import com.example.rinconsur.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GestionAccesosViewModel(
    private val usuarioRepositorio: UsuarioRepository = UsuarioRepository(),
    private val administracionRepositorio: AdministracionRepository = AdministracionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionAccesosUiState())
    val uiState: StateFlow<GestionAccesosUiState> = _uiState.asStateFlow()

    fun cargarDatos(contexto: Context) {
        val idUsuario = SesionPreferencias(contexto).obtenerIdUsuario()
        if (idUsuario == -1L) {
            _uiState.update { it.copy(esJefeTienda = false) }
            return
        }

        _uiState.update { it.copy(cargando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                val usuario = usuarioRepositorio.obtener(idUsuario)

                if (usuario.rolId == Rol.ID_JEFE_TIENDA) {
                    val mapa = administracionRepositorio.estadoAccesos()
                    _uiState.update {
                        it.copy(
                            estadoAccesosMap = mapa,
                            esJefeTienda = true,
                            cargando = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(esJefeTienda = false, cargando = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, contexto)) }
            }
        }
    }

    fun onCambiarEstadoAcceso(contexto: Context, accesoNombre: String, nuevoEstado: Boolean) {
        if (_uiState.value.cargando) return

        _uiState.update { it.copy(cargando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                val disponible = administracionRepositorio.cambiarDisponibilidad(accesoNombre, nuevoEstado)

                _uiState.update {
                    it.copy(
                        estadoAccesosMap = it.estadoAccesosMap + (accesoNombre to disponible),
                        mensajeExito = contexto.getString(R.string.gestion_accesos_mensaje_exito, accesoNombre),
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, contexto)) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    private fun textoDeError(e: Exception, contexto: Context): String =
        if (e is ApiException) e.textoParaUsuario(contexto) else contexto.getString(R.string.error_inesperado)
}