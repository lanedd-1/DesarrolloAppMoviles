package com.example.rinconsur.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rinconsur.model.FormularioUiState
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registroExitoso = false) }
    }

    fun onRolSeleccionado(rol: String) {
        _uiState.update { it.copy(rolSeleccionado = rol, errorRol = null, registroExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmarContrasena = null, registroExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onRegistrar(): Usuario? {
        val estado = _uiState.value
        val correoLimpio = estado.correo.trim().lowercase()

        // 1. Nombre completo obligatorio
        val errorNombre = if (estado.nombre.isBlank()) "El nombre completo es obligatorio" else null

        // 2. Correo corporativo estricto (@RinconSur.cl insensible a mayúsculas/minúsculas)
        val errorCorreo = if (correoLimpio.isBlank()) {
            "El correo es obligatorio"
        } else if (!correoLimpio.contains("@")) {
            "Ingresa un correo válido"
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            "Debe ser un correo institucional (@RinconSur.cl)"
        } else {
            null
        }

        // 3. Selección obligatoria de Rol/Cargo
        val errorRol = if (estado.rolSeleccionado.isBlank()) "Debes seleccionar un rol para el colaborador" else null

        // Regex para Contraseña: min 8 caracteres, 1 mayúscula, 1 minúscula, 1 carácter especial
        val patronContrasena = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-]).{8,}$".toRegex()

        // 4. Validación de contraseña con los nuevos requisitos
        val errorContrasena = if (estado.contrasena.isBlank()) {
            "La contraseña es obligatoria"
        } else if (!estado.contrasena.matches(patronContrasena)) {
            "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
        } else {
            null
        }

        // 5. Confirmación de contraseña idéntica
        val errorConfirmarContrasena = if (estado.confirmarContrasena.isBlank()) {
            "Confirma la contraseña"
        } else if (estado.confirmarContrasena != estado.contrasena) {
            "Las contraseñas no coinciden"
        } else {
            null
        }

        // Evaluación de errores al estilo exacto de tu profesor
        val hayErrores = listOf(
            errorNombre,
            errorCorreo,
            errorRol,
            errorContrasena,
            errorConfirmarContrasena
        ).any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorRol = errorRol,
                errorContrasena = errorContrasena,
                errorConfirmarContrasena = errorConfirmarContrasena,
                registroExitoso = !hayErrores
            )
        }

        return if (hayErrores) null else Usuario(
            nombre = estado.nombre,
            correo = correoLimpio,
            contrasena = estado.contrasena,
            rolNombre = estado.rolSeleccionado
        )
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}