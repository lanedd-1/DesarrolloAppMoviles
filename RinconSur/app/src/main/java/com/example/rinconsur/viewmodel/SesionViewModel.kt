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
import java.io.File
import java.io.FileOutputStream

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

                // La foto sigue siendo local hasta que migremos la subida al servidor
                val rutaFotoLocal = dbHelper.buscarPorId(idLogueado)?.fotoPerfilRuta
                var bitmapCargado: Bitmap? = null
                if (!rutaFotoLocal.isNullOrEmpty()) {
                    val archivo = File(rutaFotoLocal)
                    if (archivo.exists()) {
                        bitmapCargado = BitmapFactory.decodeFile(archivo.absolutePath)
                    }
                }

                _uiState.update {
                    it.copy(
                        usuarioActual = usuario,
                        totalMuestras = muestras,
                        sesionesCompletadas = stats.first,
                        sesionesTotales = stats.second,
                        listaSesiones = sesiones,
                        fotoPerfilBitmap = bitmapCargado
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
            val rutaGuardada = guardarBitmapEnDisco(contexto, bitmap, usuario.id)
            if (rutaGuardada != null) {
                val dbHelper = RinconDbHelper(contexto)
                dbHelper.actualizarFotoPerfil(usuario.id, rutaGuardada)

                _uiState.update {
                    it.copy(
                        fotoPerfilBitmap = bitmap,
                        mensajeExito = contexto.getString(R.string.mis_sesiones_foto_actualizada_exito)
                    )
                }
            }
        }
    }

    fun guardarFotoUri(contexto: Context, uri: Uri) {
        val usuario = _uiState.value.usuarioActual ?: _usuarioActual.value ?: return
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

    private fun guardarBitmapEnDisco(contexto: Context, bitmap: Bitmap, usuarioId: Long): String? {
        return try {
            val archivo = File(contexto.filesDir, "perfil_usuario_$usuarioId.jpg")
            FileOutputStream(archivo).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            archivo.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        _uiState.value = MisSesionesUiState()
    }
}