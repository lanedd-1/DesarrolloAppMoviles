package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.DashboardRepository
import com.example.rinconsur.data.remote.DistribucionResponse
import com.example.rinconsur.data.remote.ItemDistribucionResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VerResumenViewModelTest {

    private lateinit var viewModel: VerResumenViewModel
    private lateinit var dashboardRepoMock: DashboardRepository
    private lateinit var contextoMock: Context

    private val distribucionPrueba = DistribucionResponse(
        fecha = "20-11-2026",
        acceso = "Acceso Norte",
        total = 50,
        porClasificacion = listOf(
            ItemDistribucionResponse("Cliente", 35),
            ItemDistribucionResponse("Tránsito", 15)
        ),
        porRangoEtario = listOf(
            ItemDistribucionResponse("Adulto", 30),
            ItemDistribucionResponse("Joven", 20)
        ),
        porCompania = listOf(
            ItemDistribucionResponse("Solo", 25),
            ItemDistribucionResponse("En pareja", 25)
        ),
        porBolsas = listOf(
            ItemDistribucionResponse("Con bolsas", 18),
            ItemDistribucionResponse("Sin bolsas", 32)
        )
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dashboardRepoMock = mockk()
        viewModel = VerResumenViewModel(dashboardRepoMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun cargarDatos_conExito_guardaDistribucionesYDatosDelGraficoInicial() {
        coEvery { dashboardRepoMock.distribucion("20-11-2026", "Acceso Norte") } returns distribucionPrueba

        viewModel.cargarDatos(contextoMock, "Acceso Norte", "20-11-2026")

        val estado = viewModel.uiState.value
        assertEquals("Acceso Norte", estado.acceso)
        assertEquals("20-11-2026", estado.fecha)
        assertEquals(50, estado.totalMuestras)
        assertEquals(listOf("Cliente" to 35, "Tránsito" to 15), estado.distribucionClasificacion)
        assertEquals(listOf("Adulto" to 30, "Joven" to 20), estado.distribucionEdades)
        assertEquals(listOf("Solo" to 25, "En pareja" to 25), estado.distribucionCompania)
        assertEquals(listOf("Con bolsas" to 18, "Sin bolsas" to 32), estado.distribucionBolsas)
        assertEquals(listOf("Cliente" to 35, "Tránsito" to 15), estado.datosGrafico)
        assertFalse(estado.cargando)
        assertNull(estado.mensajeError)
        coVerify(exactly = 1) { dashboardRepoMock.distribucion("20-11-2026", "Acceso Norte") }
    }

    @Test
    fun cambiarFiltro_actualizaDatosGraficoSegunCategoriaSeleccionada() {
        coEvery { dashboardRepoMock.distribucion("20-11-2026", "Acceso Norte") } returns distribucionPrueba

        viewModel.cargarDatos(contextoMock, "Acceso Norte", "20-11-2026")

        viewModel.cambiarFiltro("Rango Etario")
        assertEquals("Rango Etario", viewModel.uiState.value.filtroSeleccionado)
        assertEquals(listOf("Adulto" to 30, "Joven" to 20), viewModel.uiState.value.datosGrafico)

        viewModel.cambiarFiltro("Compañía")
        assertEquals("Compañía", viewModel.uiState.value.filtroSeleccionado)
        assertEquals(listOf("Solo" to 25, "En pareja" to 25), viewModel.uiState.value.datosGrafico)

        viewModel.cambiarFiltro("Bolsas de otra tienda")
        assertEquals("Bolsas de otra tienda", viewModel.uiState.value.filtroSeleccionado)
        assertEquals(listOf("Con bolsas" to 18, "Sin bolsas" to 32), viewModel.uiState.value.datosGrafico)

        viewModel.cambiarFiltro("Clasificación")
        assertEquals("Clasificación", viewModel.uiState.value.filtroSeleccionado)
        assertEquals(listOf("Cliente" to 35, "Tránsito" to 15), viewModel.uiState.value.datosGrafico)
    }

    @Test
    fun cargarDatos_conErrorServidor404_guardaMensajeDeApiException() {
        coEvery {
            dashboardRepoMock.distribucion("20-11-2026", "Acceso Norte")
        } throws ApiException(404, "Sin datos para el acceso indicado")

        viewModel.cargarDatos(contextoMock, "Acceso Norte", "20-11-2026")

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Sin datos para el acceso indicado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun cargarDatos_sinConexion_guardaErrorInesperado() {
        coEvery {
            dashboardRepoMock.distribucion("20-11-2026", "Acceso Norte")
        } throws IOException("Sin red")

        viewModel.cargarDatos(contextoMock, "Acceso Norte", "20-11-2026")

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeError)
    }
}