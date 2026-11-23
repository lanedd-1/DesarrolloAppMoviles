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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

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

        every { contextoMock.getString(R.string.formulario_error_nombre_obligatorio) } returns "El nombre es obligatorio"
        every { contextoMock.getString(R.string.formulario_error_nombre_largo) } returns "El nombre no puede superar los 50 caracteres"
        every { contextoMock.getString(R.string.formulario_error_nombre_numeros) } returns "El nombre solo puede contener letras y espacios"
        every { contextoMock.getString(R.string.formulario_error_correo_obligatorio) } returns "El correo es obligatorio"
        every { contextoMock.getString(R.string.formulario_error_correo_largo) } returns "El correo no puede superar los 50 caracteres"
        every { contextoMock.getString(R.string.formulario_error_correo_invalido) } returns "Correo electrónico inválido"
        every { contextoMock.getString(R.string.formulario_error_correo_institucional) } returns "Debe ser un correo institucional (@rinconsur.cl)"
        every { contextoMock.getString(R.string.formulario_error_rol_obligatorio) } returns "Debe seleccionar un rol"
        every { contextoMock.getString(R.string.formulario_error_contrasena_obligatoria) } returns "La contraseña es obligatoria"
        every { contextoMock.getString(R.string.formulario_error_contrasena_formato) } returns "Mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 carácter especial"
        every { contextoMock.getString(R.string.formulario_error_confirmar_obligatoria) } returns "Debe confirmar la contraseña"
        every { contextoMock.getString(R.string.formulario_error_no_coinciden) } returns "Las contraseñas no coinciden"
        every { contextoMock.getString(R.string.formulario_mensaje_exito, any()) } returns "Usuario registrado correctamente"
        every { contextoMock.getString(R.string.formulario_error_correo_existente) } returns "Este correo ya está registrado"
        every { contextoMock.getString(R.string.formulario_error_creacion_fallida) } returns "No se pudo crear el usuario"
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
    fun inicializarRolesSegunUsuario_comoAdministrador_habilitaLosTresRoles() {
        val adminFicticio = mockk<Usuario>(relaxed = true) {
            every { rolId } returns Rol.ID_ADMINISTRADOR
        }

        viewModel.inicializarRolesSegunUsuario(adminFicticio, contextoMock)

        assertEquals(
            listOf("Observador de Pasillo", "Jefe de Tienda", "Administrador"),
            viewModel.uiState.value.rolesDisponibles
        )
        assertEquals("Observador de Pasillo", viewModel.uiState.value.rolSeleccionado)
    }

    @Test
    fun inicializarRolesSegunUsuario_comoJefeTienda_ocultaRolAdministrador() {
        val jefeFicticio = mockk<Usuario>(relaxed = true) {
            every { rolId } returns Rol.ID_JEFE_TIENDA
        }

        viewModel.inicializarRolesSegunUsuario(jefeFicticio, contextoMock)

        assertEquals(
            listOf("Observador de Pasillo", "Jefe de Tienda"),
            viewModel.uiState.value.rolesDisponibles
        )
        assertFalse(viewModel.uiState.value.rolesDisponibles.contains("Administrador"))
    }

    @Test
    fun onRegistrar_conCamposVacios_muestraTodosLosErroresObligatorios() {
        viewModel.onRolSeleccionado("")

        viewModel.onRegistrar(contextoMock)

        assertFalse(viewModel.uiState.value.registroExitoso)
        assertEquals("El nombre es obligatorio", viewModel.uiState.value.errorNombre)
        assertEquals("El correo es obligatorio", viewModel.uiState.value.errorCorreo)
        assertEquals("Debe seleccionar un rol", viewModel.uiState.value.errorRol)
        assertEquals("La contraseña es obligatoria", viewModel.uiState.value.errorContrasena)
        assertEquals("Debe confirmar la contraseña", viewModel.uiState.value.errorConfirmarContrasena)
        coVerify(exactly = 0) { repositorioMock.registrar(any(), any(), any(), any()) }
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
    fun onRegistrar_conCorreoSinArroba_muestraErrorCorreoInvalido() {
        llenarFormularioValido()
        viewModel.onCorreoCambia("josebustamante.rinconsur.cl")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Correo electrónico inválido", viewModel.uiState.value.errorCorreo)
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
    fun onRegistrar_sinRolSeleccionado_noRegistraYMuestraError() {
        llenarFormularioValido()
        viewModel.onRolSeleccionado("")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Debe seleccionar un rol", viewModel.uiState.value.errorRol)
        coVerify(exactly = 0) { repositorioMock.registrar(any(), any(), any(), any()) }
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

    @Test
    fun onRegistrar_conContrasenasDiferentes_noRegistraYMuestraError() {
        llenarFormularioValido()
        viewModel.onConfirmarContrasenaCambia("Diferente123.")

        viewModel.onRegistrar(contextoMock)

        assertNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorConfirmarContrasena)
    }

    @Test
    fun onRegistrar_conDatosValidos_creaUsuarioYMarcaRegistroExitoso() {
        val usuarioFicticio = mockk<Usuario>(relaxed = true)
        coEvery {
            repositorioMock.registrar(
                "Jose Bustamante",
                "josebustamante@rinconsur.cl",
                "Abc12345.",
                Rol.ID_OBSERVADOR
            )
        } returns usuarioFicticio

        llenarFormularioValido()

        viewModel.onRegistrar(contextoMock)

        assertTrue(viewModel.uiState.value.registroExitoso)
        assertNotNull(viewModel.uiState.value.usuarioCreado)
        assertEquals("Usuario registrado correctamente", viewModel.uiState.value.mensajeExito)
        coVerify(exactly = 1) {
            repositorioMock.registrar(
                "Jose Bustamante",
                "josebustamante@rinconsur.cl",
                "Abc12345.",
                Rol.ID_OBSERVADOR
            )
        }
    }

    @Test
    fun onRegistrar_conCorreoDuplicado409_muestraErrorCorreoExistente() {
        coEvery {
            repositorioMock.registrar(any(), any(), any(), any())
        } throws ApiException(409, "Conflicto")

        llenarFormularioValido()

        viewModel.onRegistrar(contextoMock)

        assertFalse(viewModel.uiState.value.registroExitoso)
        assertEquals("Este correo ya está registrado", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onRegistrar_sinConexion_muestraErrorCreacionFallida() {
        coEvery {
            repositorioMock.registrar(any(), any(), any(), any())
        } throws IOException("Sin conexión")

        llenarFormularioValido()

        viewModel.onRegistrar(contextoMock)

        assertFalse(viewModel.uiState.value.registroExitoso)
        assertEquals("No se pudo crear el usuario", viewModel.uiState.value.errorCorreo)
    }
}