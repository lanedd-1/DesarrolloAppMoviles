package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.UsuarioRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class FormularioViewModelTest {

    private lateinit var viewModel: FormularioViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repositorioMock = mockk()
        viewModel = FormularioViewModel(repositorioMock)
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.rol_observador) } returns "Observador de Pasillo"
        every { contextoMock.getString(R.string.rol_jefe_tienda) } returns "Jefe de Tienda"
        every { contextoMock.getString(R.string.rol_administrador) } returns "Administrador"

        every { contextoMock.getString(R.string.formulario_error_nombre_numeros) } returns "El nombre solo puede contener letras y espacios"
        every { contextoMock.getString(R.string.formulario_error_correo_institucional) } returns "Debe ser un correo institucional (@rinconsur.cl)"
        every { contextoMock.getString(R.string.formulario_error_no_coinciden) } returns "Las contraseñas no coinciden"
        every { contextoMock.getString(R.string.formulario_error_contrasena_formato) } returns "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun llenarFormularioValido() {
        viewModel.onNombreCambia("Jose Bustamante")
        viewModel.onCorreoCambia("JoseBustamante@rinconsur.cl")
        viewModel.onRolSeleccionado("Observador de Pasillo")
        viewModel.onContrasenaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")
    }

    @Test
    fun onRegistrar_conNombreConNumeros_noLlamaAlServidorYMuestraError() {
        llenarFormularioValido()

        viewModel.onNombreCambia("Jose123")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertFalse(viewModel.uiState.value.registroExitoso)
        assertEquals("El nombre solo puede contener letras y espacios", viewModel.uiState.value.errorNombre)
        coVerify(exactly = 0) { repositorioMock.registrar(any(), any(), any(), any()) }
    }

    @Test
    fun onRegistrar_conCorreoSinDominio_noRegistraYMuestraError() {
        llenarFormularioValido()
        viewModel.onCorreoCambia("JoseBustamante@gmail.com")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Debe ser un correo institucional (@rinconsur.cl)", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onRegistrar_conContrasenasDiferentes_noRegistraYMuestraError() {
        llenarFormularioValido()

        viewModel.onConfirmarContrasenaCambia("Diferente123.")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorConfirmarContrasena)
    }

    @Test
    fun onRegistrar_conContrasenaSinFormato_noRegistraYMuestraError() {
        llenarFormularioValido()

        viewModel.onContrasenaCambia("minusculasy5numero")
        viewModel.onConfirmarContrasenaCambia("minusculasy5numero")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial", viewModel.uiState.value.errorContrasena)
    }
}