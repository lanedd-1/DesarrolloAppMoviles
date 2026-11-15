package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.LoginUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repositorio: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        if (valor.length <= 50) {
            _uiState.update {
                it.copy(
                    correo = valor.trim(),
                    errorCorreo = null,
                    loginExitoso = false
                )
            }
        }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = valor,
                errorContrasena = null,
                loginExitoso = false
            )
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onCerrarDialogoSuspendido() {
        _uiState.update { it.copy(mostrarDialogoSuspendido = false) }
    }

    fun onIngresar(contexto: Context) {
        val estado = _uiState.value
        if (estado.cargando) return

        val correoLimpio = estado.correo.trim().lowercase()

        val errorCorreo = if (correoLimpio.isBlank()) {
            contexto.getString(R.string.login_error_correo_obligatorio)
        } else if (correoLimpio.length > 50) {
            contexto.getString(R.string.formulario_error_correo_largo)
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            contexto.getString(R.string.login_error_correo_institucional)
        } else {
            null
        }

        val errorContrasena = if (estado.contrasena.isBlank()) {
            contexto.getString(R.string.login_error_contrasena_obligatoria)
        } else {
            null
        }

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update {
                it.copy(
                    errorCorreo = errorCorreo,
                    errorContrasena = errorContrasena,
                    loginExitoso = false
                )
            }
            return
        }

        _uiState.update { it.copy(errorCorreo = null, errorContrasena = null, cargando = true) }

        viewModelScope.launch {
            try {
                val usuario = repositorio.iniciarSesion(correoLimpio, estado.contrasena)
                SesionPreferencias(contexto).guardar(usuario.id)
                _uiState.update { it.copy(cargando = false, loginExitoso = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                when (e.codigo) {
                    403 -> _uiState.update {
                        it.copy(
                            cargando = false,
                            mostrarDialogoSuspendido = true
                        )
                    }

                    401 -> _uiState.update {
                        it.copy(
                            cargando = false,
                            errorContrasena = contexto.getString(R.string.login_error_credenciales_incorrectas)
                        )
                    }

                    else -> _uiState.update {
                        it.copy(cargando = false, errorContrasena = e.textoParaUsuario(contexto))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorContrasena = contexto.getString(R.string.error_inesperado)
                    )
                }
            }
        }
    }
}