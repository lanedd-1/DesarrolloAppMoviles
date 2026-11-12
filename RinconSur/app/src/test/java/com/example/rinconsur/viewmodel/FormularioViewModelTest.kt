package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FormularioViewModelTest {

    private lateinit var viewModel: FormularioViewModel
    private lateinit var contextoMock: Context

    @Before
    fun preparar() {
        viewModel = FormularioViewModel()
        contextoMock = mockk(relaxed = true)

        every { contextoMock.getString(R.string.rol_observador) } returns "Observador de Pasillo"
        every { contextoMock.getString(R.string.rol_jefe_tienda) } returns "Jefe de Tienda"
        every { contextoMock.getString(R.string.rol_administrador) } returns "Administrador"

        every { contextoMock.getString(R.string.formulario_error_nombre_numeros) } returns "El nombre solo puede contener letras y espacios"
        every { contextoMock.getString(R.string.formulario_error_correo_institucional) } returns "Debe ser un correo institucional (@rinconsur.cl)"
        every { contextoMock.getString(R.string.formulario_error_no_coinciden) } returns "Las contraseñas no coinciden"
        every { contextoMock.getString(R.string.formulario_error_contrasena_formato) } returns "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
    }

    private fun llenarFormularioValido() {
        viewModel.onNombreCambia("Jose Bustamante")
        viewModel.onCorreoCambia("JoseBustamante@rinconsur.cl")
        viewModel.onRolSeleccionado("Observador de Pasillo")
        viewModel.onContrasenaCambia("Abc12345.")
        viewModel.onConfirmarContrasenaCambia("Abc12345.")
    }

    @Test
    fun onRegistrar_conNombreConNumeros_devuelveNullYMuestraError() {
        llenarFormularioValido()

        viewModel.onNombreCambia("Jose123")

        val resultado = viewModel.onRegistrar(contextoMock)

        assertNull(resultado)
        assertEquals("El nombre solo puede contener letras y espacios", viewModel.uiState.value.errorNombre)
    }

    @Test
    fun onRegistrar_conCorreoSinDominio_devuelveNullYMuestraError() {

        llenarFormularioValido()
        viewModel.onCorreoCambia("JoseBustamante@gmail.com")

        val resultado = viewModel.onRegistrar(contextoMock)

        assertNull(resultado)
        assertEquals("Debe ser un correo institucional (@rinconsur.cl)", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onRegistrar_conContrasenasDiferentes_devuelveNullYMuestraError() {
        llenarFormularioValido()

        viewModel.onConfirmarContrasenaCambia("Diferente123.")

        val resultado = viewModel.onRegistrar(contextoMock)

        assertNull(resultado)
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorConfirmarContrasena)
    }

    @Test
    fun onRegistrar_conContrasenaSinFormato_devuelveNullYMuestraError() {

        llenarFormularioValido()

        viewModel.onContrasenaCambia("minusculasy5numero")
        viewModel.onConfirmarContrasenaCambia("minusculasy5numero")

        val resultado = viewModel.onRegistrar(contextoMock)

        assertNull(resultado)
        assertEquals("Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial", viewModel.uiState.value.errorContrasena)
    }
}