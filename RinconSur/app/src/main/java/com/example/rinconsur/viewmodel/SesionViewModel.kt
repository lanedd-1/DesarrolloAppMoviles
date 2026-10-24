package com.example.rinconsur.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.MisSesionesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SesionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MisSesionesUiState())
    val uiState: StateFlow<MisSesionesUiState> = _uiState.asStateFlow()

    private val _usuarioActual = MutableStateFlow<com.example.rinconsur.model.Usuario?>(null)
    val usuarioActual: StateFlow<com.example.rinconsur.model.Usuario?> = _usuarioActual.asStateFlow()

    fun cargarUsuario(contexto: Context) {
        val idUsuario = SesionPreferencias(contexto).obtenerIdUsuario()
        if (idUsuario != -1L) {
            val dbHelper = RinconDbHelper(contexto)
            _usuarioActual.value = dbHelper.buscarPorId(idUsuario)
        } else {
            _usuarioActual.value = null
        }
    }

    fun cargarDatos(contexto: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val idLogueado = SesionPreferencias(contexto).obtenerIdUsuario()
            if (idLogueado != -1L) {
                val dbHelper = RinconDbHelper(contexto)
                val usuario = dbHelper.buscarPorId(idLogueado)
                val muestras = dbHelper.obtenerMuestrasRegistradasUsuario(idLogueado)
                val stats = dbHelper.obtenerEstadisticasSesiones(idLogueado)
                val sesiones = dbHelper.obtenerSesionesPorUsuario(idLogueado)

                _usuarioActual.value = usuario

                var bitmapCargado: Bitmap? = null
                if (!usuario?.fotoPerfilRuta.isNullOrEmpty()) {
                    val archivo = File(usuario!!.fotoPerfilRuta!!)
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
                        mensajeExito = "¡Foto actualizada con éxito!"
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