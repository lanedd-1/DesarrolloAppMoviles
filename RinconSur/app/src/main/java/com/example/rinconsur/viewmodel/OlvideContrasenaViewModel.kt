package com.example.rinconsur.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class OlvideContrasenaUiState(
    val correo: String = "",
    val codigoCorreo: String = "", // Tu variable original
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val codigoEnviado: Boolean = false,
    val mensajeExito: String? = null, // Tu variable original
    val mostrarDialogoSuspendido: Boolean = false // Tu variable original
)

class OlvideContrasenaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OlvideContrasenaUiState())
    val uiState: StateFlow<OlvideContrasenaUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun onCodigoCambia(valor: String) {
        if (valor.length <= 6 && valor.all { it.isDigit() }) {
            _uiState.update { it.copy(codigoCorreo = valor, errorCodigo = null) }
        }
    }

    fun onCerrarDialogoSuspendido() {
        _uiState.update { it.copy(mostrarDialogoSuspendido = false) }
    }

    fun onEnviarCodigo(contexto: Context, dbHelper: RinconDbHelper) {
        val correo = _uiState.value.correo.trim().lowercase()

        val error = when {
            correo.isBlank() -> contexto.getString(R.string.olvide_error_correo_obligatorio)
            !correo.endsWith("@rinconsur.cl") -> contexto.getString(R.string.olvide_error_correo_institucional)
            !dbHelper.existeCorreo(correo) -> contexto.getString(R.string.olvide_error_cuenta_no_registrada)
            !dbHelper.estaCuentaActiva(correo) -> {
                _uiState.update { it.copy(mostrarDialogoSuspendido = true) }
                return
            }
            else -> null
        }

        if (error == null) {
            // 1. Generar token y guardarlo en DB
            val codigoGenerado = Random.nextInt(100000, 999999).toString()
            val guardadoExitoso = dbHelper.guardarTokenRecuperacion(correo, codigoGenerado)

            if (guardadoExitoso) {
                // 2. Enviar notificación
                enviarNotificacionLocal(contexto, codigoGenerado)

                _uiState.update {
                    it.copy(
                        codigoEnviado = true,
                        errorCorreo = null,
                        mensajeExito = contexto.getString(R.string.olvide_mensaje_codigo_enviado)
                    )
                }
            } else {
                _uiState.update { it.copy(errorCorreo = "Error interno al guardar token.") }
            }
        } else {
            _uiState.update { it.copy(errorCorreo = error) }
        }
    }

    fun onVerificarCodigo(contexto: Context, dbHelper: RinconDbHelper): Boolean {
        val estadoActual = _uiState.value
        val correo = estadoActual.correo.trim().lowercase()
        val codigoIngresado = estadoActual.codigoCorreo

        val error = when {
            codigoIngresado.length < 6 -> contexto.getString(R.string.olvide_error_codigo_incompleto)
            !dbHelper.validarTokenRecuperacion(correo, codigoIngresado) -> contexto.getString(R.string.olvide_error_codigo_incorrecto)
            else -> null
        }

        if (error == null) {
            dbHelper.eliminarTokenRecuperacion(correo)

            _uiState.update { it.copy(errorCodigo = null) }
            return true
        } else {
            _uiState.update { it.copy(errorCodigo = error) }
            return false
        }
    }

    private fun enviarNotificacionLocal(contexto: Context, codigo: String) {
        val canalId = "canal_recuperacion"
        val manager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canalNombre = contexto.getString(R.string.olvide_notificacion_canal_nombre)
            val canalDesc = contexto.getString(R.string.olvide_notificacion_canal_descripcion)
            val canal = NotificationChannel(canalId, canalNombre, NotificationManager.IMPORTANCE_HIGH).apply {
                description = canalDesc
            }
            manager.createNotificationChannel(canal)
        }

        val notificacion = NotificationCompat.Builder(contexto, canalId)
            .setSmallIcon(R.drawable.ic_correo)
            .setContentTitle(contexto.getString(R.string.olvide_notificacion_titulo))
            .setContentText(contexto.getString(R.string.olvide_notificacion_texto_corto, codigo))
            .setStyle(NotificationCompat.BigTextStyle().bigText(contexto.getString(R.string.olvide_notificacion_texto_largo, codigo)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(Random.nextInt(), notificacion)
    }
}