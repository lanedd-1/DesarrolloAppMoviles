package com.example.rinconsur.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DashboardRepositoryTest {

    private lateinit var apiFalsa: FakeDashboardApi
    private lateinit var repositorio: DashboardRepository

    @Before
    fun preparar() {
        apiFalsa = FakeDashboardApi()
        repositorio = DashboardRepository(dashboardApi = apiFalsa)
    }

    @Test
    fun fechasDisponibles_devuelveListaDeFechas() = runBlocking {
        val fechas = repositorio.fechasDisponibles()

        assertEquals(listOf("2026-11-22", "2026-11-21"), fechas)
    }

    @Test
    fun resumenDiario_enviaFechaYDevuelveResumen() = runBlocking {
        val resumen = repositorio.resumenDiario("2026-11-22")

        assertEquals("2026-11-22", apiFalsa.ultimaFechaResumen)
        assertEquals(45, resumen.totalMuestras)
        assertEquals(3, resumen.sesiones)
    }

    @Test
    fun distribucion_enviaFiltrosYDevuelveEstadisticas() = runBlocking {
        val dist = repositorio.distribucion("2026-11-22", "Acceso Norte")

        assertEquals("2026-11-22" to "Acceso Norte", apiFalsa.ultimaConsultaDistribucion)
        assertEquals(25, dist.total)
    }

    @Test
    fun registros_ordenaPorIdDescendenteYMapeaARegistroDetalleItem() = runBlocking {
        val items = repositorio.registros("2026-11-22", "Tránsito")

        assertEquals("2026-11-22" to "Tránsito", apiFalsa.ultimaConsultaRegistros)
        assertEquals(2, items.size)
        assertEquals(5L, items[0].id)
        assertEquals("Proveedor", items[0].clasificacion)
        assertTrue(items[0].bolsas)
        assertEquals("Bodega", items[0].destino)
        assertEquals(1L, items[1].id)
        assertNull(items[1].destino?.takeIf { it == "Bodega" })
    }

    @Test
    fun sinConexion_lanzaApiExceptionConCodigo0() {
        apiFalsa.sinConexion = true

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.fechasDisponibles() }
        }

        assertEquals(0, error.codigo)
    }
}