package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
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

    fun onIngresar(contexto: Context): Boolean {
        val estado = _uiState.value

        val correoLimpio = estado.correo.trim().lowercase()

        var errorCorreo = if (correoLimpio.isBlank()) {
            "El correo es obligatorio"
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            "Debe ingresar su correo corporativo (@RinconSur.cl)"
        } else {
            null
        }

        var errorContrasena = if (estado.contrasena.isBlank()) {
            "La contraseña es obligatoria"
        } else if (estado.contrasena.length < 6) {
            "Mínimo 6 caracteres"
        } else {
            null
        }

        var hayErrores = errorCorreo != null || errorContrasena != null

        // Si el formato es válido, validamos contra la BD usando BCrypt
        if (!hayErrores) {
            val dbHelper = RinconDbHelper(contexto)

            val credencialesCorrectas = dbHelper.validarLogin(correoLimpio, estado.contrasena)

            if (!credencialesCorrectas) {
                errorContrasena = "Correo o contraseña incorrectos"
                hayErrores = true
            } else {
                // Login exitoso: guardamos el ID del usuario en SharedPreferences
                val usuario = dbHelper.buscarPorCorreo(correoLimpio)
                if (usuario != null) {
                    SesionPreferencias(contexto).guardar(usuario.id)
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