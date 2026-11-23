package com.example.rinconsur.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import com.example.rinconsur.R
import com.example.rinconsur.data.AdministracionRepository
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.data.VisitanteRepository
import com.example.rinconsur.data.remote.EstadisticasResponse
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.SesionItem
import com.example.rinconsur.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    private lateinit var viewModel: SesionViewModel
    private lateinit var contextoMock: Context
    private lateinit var usuarioRepoMock: UsuarioRepository
    private lateinit var visitanteRepoMock: VisitanteRepository
    private lateinit var adminRepoMock: AdministracionRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeByteArray(any(), any(), any()) } returns null

        mockkConstructor(SesionPreferencias::class)

        usuarioRepoMock = mockk()
        visitanteRepoMock = mockk()
        adminRepoMock = mockk()
        viewModel = SesionViewModel(usuarioRepoMock, visitanteRepoMock, adminRepoMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun cargarUsuario_conUsuarioNoEncontrado404_dejaUsuarioActualEnNull() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns 99L
        coEvery { usuarioRepoMock.obtener(99L) } throws ApiException(404, "No encontrado")

        viewModel.cargarUsuario(contextoMock)

        assertNull(viewModel.usuarioActual.value)
    }

    @Test
    fun cargarUsuario_conIdValido_guardaUsuarioActual() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns 1L
        val usuario = mockk<Usuario>(relaxed = true) {
            every { id } returns 1L
            every { nombre } returns "Cristopher"
        }
        coEvery { usuarioRepoMock.obtener(1L) } returns usuario

        viewModel.cargarUsuario(contextoMock)

        assertEquals("Cristopher", viewModel.usuarioActual.value?.nombre)
    }

    @Test
    fun cargarDatos_cargaEstadisticasYSesionesDelUsuario() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns 1L
        val usuario = mockk<Usuario>(relaxed = true) { every { id } returns 1L }
        val stats = EstadisticasResponse(totalSesiones = 5, sesionesCompletadas = 4, muestrasRegistradas = 120)
        val sesiones = listOf(mockk<SesionItem>(relaxed = true))

        coEvery { usuarioRepoMock.obtener(1L) } returns usuario
        coEvery { visitanteRepoMock.estadisticas(1L) } returns stats
        coEvery { visitanteRepoMock.listarSesiones(1L) } returns sesiones
        coEvery { usuarioRepoMock.obtenerFoto(1L) } returns ByteArray(0)

        viewModel.cargarDatos(contextoMock)
        Thread.sleep(100)

        assertEquals(120, viewModel.uiState.value.totalMuestras)
        assertEquals(4, viewModel.uiState.value.sesionesCompletadas)
        assertEquals(5, viewModel.uiState.value.sesionesTotales)
        assertEquals(1, viewModel.uiState.value.listaSesiones.size)
    }

    @Test
    fun prepararAsignacion_filtraSoloObservadoresActivosYCargaOcupadosYCerrados() {
        val obsActivo = mockk<Usuario>(relaxed = true) {
            every { id } returns 10L
            every { rolId } returns Rol.ID_OBSERVADOR
            every { activo } returns true
        }
        val obsSuspendido = mockk<Usuario>(relaxed = true) {
            every { id } returns 11L
            every { rolId } returns Rol.ID_OBSERVADOR
            every { activo } returns false
        }
        val jefeActivo = mockk<Usuario>(relaxed = true) {
            every { id } returns 12L
            every { rolId } returns Rol.ID_JEFE_TIENDA
            every { activo } returns true
        }

        coEvery { usuarioRepoMock.listar() } returns listOf(obsActivo, obsSuspendido, jefeActivo)
        coEvery { visitanteRepoMock.idsObservadoresOcupados() } returns listOf(10L)
        coEvery { adminRepoMock.accesosCerrados() } returns listOf("Acceso Sur")

        viewModel.prepararAsignacion(contextoMock)
        Thread.sleep(100)

        assertTrue(viewModel.uiState.value.asignacionLista)
        assertEquals(1, viewModel.uiState.value.observadoresAsignables.size)
        assertEquals(10L, viewModel.uiState.value.observadoresAsignables.first().id)
        assertEquals(listOf(10L), viewModel.uiState.value.observadoresOcupados)
        assertEquals(listOf("Acceso Sur"), viewModel.uiState.value.accesosCerrados)
    }

    @Test
    fun prepararAsignacion_sinConexion_guardaMensajeDeError() {
        coEvery { usuarioRepoMock.listar() } throws IOException("Sin red")

        viewModel.prepararAsignacion(contextoMock)
        Thread.sleep(100)

        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun asignarAccesoAObservador_conExito_creaSesionYRecargaDatos() {
        every { anyConstructed<SesionPreferencias>().obtenerIdUsuario() } returns -1L
        coEvery { visitanteRepoMock.crearSesion(10L, "Acceso Norte") } returns mockk(relaxed = true)

        viewModel.asignarAccesoAObservador(contextoMock, 10L, "Acceso Norte")

        coVerify(timeout = 2000, exactly = 1) { visitanteRepoMock.crearSesion(10L, "Acceso Norte") }
    }

    @Test
    fun asignarAccesoAObservador_conError409_guardaMensajeDelServidor() {
        coEvery {
            visitanteRepoMock.crearSesion(10L, "Acceso Norte")
        } throws ApiException(409, "El observador ya tiene una sesión activa")

        viewModel.asignarAccesoAObservador(contextoMock, 10L, "Acceso Norte")
        Thread.sleep(100)

        assertEquals("El observador ya tiene una sesión activa", viewModel.uiState.value.mensajeExito)
    }
}