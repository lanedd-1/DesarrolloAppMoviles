package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.AdministracionRepository
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GestionAccesosViewModelTest {

    private lateinit var viewModel: GestionAccesosViewModel
    private lateinit var contextoMock: Context
    private lateinit var usuarioRepoMock: UsuarioRepository
    private lateinit var adminRepoMock: AdministracionRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        usuarioRepoMock = mockk()
        adminRepoMock = mockk()
        viewModel = GestionAccesosViewModel(usuarioRepoMock, adminRepoMock)
        contextoMock = mockk(relaxed = true)

        mockkConstructor(SesionPreferencias::class)

        every { contextoMock.getString(R.string.gestion_accesos_mensaje_exito, any()) } returns "Acceso actualizado"
        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun cargarDatos_sinSesionActiva_bloqueaPanelYNoLlamaAlServidor() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns -1L

        viewModel.cargarDatos(contextoMock)

        assertFalse(viewModel.uiState.value.esJefeTienda)
        coVerify(exactly = 0) { usuarioRepoMock.obtener(any()) }
        coVerify(exactly = 0) { adminRepoMock.estadoAccesos() }
    }

    @Test
    fun cargarDatos_conRolObservador_noHabilitaPanelNiCargaAccesos() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns 5L
        val observador = mockk<Usuario>(relaxed = true) {
            every { rolId } returns Rol.ID_OBSERVADOR
        }
        coEvery { usuarioRepoMock.obtener(5L) } returns observador

        viewModel.cargarDatos(contextoMock)

        assertFalse(viewModel.uiState.value.esJefeTienda)
        assertFalse(viewModel.uiState.value.cargando)
        coVerify(exactly = 0) { adminRepoMock.estadoAccesos() }
    }

    @Test
    fun cargarDatos_conRolJefeTienda_habilitaPanelYCargaMapaDeAccesos() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns 2L
        val jefe = mockk<Usuario>(relaxed = true) {
            every { rolId } returns Rol.ID_JEFE_TIENDA
        }
        val mapaAccesos = mapOf("Acceso Norte" to true, "Acceso Sur" to false)

        coEvery { usuarioRepoMock.obtener(2L) } returns jefe
        coEvery { adminRepoMock.estadoAccesos() } returns mapaAccesos

        viewModel.cargarDatos(contextoMock)

        assertTrue(viewModel.uiState.value.esJefeTienda)
        assertEquals(mapaAccesos, viewModel.uiState.value.estadoAccesosMap)
        assertNull(viewModel.uiState.value.mensajeError)
    }

    @Test
    fun cargarDatos_conErrorDeRed_muestraMensajeError() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns 2L
        coEvery { usuarioRepoMock.obtener(2L) } throws IOException("Sin conexión")

        viewModel.cargarDatos(contextoMock)

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun onCambiarEstadoAcceso_conExito_actualizaEstadoEnElMapa() {
        coEvery { adminRepoMock.cambiarDisponibilidad("Acceso Sur", true) } returns true

        viewModel.onCambiarEstadoAcceso(contextoMock, "Acceso Sur", true)

        assertEquals(true, viewModel.uiState.value.estadoAccesosMap["Acceso Sur"])
        assertEquals("Acceso actualizado", viewModel.uiState.value.mensajeExito)
        assertFalse(viewModel.uiState.value.cargando)
        coVerify(exactly = 1) { adminRepoMock.cambiarDisponibilidad("Acceso Sur", true) }
    }

    @Test
    fun onCambiarEstadoAcceso_conError403_muestraErrorDelServidor() {
        coEvery {
            adminRepoMock.cambiarDisponibilidad("Acceso Norte", false)
        } throws ApiException(403, "Acceso denegado")

        viewModel.onCambiarEstadoAcceso(contextoMock, "Acceso Norte", false)

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Acceso denegado", viewModel.uiState.value.mensajeError)
    }
}