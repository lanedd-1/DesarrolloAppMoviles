package com.example.rinconsur.viewmodel

import android.content.Context
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var contextoMock: Context
    private lateinit var dbMock: RinconDbHelper

    @Before
    fun preparar() {
        viewModel = LoginViewModel()
        contextoMock = mockk(relaxed = true)
        dbMock = mockk(relaxed = true)


        every { contextoMock.getString(R.string.login_error_correo_obligatorio) } returns "El correo es obligatorio"
        every { contextoMock.getString(R.string.login_error_correo_institucional) } returns "Debe ingresar su correo corporativo (@RinconSur.cl)"
        every { contextoMock.getString(R.string.login_error_contrasena_obligatoria) } returns "La contraseña es obligatoria"
        every { contextoMock.getString(R.string.login_error_credenciales_incorrectas) } returns "Correo o contraseña incorrectos"
    }

    @Test
    fun onIngresar_conCamposVacios_devuelveFalsoYMuestraErrores() {
        val resultado = viewModel.onIngresar(contextoMock)

        assertFalse(resultado)
        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun onIngresar_conCorreoSinDominioCorporativo_devuelveFalsoYMuestraError() {

        viewModel.onCorreoCambia("cristopher@gmail.com")
        viewModel.onContrasenaCambia("Abc12345.")

        val resultado = viewModel.onIngresar(contextoMock)

        assertFalse(resultado)
        assertEquals("Debe ingresar su correo corporativo (@RinconSur.cl)", viewModel.uiState.value.errorCorreo)
        assertNull(viewModel.uiState.value.errorContrasena)
    }


    @Test
    fun onIngresar_conCorreoValidoYContrasenaVacia_muestraErrorSoloEnContrasena() {

        viewModel.onCorreoCambia("cristopher@rinconsur.cl")

        val resultado = viewModel.onIngresar(contextoMock)

        assertFalse(resultado)
        assertNull(viewModel.uiState.value.errorCorreo)
        assertEquals("La contraseña es obligatoria", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun onIngresar_conCredencialesIncorrectas_devuelveFalsoYMuestraError() {
        every { dbMock.validarLogin("observador@rinconsur.cl", "ClaveErronea") } returns false

        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onContrasenaCambia("ClaveErronea")

        val resultado = viewModel.onIngresar(contextoMock, dbMock)

        assertFalse(resultado)
        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.errorContrasena)
    }

}