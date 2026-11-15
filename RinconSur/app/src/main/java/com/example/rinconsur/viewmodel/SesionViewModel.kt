package com.example.rinconsur.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.MisSesionesUiState
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class SesionViewModel(
    private val repositorio: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisSesionesUiState())
    val uiState: StateFlow<MisSesionesUiState> = _uiState.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun cargarUsuario(contexto: Context) {
        val idUsuario = SesionPreferencias(contexto).obtenerIdUsuario()
        if (idUsuario == -1L) {
            _usuarioActual.value = null
            return
        }

        viewModelScope.launch {
            try {
                _usuarioActual.value = repositorio.obtener(idUsuario)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                // 404: el usuario ya no existe en el servidor
                if (e.codigo == 404) _usuarioActual.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cargarDatos(contexto: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val idLogueado = SesionPreferencias(contexto).obtenerIdUsuario()
            if (idLogueado != -1L) {
                val dbHelper = RinconDbHelper(contexto)

                // El usuario viene del servidor; si falla, se usa el que ya estaba cargado
                val usuario: Usuario? = try {
                    repositorio.obtener(idLogueado)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _usuarioActual.value
                }

                val muestras = dbHelper.obtenerMuestrasRegistradasUsuario(idLogueado)
                val stats = dbHelper.obtenerEstadisticasSesiones(idLogueado)
                val sesiones = dbHelper.obtenerSesionesPorUsuario(idLogueado)

                _usuarioActual.value = usuario

                // La foto viene del servidor; si no tiene (404) o no hay conexion, queda null
                val bitmapCargado: Bitmap? = try {
                    val bytes = repositorio.obtenerFoto(idLogueado)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }

                _uiState.update {
                    it.copy(
                        usuarioActual = usuario,
                        totalMuestras = muestras,
                        sesionesCompletadas = stats.first,
                        sesionesTotales = stats.second,
                        listaSesiones = sesiones,
                        fotoPerfilBitmap = bitmapCargado ?: it.fotoPerfilBitmap
                    )
                }
            }
        }
    }


    fun asignarAccesoAObservador(contexto: Context, observadorId: Long, accesoSeleccionado: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbHelper = RinconDbHelper(contexto)

            if (dbHelper.tieneSesionActiva(observadorId)) {
                _uiState.update {
                    it.copy(mensajeExito = "Este observador ya tiene un acceso activo asignado.")
                }
                return@launch
            }

            val cerrados = dbHelper.obtenerAccesosCerrados()

            if (!cerrados.contains(accesoSeleccionado)) {
                val codigoAleatorio = "SES-${(100..999).random()}"
                val fechaHoy = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())

                dbHelper.crearSesion(
                    codigo = codigoAleatorio,
                    acceso = accesoSeleccionado,
                    fecha = fechaHoy,
                    usuarioId = observadorId
                )
                cargarDatos(contexto)
            }
        }
    }

    fun guardarFotoBitmap(contexto: Context, bitmap: Bitmap) {
        val usuario = _uiState.value.usuarioActual ?: _usuarioActual.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Se reduce para no pasar el limite de tamaño del servidor
                val reducido = reducirBitmap(bitmap, 1024)
                val salida = ByteArrayOutputStream()
                reducido.compress(Bitmap.CompressFormat.JPEG, 85, salida)

                repositorio.subirFoto(usuario.id, salida.toByteArray())

                _uiState.update {
                    it.copy(
                        fotoPerfilBitmap = reducido,
                        mensajeExito = contexto.getString(R.string.mis_sesiones_foto_actualizada_exito)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _uiState.update { it.copy(mensajeExito = e.textoParaUsuario(contexto)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeExito = contexto.getString(R.string.error_inesperado)) }
            }
        }
    }

    fun guardarFotoUri(contexto: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contexto.contentResolver.openInputStream(uri)
                val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmapOriginal != null) {
                    guardarFotoBitmap(contexto, bitmapOriginal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun reducirBitmap(bitmap: Bitmap, maximo: Int): Bitmap {
        val mayor = maxOf(bitmap.width, bitmap.height)
        if (mayor <= maximo) return bitmap
        val escala = maximo.toFloat() / mayor
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt(),
            (bitmap.height * escala).toInt(),
            true
        )
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        _uiState.value = MisSesionesUiState()
    }
}