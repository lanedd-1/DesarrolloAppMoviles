package com.example.rinconsur.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VisitanteRepositoryTest {

    private lateinit var apiFalsa: FakeVisitanteApi
    private lateinit var repositorio: VisitanteRepository

    @Before
    fun preparar() {
        apiFalsa = FakeVisitanteApi()
        repositorio = VisitanteRepository(visitanteApi = apiFalsa)
    }

    @Test
    fun listarSesiones_convierteRespuestaEnSesionItem() = runBlocking {
        val lista = repositorio.listarSesiones(10L)

        assertEquals(2, lista.size)
        assertEquals("SES-001", lista[0].codigo)
        assertEquals("Acceso Norte", lista[0].acceso)
    }

    @Test
    fun idsObservadoresOcupados_extraeIdsDeSesionesActivas() = runBlocking {
        val ocupados = repositorio.idsObservadoresOcupados()

        assertEquals(listOf(10L), ocupados)
    }

    @Test
    fun crearSesion_y_finalizarSesion_devuelvenSesionItemActualizada() = runBlocking {
        val creada = repositorio.crearSesion(usuarioId = 15L, acceso = "Acceso Sur")
        assertEquals(15L, apiFalsa.ultimoCrearSesion?.usuarioId)
        assertEquals("Acceso Sur", creada.acceso)
        assertEquals("En curso", creada.estado)

        val finalizada = repositorio.finalizarSesion(1L)
        assertEquals(1L, apiFalsa.ultimaSesionFinalizadaId)
        assertEquals("Finalizada", finalizada.estado)
    }

    @Test
    fun listarRegistros_y_guardarRegistro_mapeanARegistroDetalleItem() = runBlocking {
        val existentes = repositorio.listarRegistros(1L)
        assertEquals(2, existentes.size)
        assertEquals("Cliente", existentes[0].clasificacion)

        val nuevo = repositorio.guardarRegistro(
            sesionId = 1L,
            clasificacion = "Cliente",
            rangoEtario = "Adulto Mayor",
            compania = "Acompañado",
            bolsas = true,
            destino = null,
            hora = "10:05"
        )
        assertEquals(1L, apiFalsa.ultimoGuardarRegistro?.first)
        assertEquals(102L, nuevo.id)
        assertEquals("Adulto Mayor", nuevo.rangoEtario)
        assertTrue(nuevo.bolsas)
    }

    @Test
    fun anularUltimoRegistro_enviaIdSesionALaApi() = runBlocking {
        repositorio.anularUltimoRegistro(1L)

        assertEquals(1L, apiFalsa.ultimaAnulacionSesionId)
    }

    @Test
    fun estadisticas_devuelveTotalesDelUsuario() = runBlocking {
        val stats = repositorio.estadisticas(10L)

        assertEquals(2, stats.totalSesiones)
        assertEquals(1, stats.sesionesCompletadas)
        assertEquals(16, stats.muestrasRegistradas)
    }

    @Test
    fun sinConexion_lanzaApiExceptionConCodigo0() {
        apiFalsa.sinConexion = true

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.listarSesiones(10L) }
        }

        assertEquals(0, error.codigo)
    }
}