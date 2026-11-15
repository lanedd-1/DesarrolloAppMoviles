package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException

fun ApiException.textoParaUsuario(contexto: Context): String = when {
    codigo == 0 -> contexto.getString(R.string.error_sin_conexion)
    !mensajeServidor.isNullOrBlank() -> mensajeServidor
    else -> contexto.getString(R.string.error_servidor_generico, codigo)
}