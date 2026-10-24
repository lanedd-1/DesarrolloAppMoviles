package com.example.rinconsur.model

import android.graphics.Bitmap
import com.example.rinconsur.data.SesionItem

data class MisSesionesUiState(
    val usuarioActual: Usuario? = null,
    val listaSesiones: List<SesionItem> = emptyList(),
    val totalMuestras: Int = 0,
    val sesionesCompletadas: Int = 0,
    val sesionesTotales: Int = 0,
    val fotoPerfilBitmap: Bitmap? = null,
    val mensajeExito: String? = null
)