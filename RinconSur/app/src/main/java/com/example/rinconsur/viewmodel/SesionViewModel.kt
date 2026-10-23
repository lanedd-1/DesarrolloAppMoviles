package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionViewModel : ViewModel() {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // Lee las preferencias y busca al usuario en SQLite
    fun cargarUsuario(contexto: Context) {
        val idUsuario = SesionPreferencias(contexto).obtenerIdUsuario()
        if (idUsuario != -1L) {
            val dbHelper = RinconDbHelper(contexto)
            _usuarioActual.value = dbHelper.buscarPorId(idUsuario)
        } else {
            _usuarioActual.value = null
        }
    }

    // Limpia la variable global al cerrar sesión
    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}