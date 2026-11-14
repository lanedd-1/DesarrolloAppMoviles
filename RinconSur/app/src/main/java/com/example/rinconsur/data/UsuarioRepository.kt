package com.example.rinconsur.data

import com.example.rinconsur.data.remote.ActualizarPerfilRequest
import com.example.rinconsur.data.remote.CambiarContrasenaRequest
import com.example.rinconsur.data.remote.CambiarEstadoRequest
import com.example.rinconsur.data.remote.CambiarRolRequest
import com.example.rinconsur.data.remote.CodigoResponse
import com.example.rinconsur.data.remote.CrearUsuarioRequest
import com.example.rinconsur.data.remote.LoginRequest
import com.example.rinconsur.data.remote.RestablecerContrasenaRequest
import com.example.rinconsur.data.remote.RetrofitClient
import com.example.rinconsur.data.remote.SolicitarCodigoRequest
import com.example.rinconsur.data.remote.UsuarioApi
import com.example.rinconsur.data.remote.VerificarCodigoRequest
import com.example.rinconsur.data.remote.aUsuario
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

class ApiException(val codigo: Int, mensaje: String) : Exception(mensaje)

class UsuarioRepository(
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario =
        llamar { usuarioApi.iniciarSesion(LoginRequest(correo, contrasena)) }.aUsuario()

    suspend fun registrar(nombre: String, correo: String, contrasena: String, rolId: Long): Usuario =
        llamar { usuarioApi.crear(CrearUsuarioRequest(nombre, correo, contrasena, rolId)) }.aUsuario()

    suspend fun listar(): List<Usuario> =
        llamar { usuarioApi.listar() }.map { it.aUsuario() }

    suspend fun obtener(id: Long): Usuario =
        llamar { usuarioApi.obtener(id) }.aUsuario()

    suspend fun actualizarPerfil(id: Long, nombre: String, correo: String): Usuario =
        llamar { usuarioApi.actualizarPerfil(id, ActualizarPerfilRequest(nombre, correo)) }.aUsuario()

    suspend fun cambiarEstado(id: Long, activo: Boolean): Usuario =
        llamar { usuarioApi.cambiarEstado(id, CambiarEstadoRequest(activo)) }.aUsuario()

    suspend fun cambiarRol(id: Long, rolId: Long): Usuario =
        llamar { usuarioApi.cambiarRol(id, CambiarRolRequest(rolId)) }.aUsuario()

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String) =
        llamar { usuarioApi.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)) }

    suspend fun solicitarCodigo(correo: String): CodigoResponse =
        llamar { usuarioApi.solicitarCodigo(SolicitarCodigoRequest(correo)) }

    suspend fun verificarCodigo(correo: String, codigo: String) =
        llamar { usuarioApi.verificarCodigo(VerificarCodigoRequest(correo, codigo)) }

    suspend fun restablecerContrasena(correo: String, codigo: String, nueva: String) =
        llamar { usuarioApi.restablecerContrasena(RestablecerContrasenaRequest(correo, codigo, nueva)) }

    suspend fun listarRoles(): List<Rol> =
        llamar { usuarioApi.listarRoles() }.map { Rol(id = it.id, nombre = it.nombre) }

    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw ApiException(e.code(), mensajeDelServidor(e))
    } catch (e: IOException) {
        throw ApiException(0, "No se pudo conectar con el servidor")
    }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        val mensaje = try {
            JsonParser.parseString(cuerpo ?: "").asJsonObject.get("message")?.asString ?: ""
        } catch (ex: Exception) {
            ""
        }
        return mensaje.ifBlank { "Error del servidor (${e.code()})" }
    }
}