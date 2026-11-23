package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        contextoMock = mockk(relaxed = true)
        repositorioMock = mockk()
        viewModel = LoginViewModel(repositorioMock)

        every { contextoMock.getString(R.string.login_error_correo_obligatorio) } returns "El correo es obligatorio"
        every { contextoMock.getString(R.string.formulario_error_correo_largo) } returns "El correo no puede superar los 50 caracteres"
        every { contextoMock.getString(R.string.login_error_correo_institucional) } returns "Debe ingresar su correo corporativo (@RinconSur.cl)"
        every { contextoMock.getString(R.string.login_error_contrasena_obligatoria) } returns "La contraseña es obligatoria"
        every { contextoMock.getString(R.string.login_error_credenciales_incorrectas) } returns "Correo o contraseña incorrectos"
        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun onIngresar_conCamposVacios_muestraErrores() {
        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria", viewModel.uiState.value.errorContrasena)
        coVerify(exactly = 0) { repositorioMock.iniciarSesion(any(), any()) }
    }

    @Test
    fun onIngresar_conCorreoSinDominioCorporativo_muestraError() {
        viewModel.onCorreoCambia("cristopher@gmail.com")
        viewModel.onContrasenaCambia("Abc12345.")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("Debe ingresar su correo corporativo (@RinconSur.cl)", viewModel.uiState.value.errorCorreo)
        assertNull(viewModel.uiState.value.errorContrasena)
        coVerify(exactly = 0) { repositorioMock.iniciarSesion(any(), any()) }
    }

    @Test
    fun onIngresar_conCorreoValidoYContrasenaVacia_muestraErrorSoloEnContrasena() {
        viewModel.onCorreoCambia("cristopher@rinconsur.cl")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertNull(viewModel.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun onCorreoCambia_conMasDe50Caracteres_ignoraElCambio() {
        val correoValido = "observador@rinconsur.cl"
        val correoMuyLargo = "a".repeat(45) + "@rinconsur.cl"

        viewModel.onCorreoCambia(correoValido)
        viewModel.onCorreoCambia(correoMuyLargo)

        assertEquals(correoValido, viewModel.uiState.value.correo)
    }

    @Test
    fun onIngresar_conCredencialesValidas_normalizaCorreoYMarcaLoginExitoso() {
        val usuarioFicticio = mockk<Usuario>(relaxed = true) {
            every { id } returns 10L
        }
        coEvery {
            repositorioMock.iniciarSesion("observador@rinconsur.cl", "Abc12345.")
        } returns usuarioFicticio

        viewModel.onCorreoCambia("  Observador@RinconSur.cl  ")
        viewModel.onContrasenaCambia("Abc12345.")

        viewModel.onIngresar(contextoMock)

        assertTrue(viewModel.uiState.value.loginExitoso)
        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.errorCorreo)
        assertNull(viewModel.uiState.value.errorContrasena)
        coVerify(exactly = 1) { repositorioMock.iniciarSesion("observador@rinconsur.cl", "Abc12345.") }
    }

    @Test
    fun onIngresar_conCredencialesIncorrectas401_muestraErrorEnContrasena() {
        coEvery {
            repositorioMock.iniciarSesion("observador@rinconsur.cl", "ClaveErronea")
        } throws ApiException(401, "Correo o contraseña incorrectos")

        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onContrasenaCambia("ClaveErronea")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun onIngresar_conCuentaSuspendida403_bloqueaAccesoYMarcaSuspendida() {
        coEvery {
            repositorioMock.iniciarSesion("suspendido@rinconsur.cl", "Abc12345.")
        } throws ApiException(403, "Cuenta suspendida")

        viewModel.onCorreoCambia("suspendido@rinconsur.cl")
        viewModel.onContrasenaCambia("Abc12345.")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertTrue(viewModel.uiState.value.mostrarDialogoSuspendido)
    }

    @Test
    fun onIngresar_sinConexion_muestraErrorInesperado() {
        coEvery {
            repositorioMock.iniciarSesion("observador@rinconsur.cl", "Abc12345.")
        } throws IOException("Sin conexión a internet")

        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onContrasenaCambia("Abc12345.")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.errorContrasena)
    }
}