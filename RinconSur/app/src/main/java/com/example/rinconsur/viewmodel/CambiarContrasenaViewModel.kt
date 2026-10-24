package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.R
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
        _uiState.update { it.copy(contrasenaNueva = valor, errorContrasenaNueva = null) }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmarContrasena = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onGuardarContrasena(correoUsuario: String, dbHelper: RinconDbHelper, contexto: Context): Boolean {
        val estado = _uiState.value

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

        if (errorNueva == null && correoUsuario.isBlank()) {
            errorNueva = contexto.getString(R.string.cambiar_error_cuenta_no_identificada)
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