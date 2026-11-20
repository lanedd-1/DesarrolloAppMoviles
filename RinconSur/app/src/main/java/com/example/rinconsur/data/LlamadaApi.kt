package com.example.rinconsur.data

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> llamarApi(peticion: suspend () -> T): T = try {
    peticion()
} catch (e: HttpException) {
    throw ApiException(e.code(), mensajeDelServidor(e))
} catch (e: IOException) {
    throw ApiException(0, "")
}

private fun mensajeDelServidor(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    return try {
        JsonParser.parseString(cuerpo ?: "").asJsonObject.get("message")?.asString ?: ""
    } catch (ex: Exception) {
        ""
    }
}