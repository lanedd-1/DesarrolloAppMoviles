package com.example.rinconsur.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CambiarContrasenaUiState(
    val contrasenaNueva: String = "",
    val confirmarContrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val errorContrasenaNueva: String? = null,
    val errorConfirmarContrasena: String? = null,
    val cambioExitoso: Boolean = false
)

class CambiarContrasenaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CambiarContrasenaUiState())
    val uiState: StateFlow<CambiarContrasenaUiState> = _uiState.asStateFlow()

    fun onContrasenaNuevaCambia(valor: String) {
        _uiState.update {
            it.copy(contrasenaNueva = valor, errorContrasenaNueva = null)
        }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(confirmarContrasena = valor, errorConfirmarContrasena = null)
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }


    fun onGuardarContrasena(correoUsuario: String, dbHelper: RinconDbHelper): Boolean {
        val estado = _uiState.value

        var errorNueva = when {
            estado.contrasenaNueva.isBlank() -> "La contraseña es obligatoria"
            estado.contrasenaNueva.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        var errorConfirmar = when {
            estado.confirmarContrasena.isBlank() -> "Confirma la contraseña"
            estado.contrasenaNueva != estado.confirmarContrasena -> "Las contraseñas no coinciden"
            else -> null
        }

        val hayErrores = errorNueva != null || errorConfirmar != null

        if (!hayErrores) {
            dbHelper.actualizarContrasena(correoUsuario, estado.contrasenaNueva)
        }

        _uiState.update {
            it.copy(
                errorContrasenaNueva = errorNueva,
                errorConfirmarContrasena = errorConfirmar,
                cambioExitoso = !hayErrores
            )
        }

        return !hayErrores
    }
}