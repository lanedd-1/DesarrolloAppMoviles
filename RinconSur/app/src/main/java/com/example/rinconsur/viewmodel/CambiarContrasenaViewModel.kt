package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.CambiarContrasenaUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CambiarContrasenaViewModel(
    private val repositorio: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CambiarContrasenaUiState())
    val uiState: StateFlow<CambiarContrasenaUiState> = _uiState.asStateFlow()

    fun onContrasenaNuevaCambia(valor: String) {
        _uiState.update { it.copy(contrasenaNueva = valor, errorContrasenaNueva = null) }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmarContrasena = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onGuardarContrasena(correoUsuario: String, codigo: String, contexto: Context) {
        val estado = _uiState.value
        if (estado.cargando) return

        val patronContrasena = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-]).{8,}$".toRegex()

        var errorNueva = when {
            estado.contrasenaNueva.isBlank() -> contexto.getString(R.string.cambiar_error_obligatoria)
            !estado.contrasenaNueva.matches(patronContrasena) ->
                contexto.getString(R.string.cambiar_error_formato)
            else -> null
        }

        val errorConfirmar = when {
            estado.confirmarContrasena.isBlank() -> contexto.getString(R.string.cambiar_error_confirmar_obligatoria)
            estado.contrasenaNueva != estado.confirmarContrasena -> contexto.getString(R.string.cambiar_error_no_coinciden)
            else -> null
        }

        if (errorNueva == null && (correoUsuario.isBlank() || codigo.isBlank())) {
            errorNueva = contexto.getString(R.string.cambiar_error_cuenta_no_identificada)
        }

        if (errorNueva != null || errorConfirmar != null) {
            _uiState.update {
                it.copy(
                    errorContrasenaNueva = errorNueva,
                    errorConfirmarContrasena = errorConfirmar,
                    cambioExitoso = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(errorContrasenaNueva = null, errorConfirmarContrasena = null, cargando = true)
        }

        viewModelScope.launch {
            try {
                repositorio.restablecerContrasena(correoUsuario, codigo, estado.contrasenaNueva)
                _uiState.update { it.copy(cargando = false, cambioExitoso = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _uiState.update {
                    it.copy(cargando = false, errorContrasenaNueva = e.textoParaUsuario(contexto))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorContrasenaNueva = contexto.getString(R.string.error_inesperado))
                }
            }
        }
    }
}