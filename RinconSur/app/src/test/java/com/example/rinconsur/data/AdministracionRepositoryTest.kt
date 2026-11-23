package com.example.rinconsur.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdministracionRepositoryTest {

    private lateinit var apiFalsa: FakeAdministracionApi
    private lateinit var repositorio: AdministracionRepository

    @Before
    fun preparar() {
        apiFalsa = FakeAdministracionApi()
        repositorio = AdministracionRepository(administracionApi = apiFalsa)
    }

    @Test
    fun estadoAccesos_enviaNullYConvierteListaEnMapa() = runBlocking {
        val mapa = repositorio.estadoAccesos()

        assertNull(apiFalsa.ultimoFiltroDisponible)
        assertEquals(3, mapa.size)
        assertEquals(true, mapa["Acceso Norte"])
        assertEquals(false, mapa["Acceso Sur"])
    }

    @Test
    fun accesosCerrados_filtraPorFalseYDevuelveSoloNombres() = runBlocking {
        val cerrados = repositorio.accesosCerrados()

        assertEquals(false, apiFalsa.ultimoFiltroDisponible)
        assertEquals(listOf("Acceso Sur"), cerrados)
    }

    @Test
    fun cambiarDisponibilidad_actualizaEstadoYRetornaNuevoValor() = runBlocking {
        val nuevoEstado = repositorio.cambiarDisponibilidad("Acceso Norte", false)

        assertFalse(nuevoEstado)
        assertEquals("Acceso Norte", apiFalsa.ultimoCambio?.first)
        assertEquals(false, apiFalsa.ultimoCambio?.second?.disponible)

        val reactivado = repositorio.cambiarDisponibilidad("Acceso Sur", true)
        assertTrue(reactivado)
    }

    @Test
    fun sinConexion_lanzaApiExceptionConCodigo0() {
        apiFalsa.sinConexion = true

        val error = assertThrows(ApiException::class.java) {
            runBlocking { repositorio.estadoAccesos() }
        }

        assertEquals(0, error.codigo)
    }
}