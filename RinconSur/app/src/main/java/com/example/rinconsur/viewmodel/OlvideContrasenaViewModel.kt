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

data class OlvideContrasenaUiState(
    val correo: String = "",
    val codigoCorreo: String = "",
    val codigoEnviado: Boolean = false,
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val mensajeExito: String? = null,
    val mostrarDialogoSuspendido: Boolean = false
)

class OlvideContrasenaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OlvideContrasenaUiState())
    val uiState: StateFlow<OlvideContrasenaUiState> = _uiState.asStateFlow()

    private var codigoGenerado: String = ""

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor.trim().lowercase(),
                errorCorreo = null,
                codigoEnviado = false,
                codigoCorreo = "",
                errorCodigo = null,
                mensajeExito = null
            )
        }
    }

    fun onCodigoCambia(valor: String) {
        if (valor.all { it.isDigit() } && valor.length <= 6) {
            _uiState.update { it.copy(codigoCorreo = valor, errorCodigo = null) }
        }
    }

    fun onCerrarDialogoSuspendido() {
        _uiState.update { it.copy(mostrarDialogoSuspendido = false) }
    }

    fun onEnviarCodigo(contexto: Context): Boolean {
        val correo = _uiState.value.correo
        var error = when {
            correo.isBlank() -> contexto.getString(R.string.olvide_error_correo_obligatorio)
            !correo.endsWith("@rinconsur.cl") -> contexto.getString(R.string.olvide_error_correo_institucional)
            else -> null
        }

        if (error == null) {
            val dbHelper = RinconDbHelper(contexto)

            if (!dbHelper.existeCorreo(correo)) {
                error = contexto.getString(R.string.olvide_error_cuenta_no_registrada)
            } else if (!dbHelper.estaCuentaActiva(correo)) {
                // NUEVA CAPA DE SEGURIDAD: Si está suspendido, mostramos popup y evitamos el envío
                _uiState.update { it.copy(mostrarDialogoSuspendido = true) }
                return false
            }
        }

        if (error != null) {
            _uiState.update { it.copy(errorCorreo = error) }
            return false
        }

        codigoGenerado = (100000..999999).random().toString()

        mostrarNotificacionCodigo(contexto, codigoGenerado)

        _uiState.update {
            it.copy(
                codigoEnviado = true,
                mensajeExito = contexto.getString(R.string.olvide_mensaje_codigo_enviado)
            )
        }
        return true
    }

    private fun mostrarNotificacionCodigo(contexto: Context, codigo: String) {
        val channelId = "canal_recuperacion_rincon"
        val notificationManager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                contexto.getString(R.string.olvide_notificacion_canal_nombre),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = contexto.getString(R.string.olvide_notificacion_canal_descripcion)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificacion = NotificationCompat.Builder(contexto, channelId)
            .setSmallIcon(R.drawable.ic_correo)
            .setContentTitle(contexto.getString(R.string.olvide_notificacion_titulo))
            .setContentText(contexto.getString(R.string.olvide_notificacion_texto_corto, codigo))
            .setStyle(NotificationCompat.BigTextStyle().bigText(contexto.getString(R.string.olvide_notificacion_texto_largo, codigo)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notificacion)
    }

    fun onVerificarCodigo(contexto: Context): Boolean {
        val codigo = _uiState.value.codigoCorreo
        if (codigo.length < 6) {
            _uiState.update { it.copy(errorCodigo = contexto.getString(R.string.olvide_error_codigo_incompleto)) }
            return false
        }
        if (codigo != codigoGenerado) {
            _uiState.update { it.copy(errorCodigo = contexto.getString(R.string.olvide_error_codigo_incorrecto)) }
            return false
        }
        return true
    }

    fun onLimpiar() {
        codigoGenerado = ""
        _uiState.value = OlvideContrasenaUiState()
    }
}