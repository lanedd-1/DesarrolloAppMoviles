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

    // Convierte el nombre del rol elegido en el ID que guarda la BD
    private fun rolIdDesdeNombre(nombre: String): Long? = when (nombre) {
        "Observador de Pasillo" -> Rol.ID_OBSERVADOR
        "Jefe de Tienda" -> Rol.ID_JEFE_TIENDA
        "Administrador" -> Rol.ID_ADMINISTRADOR
        else -> null
    }

    fun onRegistrar(contexto: Context): Usuario? {
        val estado = _uiState.value
        val correoLimpio = estado.correo.trim().lowercase()

        // 1. Nombre completo obligatorio
        val errorNombre = if (estado.nombre.isBlank()) "El nombre completo es obligatorio" else null

        // 2. Correo corporativo estricto
        var errorCorreo = if (correoLimpio.isBlank()) {
            "El correo es obligatorio"
        } else if (!correoLimpio.contains("@")) {
            "Ingresa un correo válido"
        } else if (!correoLimpio.endsWith("@rinconsur.cl")) {
            "Debe ser un correo institucional (@RinconSur.cl)"
        } else {
            null
        }

        // 3. Rol obligatorio
        val rolId = rolIdDesdeNombre(estado.rolSeleccionado)
        val errorRol = if (rolId == null) "Debes seleccionar un rol para el colaborador" else null

        // Contraseña: min 8 caracteres, 1 mayúscula, 1 minúscula, 1 carácter especial
        val patronContrasena = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-]).{8,}$".toRegex()

        // 4. Validación de contraseña
        val errorContrasena = if (estado.contrasena.isBlank()) {
            "La contraseña es obligatoria"
        } else if (!estado.contrasena.matches(patronContrasena)) {
            "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
        } else {
            null
        }

        // 5. Confirmación idéntica
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

        // 6. Si el formulario es válido, se guarda en la BD (el hash se calcula en crearUsuario)
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
                    // Se devuelve el usuario SIN la contraseña
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

        return if (hayErrores) null else usuarioCreado
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}