package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.R
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

    fun inicializarRolesSegunUsuario(usuarioActual: Usuario?, contexto: Context) {
        val obs = contexto.getString(R.string.rol_observador)
        val jefe = contexto.getString(R.string.rol_jefe_tienda)
        val admin = contexto.getString(R.string.rol_administrador)

        val roles = if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR) {
            listOf(obs, jefe, admin)
        } else {
            listOf(obs, jefe)
        }

        _uiState.update { estado ->
            val rolValido = if (estado.rolSeleccionado in roles) estado.rolSeleccionado else roles.first()
            estado.copy(
                rolesDisponibles = roles,
                rolSeleccionado = rolValido
            )
        }
    }

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

    private fun rolIdDesdeNombre(nombre: String, contexto: Context): Long? {
        val obs = contexto.getString(R.string.rol_observador)
        val jefe = contexto.getString(R.string.rol_jefe_tienda)
        val admin = contexto.getString(R.string.rol_administrador)

        return when (nombre) {
            obs -> Rol.ID_OBSERVADOR
            jefe -> Rol.ID_JEFE_TIENDA
            admin -> Rol.ID_ADMINISTRADOR
            else -> null
        }
    }

    fun onRegistrar(contexto: Context): Usuario? {
        val estado = _uiState.value
        val correoLimpio = estado.correo.trim().lowercase()

        val errorNombre = if (estado.nombre.isBlank()) contexto.getString(R.string.formulario_error_nombre_obligatorio) else null

        var errorCorreo = if (correoLimpio.isBlank()) {
            contexto.getString(R.string.formulario_error_correo_obligatorio)
        } else if (!correoLimpio.contains("@")) {
            contexto.getString(R.string.formulario_error_correo_invalido)
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            contexto.getString(R.string.formulario_error_correo_institucional)
        } else {
            null
        }

        val rolId = rolIdDesdeNombre(estado.rolSeleccionado, contexto)
        val errorRol = if (rolId == null) contexto.getString(R.string.formulario_error_rol_obligatorio) else null

        val patronContrasena = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-]).{8,}$".toRegex()

        val errorContrasena = if (estado.contrasena.isBlank()) {
            contexto.getString(R.string.formulario_error_contrasena_obligatoria)
        } else if (!estado.contrasena.matches(patronContrasena)) {
            contexto.getString(R.string.formulario_error_contrasena_formato)
        } else {
            null
        }

        val errorConfirmarContrasena = if (estado.confirmarContrasena.isBlank()) {
            contexto.getString(R.string.formulario_error_confirmar_obligatoria)
        } else if (estado.confirmarContrasena != estado.contrasena) {
            contexto.getString(R.string.formulario_error_no_coinciden)
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
                errorCorreo = contexto.getString(R.string.formulario_error_correo_existente)
                hayErrores = true
            } else {
                val nuevoId = dbHelper.crearUsuario(
                    nombre = estado.nombre,
                    correo = correoLimpio,
                    contrasenaPlana = estado.contrasena,
                    rolId = rolId
                )
                if (nuevoId == -1L) {
                    errorCorreo = contexto.getString(R.string.formulario_error_creacion_fallida)
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
                mensajeExito = if (!hayErrores) contexto.getString(R.string.formulario_mensaje_exito, nombreIngresado) else null
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