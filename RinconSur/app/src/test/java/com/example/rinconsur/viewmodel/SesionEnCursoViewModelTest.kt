package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.VisitanteRepository
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RegistroDetalleItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SesionEnCursoViewModelTest {

    private lateinit var viewModel: SesionEnCursoViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: VisitanteRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repositorioMock = mockk()
        viewModel = SesionEnCursoViewModel(repositorioMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.sesion_curso_sin_registros) } returns "Sin registros aún"
        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun inicializarSesion_sinRegistrosPrevios_iniciaEnCeroYNoPermiteAnular() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")

        assertEquals(10L, viewModel.uiState.value.idSesionActual)
        assertEquals("Acceso Norte", viewModel.uiState.value.accesoNombre)
        assertEquals(0, viewModel.uiState.value.totalRegistrosSesion)
        assertEquals("Sin registros aún", viewModel.uiState.value.ultimoRegistroResumen)
        assertFalse(viewModel.uiState.value.permiteAnular)
    }

    @Test
    fun inicializarSesion_conRegistrosPrevios_cargaTotalYHabilitaAnular() {
        val registroPrevio = mockk<RegistroDetalleItem>(relaxed = true) {
            every { clasificacion } returns "CLIENTE"
            every { rangoEtario } returns "ADULTO"
            every { compania } returns "SOLO"
        }
        coEvery { repositorioMock.listarRegistros(10L) } returns listOf(registroPrevio)

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")

        assertEquals(1, viewModel.uiState.value.totalRegistrosSesion)
        assertTrue(viewModel.uiState.value.permiteAnular)
    }

    @Test
    fun onGuardarRegistro_sinSerTransito_enviaDestinoNullEIncrementaContador() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        coEvery {
            repositorioMock.guardarRegistro(any(), any(), any(), any(), any(), any(), any())
        } returns mockk(relaxed = true)

        val clasificacionNoTransito = ClasificacionPersona.entries.first { it != ClasificacionPersona.TRANSITO }

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")
        viewModel.onClasificacionSeleccionada(clasificacionNoTransito)
        viewModel.onBolsasOtraTiendaCambia(true)

        viewModel.onGuardarRegistro(contextoMock)

        assertEquals(1, viewModel.uiState.value.totalRegistrosSesion)
        assertTrue(viewModel.uiState.value.permiteAnular)
        coVerify(exactly = 1) {
            repositorioMock.guardarRegistro(
                sesionId = 10L,
                clasificacion = clasificacionNoTransito.name,
                rangoEtario = any(),
                compania = any(),
                bolsas = true,
                destino = isNull(),
                hora = any()
            )
        }
    }

    @Test
    fun onGuardarRegistro_enClasificacionTransito_enviaCodigoDeDestino() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        coEvery {
            repositorioMock.guardarRegistro(any(), any(), any(), any(), any(), any(), any())
        } returns mockk(relaxed = true)

        viewModel.inicializarSesion(contextoMock, 10L, "Tránsito")
        viewModel.onClasificacionSeleccionada(ClasificacionPersona.TRANSITO)
        val codigoEsperado = viewModel.uiState.value.destinoSeleccionado.codigo

        viewModel.onGuardarRegistro(contextoMock)

        assertNotNull(codigoEsperado)
        coVerify(exactly = 1) {
            repositorioMock.guardarRegistro(
                sesionId = 10L,
                clasificacion = ClasificacionPersona.TRANSITO.name,
                rangoEtario = any(),
                compania = any(),
                bolsas = any(),
                destino = codigoEsperado,
                hora = any()
            )
        }
    }

    @Test
    fun onGuardarRegistro_conErrorDeRed_noIncrementaContadorYMuestraError() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        coEvery {
            repositorioMock.guardarRegistro(any(), any(), any(), any(), any(), any(), any())
        } throws IOException("Sin red")

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")
        viewModel.onGuardarRegistro(contextoMock)

        assertEquals(0, viewModel.uiState.value.totalRegistrosSesion)
        assertFalse(viewModel.uiState.value.permiteAnular)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun onAnularUltimoRegistro_sinRegistros_noLlamaAlServidor() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")

        viewModel.onAnularUltimoRegistro(contextoMock)

        coVerify(exactly = 0) { repositorioMock.anularUltimoRegistro(any()) }
    }

    @Test
    fun onAnularUltimoRegistro_conRegistroGuardado_decrementaContadorYDeshabilitaAnular() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        coEvery {
            repositorioMock.guardarRegistro(any(), any(), any(), any(), any(), any(), any())
        } returns mockk(relaxed = true)
        coEvery { repositorioMock.anularUltimoRegistro(10L) } returns Unit

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")
        viewModel.onGuardarRegistro(contextoMock)
        assertEquals(1, viewModel.uiState.value.totalRegistrosSesion)

        viewModel.onAnularUltimoRegistro(contextoMock)

        assertEquals(0, viewModel.uiState.value.totalRegistrosSesion)
        assertFalse(viewModel.uiState.value.permiteAnular)
        assertEquals("Sin registros aún", viewModel.uiState.value.ultimoRegistroResumen)
        coVerify(exactly = 1) { repositorioMock.anularUltimoRegistro(10L) }
    }

    @Test
    fun finalizarSesionConteo_conExito_marcaSesionFinalizada() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        coEvery { repositorioMock.finalizarSesion(10L) } returns mockk(relaxed = true)

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")
        viewModel.finalizarSesionConteo(contextoMock)

        assertTrue(viewModel.uiState.value.sesionFinalizada)
        assertNull(viewModel.uiState.value.mensajeError)
        coVerify(exactly = 1) { repositorioMock.finalizarSesion(10L) }
    }

    @Test
    fun finalizarSesionConteo_conError400_muestraErrorYNoFinaliza() {
        coEvery { repositorioMock.listarRegistros(10L) } returns emptyList()
        coEvery { repositorioMock.finalizarSesion(10L) } throws ApiException(400, "La sesión ya fue cerrada")

        viewModel.inicializarSesion(contextoMock, 10L, "Acceso Norte")
        viewModel.finalizarSesionConteo(contextoMock)

        assertFalse(viewModel.uiState.value.sesionFinalizada)
        assertEquals("La sesión ya fue cerrada", viewModel.uiState.value.mensajeError)
    }
}