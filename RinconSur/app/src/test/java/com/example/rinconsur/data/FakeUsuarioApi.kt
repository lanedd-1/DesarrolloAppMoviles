package com.example.rinconsur.data

import com.example.rinconsur.data.remote.ActualizarPerfilRequest
import com.example.rinconsur.data.remote.CambiarContrasenaRequest
import com.example.rinconsur.data.remote.CambiarEstadoRequest
import com.example.rinconsur.data.remote.CambiarRolRequest
import com.example.rinconsur.data.remote.CodigoResponse
import com.example.rinconsur.data.remote.CrearUsuarioRequest
import com.example.rinconsur.data.remote.LoginRequest
import com.example.rinconsur.data.remote.RestablecerContrasenaRequest
import com.example.rinconsur.data.remote.RolResponse
import com.example.rinconsur.data.remote.SolicitarCodigoRequest
import com.example.rinconsur.data.remote.UsuarioApi
import com.example.rinconsur.data.remote.UsuarioResponse
import com.example.rinconsur.data.remote.VerificarCodigoRequest
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeUsuarioApi : UsuarioApi {

    // "Base de datos" en memoria
    private val usuarios = listOf(
        UsuarioResponse(1L, "Camila Rojas", "camila@rinconsur.cl", 3L, "Administrador", null, true),
        UsuarioResponse(2L, "Diego Soto", "diego@rinconsur.cl", 1L, "Observador de Pasillo", null, true)
    )

    // Interruptores para simular fallos de red o errores HTTP del servidor
    var sinConexion = false
    var errorHttp: Pair<Int, String>? = null

    // Variables espía para revisar qué envió el Repository
    var ultimoLogin: LoginRequest? = null
    var ultimoCrear: CrearUsuarioRequest? = null
    var ultimoCambiarEstado: Pair<Long, CambiarEstadoRequest>? = null
    var ultimoCambiarRol: Pair<Long, CambiarRolRequest>? = null
    var ultimoSolicitarCodigo: SolicitarCodigoRequest? = null
    var ultimoVerificarCodigo: VerificarCodigoRequest? = null
    var ultimoRestablecerContrasena: RestablecerContrasenaRequest? = null

    private fun verificarErrorSimulado() {
        if (sinConexion) throw IOException("Servidor apagado")
        errorHttp?.let { (codigo, mensaje) ->
            val json = """{"message":"$mensaje"}"""
            val cuerpoError = ResponseBody.create(MediaType.parse("application/json"), json)
            throw HttpException(Response.error<Any>(codigo, cuerpoError))
        }
    }

    override suspend fun iniciarSesion(datos: LoginRequest): UsuarioResponse {
        verificarErrorSimulado()
        ultimoLogin = datos
        return usuarios.first { it.correo == datos.correo }
    }

    override suspend fun crear(datos: CrearUsuarioRequest): UsuarioResponse {
        verificarErrorSimulado()
        ultimoCrear = datos
        return UsuarioResponse(
            id = 3L,
            nombre = datos.nombre,
            correo = datos.correo,
            rolId = datos.rolId,
            rolNombre = "Observador de Pasillo",
            fotoPerfilUrl = null,
            activo = true
        )
    }

    override suspend fun listar(): List<UsuarioResponse> {
        verificarErrorSimulado()
        return usuarios
    }

    override suspend fun obtener(id: Long): UsuarioResponse {
        verificarErrorSimulado()
        return usuarios.first { it.id == id }
    }

    override suspend fun cambiarEstado(id: Long, datos: CambiarEstadoRequest): UsuarioResponse {
        verificarErrorSimulado()
        ultimoCambiarEstado = id to datos
        return usuarios.first { it.id == id }.copy(activo = datos.activo)
    }

    override suspend fun cambiarRol(id: Long, datos: CambiarRolRequest): UsuarioResponse {
        verificarErrorSimulado()
        ultimoCambiarRol = id to datos
        return usuarios.first { it.id == id }.copy(rolId = datos.rolId)
    }

    override suspend fun solicitarCodigo(datos: SolicitarCodigoRequest): CodigoResponse {
        verificarErrorSimulado()
        ultimoSolicitarCodigo = datos
        return CodigoResponse(mensaje = "Código enviado", codigo = "123456")
    }

    override suspend fun verificarCodigo(datos: VerificarCodigoRequest) {
        verificarErrorSimulado()
        ultimoVerificarCodigo = datos
    }

    override suspend fun restablecerContrasena(datos: RestablecerContrasenaRequest) {
        verificarErrorSimulado()
        ultimoRestablecerContrasena = datos
    }

    override suspend fun listarRoles(): List<RolResponse> {
        verificarErrorSimulado()
        return listOf(
            RolResponse(1L, "Observador de Pasillo"),
            RolResponse(2L, "Jefe de Tienda"),
            RolResponse(3L, "Administrador")
        )
    }

    // Las que no usamos en estas pruebas quedan con TODO()
    override suspend fun actualizarPerfil(id: Long, datos: ActualizarPerfilRequest): UsuarioResponse = TODO()
    override suspend fun cambiarContrasena(id: Long, datos: CambiarContrasenaRequest) = TODO()
    override suspend fun subirFoto(id: Long, archivo: MultipartBody.Part): ResponseBody = TODO()
    override suspend fun obtenerFoto(id: Long): ResponseBody = TODO()
}