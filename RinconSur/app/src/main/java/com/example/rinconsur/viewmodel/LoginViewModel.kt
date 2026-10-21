package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.RinconDbHelper // Revisa que este import coincida con la ubicación de tu DbHelper
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

        // 1. Transformamos el correo a minúsculas para que no importe cómo lo escriban
        val correoLimpio = estado.correo.trim().lowercase()

        // 2. Validaciones básicas de formato usando el correo limpio
        val errorCorreo = if (correoLimpio.isBlank()) {
            "El correo es obligatorio"
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            "Debe ingresar su correo corporativo (@RinconSur.cl)"
        } else {
            null
        }

        val errorContrasena = if (estado.contrasena.isBlank()) {
            "La contraseña es obligatoria"
        } else if (estado.contrasena.length < 6) {
            "Mínimo 6 caracteres"
        } else {
            null
        }

        val hayErroresFormato = errorCorreo != null || errorContrasena != null

        // Si hay errores de formato, avisa a la pantalla y corta la ejecución
        if (hayErroresFormato) {
            _uiState.update {
                it.copy(
                    errorCorreo = errorCorreo,
                    errorContrasena = errorContrasena,
                    loginExitoso = false
                )
            }
            return false
        }

        // 3. Validación Real contra tu SQLite
        val dbHelper = RinconDbHelper(contexto)

        // Usamos el correo en minúsculas para que haga "match" exacto con tu SQLite
        val credencialesCorrectas = dbHelper.validarLogin(correoLimpio, estado.contrasena)

        return if (credencialesCorrectas) {
            // ¡Todo correcto! El usuario existe en la BD
            _uiState.update { it.copy(loginExitoso = true) }
            true
        } else {
            // Error: No existe en SQLite o la clave es incorrecta
            _uiState.update {
                it.copy(
                    errorContrasena = "Correo o contraseña incorrectos",
                    loginExitoso = false
                )
            }
            false
        }
    }

    fun onLimpiar() {
        _uiState.value = LoginUiState()
    }
}