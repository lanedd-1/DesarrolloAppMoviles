package com.example.rinconsur.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class OlvideContrasenaUiState(
    val correo: String = "",
    val codigoSms: String = "",
    val codigoEnviado: Boolean = false,
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val mensajeExito: String? = null
)

class OlvideContrasenaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OlvideContrasenaUiState())
    val uiState: StateFlow<OlvideContrasenaUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim().lowercase(), errorCorreo = null) }
    }

    fun onCodigoCambia(valor: String) {
        if (valor.all { it.isDigit() } && valor.length <= 6) {
            _uiState.update { it.copy(codigoSms = valor, errorCodigo = null) }
        }
    }


    fun onEnviarCodigo(): Boolean {
        val correo = _uiState.value.correo
        val error = when {
            correo.isBlank() -> "El correo es obligatorio"
            !correo.endsWith("@rinconsur.cl") -> "Debe ser un correo institucional (@RinconSur.cl)"
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorCorreo = error) }
            return false
        }


        _uiState.update {
            it.copy(
                codigoEnviado = true,
                mensajeExito = "¡Código SMS enviado a tu teléfono asociado!"
            )
        }
        return true
    }


    fun onVerificarCodigo(): Boolean {
        val codigo = _uiState.value.codigoSms
        if (codigo.length < 4) {
            _uiState.update { it.copy(errorCodigo = "Ingresa el código completo de 4 o 6 dígitos") }
            return false
        }
        return true
    }

    fun onLimpiar() {
        _uiState.value = OlvideContrasenaUiState()
    }
}