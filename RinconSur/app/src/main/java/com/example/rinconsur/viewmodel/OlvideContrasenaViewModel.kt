package com.example.rinconsur.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.OlvideContrasenaUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class OlvideContrasenaViewModel(
    private val repositorio: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OlvideContrasenaUiState())
    val uiState: StateFlow<OlvideContrasenaUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, codigoVerificado = false) }
    }

    fun onCodigoCambia(valor: String) {
        if (valor.length <= 6 && valor.all { it.isDigit() }) {
            _uiState.update { it.copy(codigoCorreo = valor, errorCodigo = null, codigoVerificado = false) }
        }
    }

    fun onCerrarDialogoSuspendido() {
        _uiState.update { it.copy(mostrarDialogoSuspendido = false) }
    }

    fun onEnviarCodigo(contexto: Context) {
        val estado = _uiState.value
        if (estado.cargando) return

        val correo = estado.correo.trim().lowercase()

        val error = when {
            correo.isBlank() -> contexto.getString(R.string.olvide_error_correo_obligatorio)
            !correo.endsWith("@rinconsur.cl") -> contexto.getString(R.string.olvide_error_correo_institucional)
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorCorreo = error) }
            return
        }

        _uiState.update { it.copy(errorCorreo = null, cargando = true) }

        viewModelScope.launch {
            try {
                val respuesta = repositorio.solicitarCodigo(correo)
                respuesta.codigo?.let { enviarNotificacionLocal(contexto, it) }

                _uiState.update {
                    it.copy(
                        cargando = false,
                        codigoEnviado = true,
                        errorCorreo = null,
                        mensajeExito = contexto.getString(R.string.olvide_mensaje_codigo_enviado)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                when (e.codigo) {
                    403 -> _uiState.update { it.copy(cargando = false, mostrarDialogoSuspendido = true) }
                    404 -> _uiState.update {
                        it.copy(
                            cargando = false,
                            errorCorreo = contexto.getString(R.string.olvide_error_cuenta_no_registrada)
                        )
                    }
                    else -> _uiState.update {
                        it.copy(cargando = false, errorCorreo = e.textoParaUsuario(contexto))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorCorreo = contexto.getString(R.string.error_inesperado))
                }
            }
        }
    }

    fun onVerificarCodigo(contexto: Context) {
        val estado = _uiState.value
        if (estado.cargando) return

        val correo = estado.correo.trim().lowercase()
        val codigoIngresado = estado.codigoCorreo

        if (codigoIngresado.length < 6) {
            _uiState.update {
                it.copy(errorCodigo = contexto.getString(R.string.olvide_error_codigo_incompleto))
            }
            return
        }

        _uiState.update { it.copy(errorCodigo = null, cargando = true) }

        viewModelScope.launch {
            try {
                repositorio.verificarCodigo(correo, codigoIngresado)
                _uiState.update { it.copy(cargando = false, codigoVerificado = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                val mensaje = if (e.codigo in 400..499) {
                    contexto.getString(R.string.olvide_error_codigo_incorrecto)
                } else {
                    e.textoParaUsuario(contexto)
                }
                _uiState.update { it.copy(cargando = false, errorCodigo = mensaje) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorCodigo = contexto.getString(R.string.error_inesperado))
                }
            }
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