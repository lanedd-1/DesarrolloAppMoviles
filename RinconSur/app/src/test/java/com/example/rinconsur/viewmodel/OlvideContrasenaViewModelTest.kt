package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.UsuarioRepository
import com.example.rinconsur.data.remote.CodigoResponse
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
class OlvideContrasenaViewModelTest {

    private lateinit var viewModel: OlvideContrasenaViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repositorioMock = mockk()
        viewModel = OlvideContrasenaViewModel(repositorioMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.olvide_error_correo_obligatorio) } returns "El correo es obligatorio"
        every { contextoMock.getString(R.string.olvide_error_correo_institucional) } returns "Debe ser un correo institucional (@RinconSur.cl)"
        every { contextoMock.getString(R.string.olvide_mensaje_codigo_enviado) } returns "Código enviado a tu correo"
        every { contextoMock.getString(R.string.olvide_error_cuenta_no_registrada) } returns "Esta cuenta no está registrada"
        every { contextoMock.getString(R.string.olvide_error_codigo_incompleto) } returns "Ingresa el código completo de 6 dígitos"
        every { contextoMock.getString(R.string.olvide_error_codigo_incorrecto) } returns "Código incorrecto"
        every { contextoMock.getString(R.string.error_inesperado) } returns "Ocurrió un error inesperado"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEnviarCodigo_conCorreoVacio_noLlamaAlServidorYMuestraError() {
        viewModel.onEnviarCodigo(contextoMock)

        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
        assertFalse(viewModel.uiState.value.codigoEnviado)
        coVerify(exactly = 0) { repositorioMock.solicitarCodigo(any()) }
    }

    @Test
    fun onEnviarCodigo_conCorreoSinDominio_noLlamaAlServidorYMuestraError() {
        viewModel.onCorreoCambia("alguien@gmail.com")

        viewModel.onEnviarCodigo(contextoMock)

        assertEquals("Debe ser un correo institucional (@RinconSur.cl)", viewModel.uiState.value.errorCorreo)
        assertFalse(viewModel.uiState.value.codigoEnviado)
        coVerify(exactly = 0) { repositorioMock.solicitarCodigo(any()) }
    }

    @Test
    fun onEnviarCodigo_conCorreoValido_marcaCodigoEnviadoYMuestraMensajeExito() {
        viewModel.onCorreoCambia("Observador@RinconSur.cl")
        coEvery {
            repositorioMock.solicitarCodigo("observador@rinconsur.cl")
        } returns CodigoResponse(mensaje = "Código enviado", codigo = null)

        viewModel.onEnviarCodigo(contextoMock)

        assertTrue(viewModel.uiState.value.codigoEnviado)
        assertNull(viewModel.uiState.value.errorCorreo)
        assertEquals("Código enviado a tu correo", viewModel.uiState.value.mensajeExito)
        coVerify(exactly = 1) { repositorioMock.solicitarCodigo("observador@rinconsur.cl") }
    }

    @Test
    fun onEnviarCodigo_conCuentaSuspendida403_bloqueaElEnvio() {
        viewModel.onCorreoCambia("suspendido@rinconsur.cl")
        coEvery { repositorioMock.solicitarCodigo("suspendido@rinconsur.cl") } throws ApiException(403, "Cuenta suspendida")

        viewModel.onEnviarCodigo(contextoMock)

        assertTrue(viewModel.uiState.value.mostrarDialogoSuspendido)
        assertFalse(viewModel.uiState.value.codigoEnviado)
    }

    @Test
    fun onEnviarCodigo_conCuentaNoRegistrada404_muestraError() {
        viewModel.onCorreoCambia("fantasma@rinconsur.cl")
        coEvery { repositorioMock.solicitarCodigo("fantasma@rinconsur.cl") } throws ApiException(404, "Usuario no encontrado")

        viewModel.onEnviarCodigo(contextoMock)

        assertEquals("Esta cuenta no está registrada", viewModel.uiState.value.errorCorreo)
        assertFalse(viewModel.uiState.value.codigoEnviado)
    }

    @Test
    fun onEnviarCodigo_sinConexion_muestraErrorInesperado() {
        viewModel.onCorreoCambia("observador@rinconsur.cl")
        coEvery { repositorioMock.solicitarCodigo("observador@rinconsur.cl") } throws IOException("Sin red")

        viewModel.onEnviarCodigo(contextoMock)

        assertEquals("Ocurrió un error inesperado", viewModel.uiState.value.errorCorreo)
        assertFalse(viewModel.uiState.value.codigoEnviado)
    }

    @Test
    fun onCodigoCambia_soloAceptaHasta6DigitosNumericos() {
        viewModel.onCodigoCambia("123456")
        assertEquals("123456", viewModel.uiState.value.codigoCorreo)

        viewModel.onCodigoCambia("1234567")
        assertEquals("123456", viewModel.uiState.value.codigoCorreo)

        viewModel.onCodigoCambia("12ab")
        assertEquals("123456", viewModel.uiState.value.codigoCorreo)
    }

    @Test
    fun onVerificarCodigo_conCodigoIncompleto_noLlamaAlServidorYMuestraError() {
        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onCodigoCambia("123")

        viewModel.onVerificarCodigo(contextoMock)

        assertEquals("Ingresa el código completo de 6 dígitos", viewModel.uiState.value.errorCodigo)
        assertFalse(viewModel.uiState.value.codigoVerificado)
        coVerify(exactly = 0) { repositorioMock.verificarCodigo(any(), any()) }
    }

    @Test
    fun onVerificarCodigo_conCodigoIncorrecto400_muestraError() {
        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onCodigoCambia("123456")
        coEvery { repositorioMock.verificarCodigo("observador@rinconsur.cl", "123456") } throws ApiException(400, "Código inválido")

        viewModel.onVerificarCodigo(contextoMock)

        assertEquals("Código incorrecto", viewModel.uiState.value.errorCodigo)
        assertFalse(viewModel.uiState.value.codigoVerificado)
    }

    @Test
    fun onVerificarCodigo_conCodigoCorrecto_marcaComoVerificado() {
        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onCodigoCambia("123456")
        coEvery { repositorioMock.verificarCodigo("observador@rinconsur.cl", "123456") } returns Unit

        viewModel.onVerificarCodigo(contextoMock)

        assertTrue(viewModel.uiState.value.codigoVerificado)
        assertNull(viewModel.uiState.value.errorCodigo)
    }
}