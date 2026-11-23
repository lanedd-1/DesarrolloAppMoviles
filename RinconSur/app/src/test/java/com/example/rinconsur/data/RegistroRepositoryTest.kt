package com.example.rinconsur.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class RegistroRepositoryTest {

    private lateinit var apiFalsa: FakeRegistroApi
    private lateinit var repositorio: RegistroRepository

    @Before
    fun preparar() {
        apiFalsa = FakeRegistroApi()
        repositorio = RegistroRepository(registroApi = apiFalsa)
    }

    @Test
    fun editar_enviaIdYDatosCorrectosALaApi() = runBlocking {
        repositorio.editar(
            id = 12L,
            clasificacion = "Cliente",
            rangoEtario = "Joven",
            compania = "En pareja",
            bolsas = true,
            destino = null
        )

        assertEquals(12L, apiFalsa.ultimoEditar?.first)
        assertEquals("Cliente", apiFalsa.ultimoEditar?.second?.clasificacion)
        assertEquals("Joven", apiFalsa.ultimoEditar?.second?.rangoEtario)
        assertEquals("En pareja", apiFalsa.ultimoEditar?.second?.compania)
        assertEquals(true, apiFalsa.ultimoEditar?.second?.bolsas)
    }

    @Test
    fun eliminar_enviaIdCorrectoALaApi() = runBlocking {
        repositorio.eliminar(12L)

        assertEquals(12L, apiFalsa.ultimoEliminarId)
    }

    @Test
    fun eliminar_cuandoNoExiste404_lanzaApiException404() {
        apiFalsa.errorHttp = 404 to "Registro no encontrado"

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.eliminar(999L) }
        }

        assertEquals(404, error.codigo)
        assertEquals("Registro no encontrado", error.mensajeServidor)
    }

    @Test
    fun sinConexion_lanzaApiExceptionConCodigo0() {
        apiFalsa.sinConexion = true

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.eliminar(12L) }
        }

        assertEquals(0, error.codigo)
    }
}