package com.example.rinconsur.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
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

    // Recibe la Base de Datos como parámetro
    fun onIngresar(dbHelper: RinconDbHelper): Boolean {
        val estado = _uiState.value

        // 1. Validaciones básicas de que no vengan campos en blanco
        var errorCorreo = if (estado.correo.isBlank()) "El correo es obligatorio" else null
        var errorContrasena = if (estado.contrasena.isBlank()) "La contraseña es obligatoria" else null

        var hayErrores = errorCorreo != null || errorContrasena != null

        // 2. Si escribieron algo, consultamos la Base de Datos SQLite
        if (!hayErrores) {
            val usuarioEnBd = dbHelper.buscarPorCorreo(estado.correo)

            if (usuarioEnBd == null) {
                // El correo no existe en la BD
                errorCorreo = "Esta cuenta no está registrada"
                hayErrores = true
            } else if (usuarioEnBd.contrasena != estado.contrasena) {
                // El correo existe, pero la clave está mal
                errorContrasena = "Contraseña incorrecta"
                hayErrores = true
            }
        }

        // 3. Actualizamos la pantalla con los errores (o sin ellos si entró bien)
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