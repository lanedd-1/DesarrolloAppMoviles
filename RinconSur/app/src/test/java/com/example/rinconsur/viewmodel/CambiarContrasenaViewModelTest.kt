package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CambiarContrasenaViewModelTest {

    private lateinit var viewModel: CambiarContrasenaViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        contextoMock = mockk(relaxed = true)
        repositorioMock = mockk()
        viewModel = CambiarContrasenaViewModel(repositorioMock)

        every { contextoMock.getString(R.string.cambiar_error_obligatoria) } returns "La contraseña es obligatoria"
        every { contextoMock.getString(R.string.cambiar_error_formato) } returns "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
        every { contextoMock.getString(R.string.cambiar_error_confirmar_obligatoria) } returns "Confirma la contraseña"
        every { contextoMock.getString(R.string.cambiar_error_no_coinciden) } returns "Las contraseñas no coinciden"
        every { contextoMock.getString(R.string.cambiar_error_cuenta_no_identificada) } returns "No se pudo identificar la cuenta, vuelve a iniciar el proceso"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun llenarContrasenasValidas() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")
    }

    @Test
    fun onGuardarContrasena_conContrasenaVacia_muestraErrorObligatoria() {
        viewModel.onContrasenaNuevaCambia("")
        viewModel.onConfirmarContrasenaCambia("")
        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertFalse(viewModel.uiState.value.cambioExitoso)
        assertEquals("La contraseña es obligatoria", viewModel.uiState.value.errorContrasenaNueva)
    }

    @Test
    fun onGuardarContrasena_conContrasenaSinFormato_muestraErrorFormato() {
        viewModel.onContrasenaNuevaCambia("12345678")
        viewModel.onConfirmarContrasenaCambia("12345678")
        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertFalse(viewModel.uiState.value.cambioExitoso)
        assertEquals("Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial", viewModel.uiState.value.errorContrasenaNueva)
    }

    @Test
    fun onGuardarContrasena_conContrasenasDiferentes_muestraErrorNoCoinciden() {
        viewModel.onContrasenaNuevaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Diferente123.")
        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertFalse(viewModel.uiState.value.cambioExitoso)
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorConfirmarContrasena)
    }

    @Test
    fun onGuardarContrasena_conDatosVaciosIdentificador_muestraErrorCuentaNoIdentificada() {
        llenarContrasenasValidas()
        viewModel.onGuardarContrasena("", "123456", contextoMock)

        assertFalse(viewModel.uiState.value.cambioExitoso)
        assertEquals("No se pudo identificar la cuenta, vuelve a iniciar el proceso", viewModel.uiState.value.errorContrasenaNueva)
    }

    @Test
    fun onGuardarContrasena_conDatosValidos_restableceContrasenaYActualizaEstadoExitoso() {
        coEvery {
            repositorioMock.restablecerContrasena("observador@rinconsur.cl", "123456", "Abc12345.")
        } returns mockk(relaxed = true)

        llenarContrasenasValidas()
        viewModel.onGuardarContrasena("observador@rinconsur.cl", "123456", contextoMock)

        assertTrue(viewModel.uiState.value.cambioExitoso)
    }
}