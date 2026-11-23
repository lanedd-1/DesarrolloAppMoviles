package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.DashboardRepository
import com.example.rinconsur.data.RegistroRepository
import com.example.rinconsur.model.RegistroDetalleItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
class DetalleResumenViewModelTest {

    private lateinit var viewModel: DetalleResumenViewModel
    private lateinit var dashboardRepoMock: DashboardRepository
    private lateinit var registroRepoMock: RegistroRepository
    private lateinit var contextoMock: Context

    private val registro1 = RegistroDetalleItem(
        id = 1L,
        hora = "10:15:00",
        clasificacion = "CLIENTE",
        rangoEtario = "ADULTO",
        compania = "SOLO",
        bolsas = true,
        destino = null
    )

    private val registro2 = RegistroDetalleItem(
        id = 2L,
        hora = "10:20:00",
        clasificacion = "TRANSITO",
        rangoEtario = "JOVEN",
        compania = "PAREJA",
        bolsas = false,
        destino = "PASILLO_CENTRAL"
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dashboardRepoMock = mockk()
        registroRepoMock = mockk()
        viewModel = DetalleResumenViewModel(dashboardRepoMock, registroRepoMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun cargarDetalles_conExito_guardaRegistrosYTotal() {
        coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } returns listOf(registro1, registro2)

        viewModel.cargarDetalles(contextoMock, "Acceso Norte", "20-11-2026")

        val estado = viewModel.uiState.value
        assertEquals("Acceso Norte", estado.acceso)
        assertEquals("20-11-2026", estado.fecha)
        assertEquals(2, estado.totalRegistros)
        assertEquals(listOf(registro1, registro2), estado.registros)
        assertFalse(estado.cargando)
        assertNull(estado.mensajeError)
    }

    @Test
    fun eliminarRegistro_eliminaYRecargaLaListaActualizada() {
        val listaInicial = listOf(registro1, registro2)
        val listaFinal = listOf(registro2)

        coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } returns listaInicial
        coEvery { registroRepoMock.eliminar(1L) } coAnswers {
            coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } returns listaFinal
        }

        viewModel.cargarDetalles(contextoMock, "Acceso Norte", "20-11-2026")
        viewModel.eliminarRegistro(contextoMock, 1L)

        val estado = viewModel.uiState.value
        assertEquals(1, estado.totalRegistros)
        assertEquals(listaFinal, estado.registros)
        assertFalse(estado.cargando)
        coVerifyOrder {
            registroRepoMock.eliminar(1L)
            dashboardRepoMock.registros("20-11-2026", "Acceso Norte")
        }
    }

    @Test
    fun actualizarRegistro_editaYRecargaRegistros() {
        val registroEditado = registro1.copy(clasificacion = "TRANSITO", destino = "ZONA_SUR", bolsas = false)
        val listaInicial = listOf(registro1)
        val listaFinal = listOf(registroEditado)

        coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } returns listaInicial
        coEvery {
            registroRepoMock.editar(1L, "TRANSITO", "ADULTO", "SOLO", false, "ZONA_SUR")
        } coAnswers {
            coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } returns listaFinal
        }

        viewModel.cargarDetalles(contextoMock, "Acceso Norte", "20-11-2026")
        viewModel.actualizarRegistro(
            contexto = contextoMock,
            idRegistro = 1L,
            clasificacion = "TRANSITO",
            rangoEtario = "ADULTO",
            compania = "SOLO",
            bolsas = false,
            destino = "ZONA_SUR"
        )

        val estado = viewModel.uiState.value
        assertEquals("TRANSITO", estado.registros.first().clasificacion)
        assertEquals("ZONA_SUR", estado.registros.first().destino)
        assertFalse(estado.cargando)
        coVerify(exactly = 1) {
            registroRepoMock.editar(1L, "TRANSITO", "ADULTO", "SOLO", false, "ZONA_SUR")
        }
    }

    @Test
    fun eliminarRegistro_conError403_guardaMensajeDeApiException() {
        coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } returns listOf(registro1)
        coEvery { registroRepoMock.eliminar(1L) } throws ApiException(403, "No tiene permisos para eliminar")

        viewModel.cargarDetalles(contextoMock, "Acceso Norte", "20-11-2026")
        viewModel.eliminarRegistro(contextoMock, 1L)

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("No tiene permisos para eliminar", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun cargarDetalles_sinConexion_guardaErrorInesperado() {
        coEvery { dashboardRepoMock.registros("20-11-2026", "Acceso Norte") } throws IOException("Sin red")

        viewModel.cargarDetalles(contextoMock, "Acceso Norte", "20-11-2026")

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeError)
    }
}