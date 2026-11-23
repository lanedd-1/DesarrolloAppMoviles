package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GestionUsuariosViewModelTest {

    private lateinit var viewModel: GestionUsuariosViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repositorioMock = mockk()
        viewModel = GestionUsuariosViewModel(repositorioMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargarUsuarios_conRespuestaExitosa_actualizaListaDeUsuarios() {
        val usuario1 = mockk<Usuario>(relaxed = true) {
            every { id } returns 1L
            every { nombre } returns "Camila Rojas"
        }
        val usuario2 = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
        }
        coEvery { repositorioMock.listar() } returns listOf(usuario1, usuario2)

        viewModel.cargarUsuarios(contextoMock)

        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.mensajeError)
        assertEquals(2, viewModel.uiState.value.usuarios.size)
        assertEquals("Camila Rojas", viewModel.uiState.value.usuarios[0].nombre)
    }

    @Test
    fun cargarUsuarios_sinConexion_muestraMensajeDeError() {
        coEvery { repositorioMock.listar() } throws IOException("Sin red")

        viewModel.cargarUsuarios(contextoMock)

        assertFalse(viewModel.uiState.value.cargando)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun cambiarEstadoUsuario_conExito_actualizaEstadoEnLaLista() {
        val original = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
            every { activo } returns true
        }
        val suspendido = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
            every { activo } returns false
        }
        coEvery { repositorioMock.listar() } returns listOf(original)
        coEvery { repositorioMock.cambiarEstado(2L, false) } returns suspendido

        viewModel.cargarUsuarios(contextoMock)
        viewModel.cambiarEstadoUsuario(contextoMock, original, false, "Estado de %s actualizado")

        assertFalse(viewModel.uiState.value.usuarios.first().activo)
        assertEquals("Estado de Diego Soto actualizado", viewModel.uiState.value.mensajeExito)
        coVerify(exactly = 1) { repositorioMock.cambiarEstado(2L, false) }
    }

    @Test
    fun cambiarEstadoUsuario_conError403_muestraErrorYNoModificaLista() {
        val original = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
            every { activo } returns true
        }
        coEvery { repositorioMock.listar() } returns listOf(original)
        coEvery { repositorioMock.cambiarEstado(2L, false) } throws ApiException(403, "No tienes permisos")

        viewModel.cargarUsuarios(contextoMock)
        viewModel.cambiarEstadoUsuario(contextoMock, original, false, "Estado de %s actualizado")

        assertEquals(true, viewModel.uiState.value.usuarios.first().activo)
        assertEquals("No tienes permisos", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun cambiarRolUsuario_conExito_actualizaRolEnLaLista() {
        val original = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
            every { rolId } returns Rol.ID_OBSERVADOR
        }
        val promovido = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
            every { rolId } returns Rol.ID_JEFE_TIENDA
        }
        coEvery { repositorioMock.listar() } returns listOf(original)
        coEvery { repositorioMock.cambiarRol(2L, Rol.ID_JEFE_TIENDA) } returns promovido

        viewModel.cargarUsuarios(contextoMock)
        viewModel.cambiarRolUsuario(contextoMock, original, Rol.ID_JEFE_TIENDA, "Rol de %s actualizado")

        assertEquals(Rol.ID_JEFE_TIENDA, viewModel.uiState.value.usuarios.first().rolId)
        assertEquals("Rol de Diego Soto actualizado", viewModel.uiState.value.mensajeExito)
        coVerify(exactly = 1) { repositorioMock.cambiarRol(2L, Rol.ID_JEFE_TIENDA) }
    }

    @Test
    fun cambiarRolUsuario_conError400_muestraErrorDelServidor() {
        val original = mockk<Usuario>(relaxed = true) {
            every { id } returns 2L
            every { nombre } returns "Diego Soto"
            every { rolId } returns Rol.ID_OBSERVADOR
        }
        coEvery { repositorioMock.listar() } returns listOf(original)
        coEvery { repositorioMock.cambiarRol(2L, 99L) } throws ApiException(400, "Rol inválido")

        viewModel.cargarUsuarios(contextoMock)
        viewModel.cambiarRolUsuario(contextoMock, original, 99L, "Rol de %s actualizado")

        assertEquals(Rol.ID_OBSERVADOR, viewModel.uiState.value.usuarios.first().rolId)
        assertEquals("Rol inválido", viewModel.uiState.value.mensajeError)
    }
}