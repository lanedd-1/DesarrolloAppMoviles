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
class OlvideContrasenaViewModelTest {

    private lateinit var viewModel: OlvideContrasenaViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        contextoMock = mockk(relaxed = true)
        repositorioMock = mockk()
        viewModel = OlvideContrasenaViewModel(repositorioMock)

        every { contextoMock.getString(R.string.olvide_error_correo_obligatorio) } returns "El correo es obligatorio"
        every { contextoMock.getString(R.string.olvide_error_correo_institucional) } returns "Debe ser un correo institucional (@rinconsur.cl)"
        every { contextoMock.getString(R.string.olvide_error_cuenta_no_registrada) } returns "Esta cuenta no está registrada"
        every { contextoMock.getString(R.string.olvide_mensaje_codigo_enviado) } returns "¡Revisa la notificación en tu dispositivo! Código enviado."
        every { contextoMock.getString(R.string.olvide_error_codigo_incompleto) } returns "Ingresa el código completo de 6 dígitos"
        every { contextoMock.getString(R.string.olvide_error_codigo_incorrecto) } returns "Código incorrecto"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEnviarCodigo_conCorreoVacio_muestraErrorCorreoObligatorio() {
        viewModel.onCorreoCambia("")
        viewModel.onEnviarCodigo(contextoMock)

        assertFalse(viewModel.uiState.value.codigoEnviado)
        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onEnviarCodigo_conCorreoSinDominioCorporativo_muestraErrorInstitucional() {
        viewModel.onCorreoCambia("usuario@gmail.com")
        viewModel.onEnviarCodigo(contextoMock)

        assertFalse(viewModel.uiState.value.codigoEnviado)
        assertEquals("Debe ser un correo institucional (@rinconsur.cl)", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onEnviarCodigo_conCorreoValidoNoRegistrado_muestraErrorCuentaNoRegistrada() {
        coEvery {
            repositorioMock.solicitarCodigo("noexistente@rinconsur.cl")
        } throws ApiException(404, "No encontrado")

        viewModel.onCorreoCambia("noexistente@rinconsur.cl")
        viewModel.onEnviarCodigo(contextoMock)

        assertFalse(viewModel.uiState.value.codigoEnviado)
        assertEquals("Esta cuenta no está registrada", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onEnviarCodigo_conCuentaSuspendida_muestraDialogoSuspendido() {
        coEvery {
            repositorioMock.solicitarCodigo("suspendido@rinconsur.cl")
        } throws ApiException(403, "Cuenta suspendida")

        viewModel.onCorreoCambia("suspendido@rinconsur.cl")
        viewModel.onEnviarCodigo(contextoMock)

        assertFalse(viewModel.uiState.value.codigoEnviado)
        assertTrue(viewModel.uiState.value.mostrarDialogoSuspendido)
    }

    @Test
    fun onEnviarCodigo_conCorreoValido_enviaCodigoYActualizaEstado() {
        coEvery {
            repositorioMock.solicitarCodigo("observador@rinconsur.cl")
        } returns mockk(relaxed = true)

        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onEnviarCodigo(contextoMock)

        assertTrue(viewModel.uiState.value.codigoEnviado)
        assertEquals(contextoMock.getString(R.string.olvide_mensaje_codigo_enviado), viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun onVerificarCodigo_conCodigoIncompleto_muestraErrorCodigoIncompleto() {
        viewModel.onCodigoCambia("12345")
        viewModel.onVerificarCodigo(contextoMock)

        assertFalse(viewModel.uiState.value.codigoVerificado)
        assertEquals("Ingresa el código completo de 6 dígitos", viewModel.uiState.value.errorCodigo)
    }
}