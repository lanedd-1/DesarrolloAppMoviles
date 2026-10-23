package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.model.FormularioUiState
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false, mensajeExito = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim().lowercase(), errorCorreo = null, registroExitoso = false, mensajeExito = null) }
    }

    fun onRolSeleccionado(rol: String) {
        _uiState.update { it.copy(rolSeleccionado = rol, errorRol = null, registroExitoso = false, mensajeExito = null) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false, mensajeExito = null) }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmarContrasena = null, registroExitoso = false, mensajeExito = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    private fun rolIdDesdeNombre(nombre: String): Long? = when (nombre) {
        "Observador de Pasillo" -> Rol.ID_OBSERVADOR
        "Jefe de Tienda" -> Rol.ID_JEFE_TIENDA
        "Administrador" -> Rol.ID_ADMINISTRADOR
        else -> null
    }

    fun onRegistrar(contexto: Context): Usuario? {
        val estado = _uiState.value
        val correoLimpio = estado.correo.trim().lowercase()

        val errorNombre = if (estado.nombre.isBlank()) "El nombre completo es obligatorio" else null

        var errorCorreo = if (correoLimpio.isBlank()) {
            "El correo es obligatorio"
        } else if (!correoLimpio.contains("@")) {
            "Ingresa un correo válido"
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            "Debe ser un correo institucional (@rinconsur.cl)"
        } else {
            null
        }

        val rolId = rolIdDesdeNombre(estado.rolSeleccionado)
        val errorRol = if (rolId == null) "Debes seleccionar un rol para el colaborador" else null

        val patronContrasena = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-]).{8,}$".toRegex()

        val errorContrasena = if (estado.contrasena.isBlank()) {
            "La contraseña es obligatoria"
        } else if (!estado.contrasena.matches(patronContrasena)) {
            "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
        } else {
            null
        }

        val errorConfirmarContrasena = if (estado.confirmarContrasena.isBlank()) {
            "Confirma la contraseña"
        } else if (estado.confirmarContrasena != estado.contrasena) {
            "Las contraseñas no coinciden"
        } else {
            null
        }

        var hayErrores = listOf(
            errorNombre, errorCorreo, errorRol, errorContrasena, errorConfirmarContrasena
        ).any { it != null }

        var usuarioCreado: Usuario? = null

        if (!hayErrores && rolId != null) {
            val dbHelper = RinconDbHelper(contexto)

            if (dbHelper.existeCorreo(correoLimpio)) {
                errorCorreo = "Ya existe una cuenta con este correo"
                hayErrores = true
            } else {
                val nuevoId = dbHelper.crearUsuario(
                    nombre = estado.nombre,
                    correo = correoLimpio,
                    contrasenaPlana = estado.contrasena,
                    rolId = rolId
                )
                if (nuevoId == -1L) {
                    errorCorreo = "No se pudo crear la cuenta, intenta nuevamente"
                    hayErrores = true
                } else {
                    usuarioCreado = Usuario(
                        id = nuevoId,
                        nombre = estado.nombre.trim(),
                        correo = correoLimpio,
                        rolId = rolId,
                        rolNombre = estado.rolSeleccionado
                    )
                }
            }
        }

        val nombreIngresado = estado.nombre.trim()

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorRol = errorRol,
                errorContrasena = errorContrasena,
                errorConfirmarContrasena = errorConfirmarContrasena,
                registroExitoso = !hayErrores,
                mensajeExito = if (!hayErrores) "Cuenta creada para $nombreIngresado con éxito!" else null
            )
        }

        return if (hayErrores) null else usuarioCreado
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}