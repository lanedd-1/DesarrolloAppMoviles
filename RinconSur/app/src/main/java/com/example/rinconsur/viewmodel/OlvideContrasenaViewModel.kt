package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
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

    // Código generado al "enviar el SMS" (simulado)
    private var codigoGenerado: String = ""

    fun onCorreoCambia(valor: String) {
        // Si cambia el correo, el código anterior deja de valer
        _uiState.update {
            it.copy(
                correo = valor.trim().lowercase(),
                errorCorreo = null,
                codigoEnviado = false,
                codigoSms = "",
                errorCodigo = null,
                mensajeExito = null
            )
        }
    }

    fun onCodigoCambia(valor: String) {
        if (valor.all { it.isDigit() } && valor.length <= 6) {
            _uiState.update { it.copy(codigoSms = valor, errorCodigo = null) }
        }
    }

    fun onEnviarCodigo(contexto: Context): Boolean {
        val correo = _uiState.value.correo
        var error = when {
            correo.isBlank() -> "El correo es obligatorio"
            !correo.endsWith("@rinconsur.cl") -> "Debe ser un correo institucional (@RinconSur.cl)"
            else -> null
        }

        if (error == null && !RinconDbHelper(contexto).existeCorreo(correo)) {
            error = "Esta cuenta no está registrada"
        }

        if (error != null) {
            _uiState.update { it.copy(errorCorreo = error) }
            return false
        }

        codigoGenerado = (100000..999999).random().toString()

        _uiState.update {
            it.copy(
                codigoEnviado = true,
                mensajeExito = "Código SMS enviado (simulado). Tu código es: $codigoGenerado"
            )
        }
        return true
    }

    fun onVerificarCodigo(): Boolean {
        val codigo = _uiState.value.codigoSms
        if (codigo.length < 4) {
            _uiState.update { it.copy(errorCodigo = "Ingresa el código completo de 6 dígitos") }
            return false
        }
        if (codigo != codigoGenerado) {
            _uiState.update { it.copy(errorCodigo = "Código incorrecto") }
            return false
        }
        return true
    }

    fun onLimpiar() {
        codigoGenerado = ""
        _uiState.value = OlvideContrasenaUiState()
    }
}