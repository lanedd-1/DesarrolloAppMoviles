package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.RinconDbHelper // Ojo con la ruta de tu DbHelper
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

        // Pasamos el correo a minúsculas para que no importe cómo lo escriba el usuario
        val correoLimpio = estado.correo.trim().lowercase()

        // 1. Usamos 'var' para poder sobreescribir el error si la BD lo rechaza
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

        // 2. Si escribieron algo válido, consultamos la Base de Datos SQLite
        if (!hayErrores) {
            val dbHelper = RinconDbHelper(contexto)
            val usuarioEnBd = dbHelper.buscarPorCorreo(correoLimpio)

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

        // 3. Actualizamos la vista con los errores finales (o éxito)
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