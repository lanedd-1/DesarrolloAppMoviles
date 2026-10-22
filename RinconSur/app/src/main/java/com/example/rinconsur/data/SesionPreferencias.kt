package com.example.rinconsur.data

import android.content.Context
import android.content.SharedPreferences

class SesionPreferencias(contexto: Context) {

    // Archivo interno del celular donde se guarda la sesión
    private val preferencias: SharedPreferences =
        contexto.getSharedPreferences("sesion_rincon_sur", Context.MODE_PRIVATE)

    // Guarda el ID del usuario al pasar por el LoginScreen
    fun guardar(idUsuario: Long) {
        preferencias.edit().putLong("id_usuario_logueado", idUsuario).apply()
    }

    // Lee el ID del usuario al abrir MisSesionesScreen (Devuelve -1 si no hay sesión)
    fun obtenerIdUsuario(): Long {
        return preferencias.getLong("id_usuario_logueado", -1L)
    }

    // Borra la sesión cuando el usuario toca "Cerrar sesión" en el menú
    fun limpiar() {
        preferencias.edit().clear().apply()
    }
}