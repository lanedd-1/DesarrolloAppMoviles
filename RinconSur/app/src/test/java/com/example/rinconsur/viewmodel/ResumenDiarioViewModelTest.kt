package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.DashboardRepository
import com.example.rinconsur.data.remote.ItemDistribucionResponse
import com.example.rinconsur.data.remote.ResumenDiarioResponse
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ResumenDiarioViewModelTest {

    private lateinit var viewModel: ResumenDiarioViewModel
    private lateinit var dashboardRepoMock: DashboardRepository
    private lateinit var contextoMock: Context

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        dashboardRepoMock = mockk()
        viewModel = ResumenDiarioViewModel(dashboardRepoMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
        every { contextoMock.getString(R.string.rol_resumen_admin) } returns "Administrador"
        every { contextoMock.getString(R.string.rol_resumen_jefatura) } returns "Jefatura"
        every { contextoMock.getString(R.string.resumen_diario_label_hoy, any()) } answers {
            val args = secondArg<Array<Any>>()
            "Hoy, ${args[0]}"
        }
    }

    @After
    fun limpiar() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun cargarDatos_conUsuarioAdministrador_formateaPerfilYCargaResumen() {
        val admin = mockk<Usuario>(relaxed = true) {
            every { nombre } returns "Cristopher Gonzalez"
            every { rolId } returns Rol.ID_ADMINISTRADOR
        }
        val resumenReal = ResumenDiarioResponse(
            fecha = "20-11-2026",
            totalMuestras = 85,
            sesiones = 4,
            flujoPorAcceso = listOf(
                ItemDistribucionResponse(etiqueta = "Acceso Sur", cantidad = 15),
                ItemDistribucionResponse(etiqueta = "Acceso Norte", cantidad = 70)
            )
        )

        coEvery { dashboardRepoMock.fechasDisponibles() } returns listOf("20-11-2026", "19-11-2026")
        coEvery { dashboardRepoMock.resumenDiario("20-11-2026") } returns resumenReal

        viewModel.cargarDatos(contextoMock, admin)

        val estado = viewModel.uiState.value
        assertEquals("CG", estado.iniciales)
        assertEquals("Cristopher G.", estado.nombreUsuario)
        assertEquals("Administrador", estado.rolUsuario)
        assertEquals("20-11-2026", estado.fechaSeleccionada)
        assertEquals(85, estado.totalIngresos)
        assertEquals(2, estado.totalAccesos)
        assertEquals("Acceso Norte", estado.flujos.first().acceso)
        assertEquals("Alto Tráfico", estado.flujos.first().estado)
        assertFalse(estado.cargando)
        assertNull(estado.mensajeError)
    }

    @Test
    fun cargarDatos_conFechasVacias_usaFechaDeHoyYRolJefatura() {
        val jefe = mockk<Usuario>(relaxed = true) {
            every { nombre } returns "Brandon"
            every { rolId } returns Rol.ID_JEFE_TIENDA
        }
        val hoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val resumenVacio = ResumenDiarioResponse(
            fecha = hoy,
            totalMuestras = 0,
            sesiones = 0,
            flujoPorAcceso = emptyList()
        )

        coEvery { dashboardRepoMock.fechasDisponibles() } returns emptyList()
        coEvery { dashboardRepoMock.resumenDiario(hoy) } returns resumenVacio

        viewModel.cargarDatos(contextoMock, jefe)

        val estado = viewModel.uiState.value
        assertEquals("BR", estado.iniciales)
        assertEquals("Brandon", estado.nombreUsuario)
        assertEquals("Jefatura", estado.rolUsuario)
        assertEquals(hoy, estado.fechaSeleccionada)
        assertEquals("Hoy, $hoy", estado.labelFecha)
    }

    @Test
    fun seleccionarFecha_actualizaResumenDeLaNuevaFecha() {
        val resumen1 = ResumenDiarioResponse(
            fecha = "21-11-2026",
            totalMuestras = 40,
            sesiones = 2,
            flujoPorAcceso = listOf(ItemDistribucionResponse("Acceso Norte", 40))
        )
        val resumen2 = ResumenDiarioResponse(
            fecha = "20-11-2026",
            totalMuestras = 110,
            sesiones = 5,
            flujoPorAcceso = listOf(
                ItemDistribucionResponse("Acceso Norte", 50),
                ItemDistribucionResponse("Acceso Sur", 45),
                ItemDistribucionResponse("Acceso Este", 15)
            )
        )

        coEvery { dashboardRepoMock.fechasDisponibles() } returns listOf("21-11-2026", "20-11-2026")
        coEvery { dashboardRepoMock.resumenDiario("21-11-2026") } returns resumen1
        coEvery { dashboardRepoMock.resumenDiario("20-11-2026") } returns resumen2

        viewModel.cargarDatos(contextoMock, null)
        viewModel.seleccionarFecha(contextoMock, "20-11-2026")

        val estado = viewModel.uiState.value
        assertEquals("20-11-2026", estado.fechaSeleccionada)
        assertEquals(110, estado.totalIngresos)
        assertEquals(3, estado.totalAccesos)
        coVerify(exactly = 1) { dashboardRepoMock.resumenDiario("20-11-2026") }
    }

    @Test
    fun cargarDatos_conErrorServidor404_guardaMensajeDeError() {
        coEvery { dashboardRepoMock.fechasDisponibles() } throws ApiException(404, "No hay registros disponibles")

        viewModel.cargarDatos(contextoMock, null)

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("No hay registros disponibles", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun seleccionarFecha_sinConexion_guardaErrorInesperado() {
        val resumenInicial = ResumenDiarioResponse(
            fecha = "21-11-2026",
            totalMuestras = 10,
            sesiones = 1,
            flujoPorAcceso = emptyList()
        )
        coEvery { dashboardRepoMock.fechasDisponibles() } returns listOf("21-11-2026")
        coEvery { dashboardRepoMock.resumenDiario("21-11-2026") } returns resumenInicial
        coEvery { dashboardRepoMock.resumenDiario("18-11-2026") } throws IOException("Sin red")

        viewModel.cargarDatos(contextoMock, null)
        viewModel.seleccionarFecha(contextoMock, "18-11-2026")

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeError)
    }
}