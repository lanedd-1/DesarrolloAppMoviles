package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, loginExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, loginExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    // NUEVO: Función para cerrar el popup
    fun onCerrarDialogoSuspendido() {
        _uiState.update { it.copy(mostrarDialogoSuspendido = false) }
    }

    fun onIngresar(contexto: Context): Boolean {
        val estado = _uiState.value
        val correoLimpio = estado.correo.trim().lowercase()

        var errorCorreo = if (correoLimpio.isBlank()) {
            contexto.getString(R.string.login_error_correo_obligatorio)
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            contexto.getString(R.string.login_error_correo_institucional)
        } else { null }

        var errorContrasena = if (estado.contrasena.isBlank()) {
            contexto.getString(R.string.login_error_contrasena_obligatoria)
        } else if (estado.contrasena.length < 6) {
            contexto.getString(R.string.login_error_contrasena_corta)
        } else { null }

        var hayErrores = errorCorreo != null || errorContrasena != null

        if (!hayErrores) {
            val dbHelper = RinconDbHelper(contexto)
            val credencialesCorrectas = dbHelper.validarLogin(correoLimpio, estado.contrasena)

            if (!credencialesCorrectas) {
                errorContrasena = contexto.getString(R.string.login_error_credenciales_incorrectas)
                hayErrores = true
            } else {
                val cuentaActiva = dbHelper.estaCuentaActiva(correoLimpio)

                if (cuentaActiva) {
                    val usuario = dbHelper.buscarPorCorreo(correoLimpio)
                    if (usuario != null) {
                        SesionPreferencias(contexto).guardar(usuario.id)
                    }
                } else {
                    _uiState.update { it.copy(mostrarDialogoSuspendido = true) }
                    return false
                }
            }
        }

        _uiState.update {
            it.copy(
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                loginExitoso = !hayErrores
            )
        }

        return !hayErrores
    }

    fun onLimpiar() {
        _uiState.value = LoginUiState()
    }
}