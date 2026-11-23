package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
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
class CambiarContrasenaViewModelTest {

    private lateinit var viewModel: CambiarContrasenaViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repositorioMock = mockk()
        viewModel = CambiarContrasenaViewModel(repositorioMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.cambiar_error_obligatoria) } returns "La nueva contraseña es obligatoria"
        every { contextoMock.getString(R.string.cambiar_error_formato) } returns "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
        every { contextoMock.getString(R.string.cambiar_error_confirmar_obligatoria) } returns "Debe confirmar la contraseña"
        every { contextoMock.getString(R.string.cambiar_error_no_coinciden) } returns "Las contraseñas no coinciden"
        every { contextoMock.getString(R.string.cambiar_error_cuenta_no_identificada) } returns "No se pudo identificar la cuenta o el código"
        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun onGuardarContrasena_conCamposVacios_noLlamaAlServidorYMuestraErrores() {
        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertEquals("La nueva contraseña es obligatoria", viewModel.uiState.value.errorContrasenaNueva)
        assertEquals("Debe confirmar la contraseña", viewModel.uiState.value.errorConfirmarContrasena)
        assertFalse(viewModel.uiState.value.cambioExitoso)
        coVerify(exactly = 0) { repositorioMock.restablecerContrasena(any(), any(), any()) }
    }

    @Test
    fun onGuardarContrasena_conContrasenaSinFormato_noLlamaAlServidorYMuestraError() {
        viewModel.onContrasenaNuevaCambia("minusculas")
        viewModel.onConfirmarContrasenaCambia("minusculas")

        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertEquals("Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial", viewModel.uiState.value.errorContrasenaNueva)
        assertFalse(viewModel.uiState.value.cambioExitoso)
        coVerify(exactly = 0) { repositorioMock.restablecerContrasena(any(), any(), any()) }
    }

    @Test
    fun onGuardarContrasena_conContrasenasDiferentes_muestraError() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Diferente123.")

        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorConfirmarContrasena)
        assertFalse(viewModel.uiState.value.cambioExitoso)
        coVerify(exactly = 0) { repositorioMock.restablecerContrasena(any(), any(), any()) }
    }

    @Test
    fun onGuardarContrasena_sinCorreoOCodigo_noLlamaAlServidorYMuestraError() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")

        viewModel.onGuardarContrasena("", "", contextoMock)

        assertEquals("No se pudo identificar la cuenta o el código", viewModel.uiState.value.errorContrasenaNueva)
        assertFalse(viewModel.uiState.value.cambioExitoso)
        coVerify(exactly = 0) { repositorioMock.restablecerContrasena(any(), any(), any()) }
    }

    @Test
    fun onGuardarContrasena_conDatosValidos_restableceYMarcaExito() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")
        coEvery { repositorioMock.restablecerContrasena("observador@rinconsur.cl", "123456", "Abc12345.") } returns Unit

        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertTrue(viewModel.uiState.value.cambioExitoso)
        assertNull(viewModel.uiState.value.errorContrasenaNueva)
        assertNull(viewModel.uiState.value.errorConfirmarContrasena)
        coVerify(exactly = 1) { repositorioMock.restablecerContrasena("observador@rinconsur.cl", "123456", "Abc12345.") }
    }

    @Test
    fun onGuardarContrasena_conCodigoVencido400_muestraMensajeDelServidor() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")
        coEvery { repositorioMock.restablecerContrasena("observador@rinconsur.cl", "123456", "Abc12345.") } throws ApiException(400, "El código expiró")

        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertEquals("El código expiró", viewModel.uiState.value.errorContrasenaNueva)
        assertFalse(viewModel.uiState.value.cambioExitoso)
    }

    @Test
    fun onGuardarContrasena_sinConexion_muestraErrorInesperado() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")
        coEvery { repositorioMock.restablecerContrasena("observador@rinconsur.cl", "123456", "Abc12345.") } throws IOException("Sin conexión")

        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.errorContrasenaNueva)
        assertFalse(viewModel.uiState.value.cambioExitoso)
    }
}