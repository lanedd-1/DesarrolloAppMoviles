package com.example.rinconsur.data.remote

import com.example.rinconsur.model.Usuario


data class LoginRequest(val correo: String, val contrasena: String)

data class CrearUsuarioRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rolId: Long
)

data class ActualizarPerfilRequest(val nombre: String, val correo: String)

data class CambiarContrasenaRequest(val actual: String, val nueva: String)

data class CambiarEstadoRequest(val activo: Boolean)

data class CambiarRolRequest(val rolId: Long)

data class SolicitarCodigoRequest(val correo: String)

data class VerificarCodigoRequest(val correo: String, val codigo: String)

data class RestablecerContrasenaRequest(val correo: String, val codigo: String, val nueva: String)

data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rolId: Long,
    val rolNombre: String,
    val fotoPerfilUrl: String?,
    val activo: Boolean
)

data class RolResponse(val id: Long, val nombre: String)

data class CodigoResponse(val mensaje: String, val codigo: String?)

fun UsuarioResponse.aUsuario() = Usuario(
    nombre = nombre,
    correo = correo,
    id = id,
    rolId = rolId,
    rolNombre = rolNombre,
    fotoPerfilRuta = fotoPerfilUrl,
    activo = activo
)