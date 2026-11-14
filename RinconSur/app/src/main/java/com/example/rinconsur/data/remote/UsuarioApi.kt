package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface UsuarioApi {

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): UsuarioResponse

    @POST("api/usuarios")
    suspend fun crear(@Body datos: CrearUsuarioRequest): UsuarioResponse

    @GET("api/usuarios")
    suspend fun listar(): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(@Path("id") id: Long, @Body datos: ActualizarPerfilRequest): UsuarioResponse

    @PUT("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body datos: CambiarEstadoRequest): UsuarioResponse

    @PUT("api/usuarios/{id}/rol")
    suspend fun cambiarRol(@Path("id") id: Long, @Body datos: CambiarRolRequest): UsuarioResponse

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(@Path("id") id: Long, @Body datos: CambiarContrasenaRequest)

    @POST("api/usuarios/recuperacion/solicitar")
    suspend fun solicitarCodigo(@Body datos: SolicitarCodigoRequest): CodigoResponse

    @POST("api/usuarios/recuperacion/verificar")
    suspend fun verificarCodigo(@Body datos: VerificarCodigoRequest)

    @POST("api/usuarios/recuperacion/restablecer")
    suspend fun restablecerContrasena(@Body datos: RestablecerContrasenaRequest)

    @GET("api/roles")
    suspend fun listarRoles(): List<RolResponse>
}