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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// =====================================================================
//  Pruebas unitarias — LoginViewModelTest.kt
//   - Validación de campos vacíos y de correo institucional.
//   - Credenciales incorrectas: el servidor (UsuarioRepository simulado
//     con MockK) responde 401 y el ViewModel muestra el error.
// =====================================================================

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var contextoMock: Context
    private lateinit var repositorioMock: UsuarioRepository

    @Before
    fun preparar() {
        // El ViewModel usa viewModelScope, que necesita un "hilo principal" en las pruebas
        Dispatchers.setMain(UnconfinedTestDispatcher())

        contextoMock = mockk(relaxed = true)
        repositorioMock = mockk()
        viewModel = LoginViewModel(repositorioMock)

        every { contextoMock.getString(R.string.login_error_correo_obligatorio) } returns "El correo es obligatorio"
        every { contextoMock.getString(R.string.login_error_correo_institucional) } returns "Debe ingresar su correo corporativo (@RinconSur.cl)"
        every { contextoMock.getString(R.string.login_error_contrasena_obligatoria) } returns "La contraseña es obligatoria"
        every { contextoMock.getString(R.string.login_error_credenciales_incorrectas) } returns "Correo o contraseña incorrectos"
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
    }

    @Test
    fun onIngresar_conCorreoSinDominioCorporativo_muestraError() {
        viewModel.onCorreoCambia("cristopher@gmail.com")
        viewModel.onContrasenaCambia("Abc12345.")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("Debe ingresar su correo corporativo (@RinconSur.cl)", viewModel.uiState.value.errorCorreo)
        assertNull(viewModel.uiState.value.errorContrasena)
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
    fun onIngresar_conCredencialesIncorrectas_muestraErrorEnContrasena() {
        coEvery {
            repositorioMock.iniciarSesion("observador@rinconsur.cl", "ClaveErronea")
        } throws ApiException(401, "Correo o contraseña incorrectos")

        viewModel.onCorreoCambia("observador@rinconsur.cl")
        viewModel.onContrasenaCambia("ClaveErronea")

        viewModel.onIngresar(contextoMock)

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.errorContrasena)
    }
}