package com.example.rinconsur.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UsuarioRepositoryTest {

    private lateinit var apiFalsa: FakeUsuarioApi
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun preparar() {
        apiFalsa = FakeUsuarioApi()
        repositorio = UsuarioRepository(usuarioApi = apiFalsa)
    }

    @Test
    fun iniciarSesion_convierteLaRespuestaEnUsuario() = runBlocking {
        val usuario = repositorio.iniciarSesion("camila@rinconsur.cl", "Abc12345.")

        assertEquals("camila@rinconsur.cl", apiFalsa.ultimoLogin?.correo)
        assertEquals(1L, usuario.id)
        assertEquals("Camila Rojas", usuario.nombre)
        assertEquals("Administrador", usuario.rolNombre)
        assertTrue(usuario.activo)
    }

    @Test
    fun iniciarSesion_conCredencialesIncorrectas_lanzaApiException401() {
        apiFalsa.errorHttp = 401 to "Correo o contraseña incorrectos"

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.iniciarSesion("camila@rinconsur.cl", "ClaveMala") }
        }

        assertEquals(401, error.codigo)
        assertEquals("Correo o contraseña incorrectos", error.mensajeServidor)
    }

    @Test
    fun registrar_enviaDatosYDevuelveUsuarioCreado() = runBlocking {
        val creado = repositorio.registrar("Jose Bustamante", "jose@rinconsur.cl", "Abc12345.", 1L)

        assertEquals("Jose Bustamante", apiFalsa.ultimoCrear?.nombre)
        assertEquals("jose@rinconsur.cl", apiFalsa.ultimoCrear?.correo)
        assertEquals(3L, creado.id)
        assertEquals("Jose Bustamante", creado.nombre)
    }

    @Test
    fun registrar_conCorreoDuplicado_lanzaApiException409() {
        apiFalsa.errorHttp = 409 to "El correo ya existe"

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.registrar("Jose", "camila@rinconsur.cl", "Abc12345.", 1L) }
        }

        assertEquals(409, error.codigo)
        assertEquals("El correo ya existe", error.mensajeServidor)
    }

    @Test
    fun listar_devuelveTodosLosUsuariosMapeados() = runBlocking {
        val lista = repositorio.listar()

        assertEquals(2, lista.size)
        assertEquals("Diego Soto", lista[1].nombre)
    }

    @Test
    fun cambiarEstado_y_cambiarRol_actualizanElUsuario() = runBlocking {
        val suspendido = repositorio.cambiarEstado(2L, false)
        assertFalse(suspendido.activo)
        assertEquals(false, apiFalsa.ultimoCambiarEstado?.second?.activo)

        val conNuevoRol = repositorio.cambiarRol(2L, 2L)
        assertEquals(2L, conNuevoRol.rolId)
        assertEquals(2L, apiFalsa.ultimoCambiarRol?.second?.rolId)
    }

    @Test
    fun recuperacionContrasena_enviaDatosCorrectosALaApi() = runBlocking {
        val respuestaCodigo = repositorio.solicitarCodigo("camila@rinconsur.cl")
        assertEquals("123456", respuestaCodigo.codigo)
        assertEquals("camila@rinconsur.cl", apiFalsa.ultimoSolicitarCodigo?.correo)

        repositorio.verificarCodigo("camila@rinconsur.cl", "123456")
        assertEquals("123456", apiFalsa.ultimoVerificarCodigo?.codigo)

        repositorio.restablecerContrasena("camila@rinconsur.cl", "123456", "Nueva123.")
        assertEquals("Nueva123.", apiFalsa.ultimoRestablecerContrasena?.nueva)
    }

    @Test
    fun listarRoles_convierteRolResponseEnRol() = runBlocking {
        val roles = repositorio.listarRoles()

        assertEquals(3, roles.size)
        assertEquals("Jefe de Tienda", roles[1].nombre)
    }

    @Test
    fun sinConexion_lanzaApiExceptionConCodigo0() {
        apiFalsa.sinConexion = true

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.listar() }
        }

        assertEquals(0, error.codigo)
    }
}