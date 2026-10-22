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
    val mensajeExito: String? = null
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


        mostrarNotificacionCodigo(contexto, codigoGenerado)

        _uiState.update {
            it.copy(
                codigoEnviado = true,
                mensajeExito = "¡Revisa la notificación en tu dispositivo! Código enviado."
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
                "Recuperación de Contraseña",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para enviar el código de verificación de Rincón Sur"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificacion = NotificationCompat.Builder(contexto, channelId)
            .setSmallIcon(R.drawable.ic_correo) // Asegúrate de tener este ícono en res/drawable
            .setContentTitle("Rincón Sur - Código de Verificación")
            .setContentText("Tu código de recuperación es: $codigo")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Tu código institucional es: $codigo. Ingrésalo en la aplicación para continuar."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notificacion)
    }

    fun onVerificarCodigo(): Boolean {
        val codigo = _uiState.value.codigoCorreo
        if (codigo.length < 6) {
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