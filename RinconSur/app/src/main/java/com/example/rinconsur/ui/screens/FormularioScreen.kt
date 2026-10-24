package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.FormularioUiState
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.BannerNotificacion
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.DialogoCerrarSesion
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.FormularioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FormularioScreen(
    usuarioActual: Usuario? = null,
    onRegistroExitoso: (Usuario) -> Unit = {},
    onVolver: () -> Unit = {},
    onIrMisSesiones: () -> Unit = {},
    onIrCrearCuenta: () -> Unit = {},
    onIrGestionAccesos: () -> Unit = {},
    onIrResumenDiario: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(usuarioActual) {
        viewModel.inicializarRolesSegunUsuario(usuarioActual, contexto)
    }

    LaunchedEffect(uiState.mensajeExito) {
        if (uiState.mensajeExito != null) {
            delay(6000)
            viewModel.limpiarMensaje()
        }
    }

    FormularioContent(
        usuarioActual = usuarioActual,
        uiState = uiState,
        onNombreCambia = viewModel::onNombreCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onRolSeleccionado = viewModel::onRolSeleccionado,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onConfirmarContrasenaCambia = viewModel::onConfirmarContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onRegistrar = { viewModel.onRegistrar(contexto)?.let { usuario -> onRegistroExitoso(usuario) } },
        onLimpiar = viewModel::onLimpiar,
        onIrMisSesiones = onIrMisSesiones,
        onIrCrearCuenta = onIrCrearCuenta,
        onIrGestionAccesos = onIrGestionAccesos,
        onIrResumenDiario = onIrResumenDiario,
        onCerrarSesion = onCerrarSesion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    usuarioActual: Usuario?,
    uiState: FormularioUiState,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onRolSeleccionado: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onIrMisSesiones: () -> Unit = {},
    onIrCrearCuenta: () -> Unit = {},
    onIrGestionAccesos: () -> Unit = {},
    onIrResumenDiario: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        DialogoCerrarSesion(
            onConfirmar = {
                mostrarDialogo = false
                onCerrarSesion()
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.login_subtitulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.gestion_accesos_boton_volver)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onIrMisSesiones() },
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                )

                if (usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.gestion_accesos_titulo)) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrGestionAccesos() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR || usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
                    NavigationDrawerItem(
                        label = { Text("Resumen del Día") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrResumenDiario() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.formulario_titulo)) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() }; onIrCrearCuenta() },
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; mostrarDialogo = true },
                    modifier = Modifier.padding(Dimens.espacioChico)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.formulario_titulo), color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = stringResource(R.string.formulario_cd_menu), tint = Color.White)
                        }
                    },
                    actions = {
                        LogoTopBarRinconSur()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BurdeoRincon)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                if (uiState.mensajeExito != null) {
                    BannerNotificacion(
                        mensaje = uiState.mensajeExito,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TarjetaSeccion(titulo = stringResource(R.string.formulario_seccion_datos)) {
                    CampoTexto(valor = uiState.nombre, onValorCambia = onNombreCambia, etiqueta = stringResource(R.string.formulario_etiqueta_nombre), icono = R.drawable.ic_persona, error = uiState.errorNombre)
                    CampoTexto(valor = uiState.correo, onValorCambia = onCorreoCambia, etiqueta = stringResource(R.string.formulario_etiqueta_correo), icono = R.drawable.ic_correo, error = uiState.errorCorreo, tipoTeclado = KeyboardType.Email)

                    var expandido by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                        OutlinedTextField(
                            value = uiState.rolSeleccionado, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.formulario_etiqueta_rol)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                            isError = uiState.errorRol != null, modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            uiState.rolesDisponibles.forEach { rol ->
                                DropdownMenuItem(text = { Text(rol) }, onClick = { onRolSeleccionado(rol); expandido = false })
                            }
                        }
                    }
                    uiState.errorRol?.let { mensaje -> Text(text = mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = Dimens.espacioChico)) }

                    CampoContrasena(valor = uiState.contrasena, onValorCambia = onContrasenaCambia, etiqueta = stringResource(R.string.formulario_etiqueta_contrasena), visible = uiState.contrasenaVisible, onAlternarVisible = onAlternarVisibilidad, error = uiState.errorContrasena)
                    CampoContrasena(valor = uiState.confirmarContrasena, onValorCambia = onConfirmarContrasenaCambia, etiqueta = stringResource(R.string.formulario_etiqueta_confirmar), visible = uiState.contrasenaVisible, onAlternarVisible = onAlternarVisibilidad, error = uiState.errorConfirmarContrasena)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    BotonSecundario(texto = stringResource(R.string.formulario_boton_limpiar), onClick = onLimpiar, modifier = Modifier.weight(1f))
                    BotonPrincipal(texto = stringResource(R.string.formulario_boton_crear), onClick = onRegistrar, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, showSystemUi = true, name = "1. Registro Limpio (Jefe de Tienda)")
@Composable
private fun FormularioLimpioPreview() {
    RinconSurTheme {
        FormularioContent(
            usuarioActual = Usuario(
                id = 1L,
                nombre = "Rodrigo Saavedra",
                correo = "supervisor@rinconsur.cl",
                contrasena = "",
                rolId = Rol.ID_JEFE_TIENDA,
                rolNombre = "Jefe de Tienda"
            ),
            uiState = FormularioUiState(
                nombre = "Cristopher Bustamante",
                correo = "cbustamante@rinconsur.cl",
                rolesDisponibles = listOf("Observador de Pasillo", "Jefe de Tienda"),
                rolSeleccionado = "Observador de Pasillo"
            ),
            onNombreCambia = {},
            onCorreoCambia = {},
            onRolSeleccionado = {},
            onContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onRegistrar = {},
            onLimpiar = {},
            onIrMisSesiones = {},
            onIrCrearCuenta = {},
            onIrGestionAccesos = {},
            onIrResumenDiario = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Registro Exitoso con Banner")
@Composable
private fun FormularioExitosoPreview() {
    RinconSurTheme {
        FormularioContent(
            usuarioActual = Usuario(
                id = 1L,
                nombre = "Rodrigo Saavedra",
                correo = "supervisor@rinconsur.cl",
                contrasena = "",
                rolId = Rol.ID_JEFE_TIENDA,
                rolNombre = "Jefe de Tienda"
            ),
            uiState = FormularioUiState(
                nombre = "Cristopher Bustamante",
                correo = "cbustamante@rinconsur.cl",
                rolesDisponibles = listOf("Observador de Pasillo", "Jefe de Tienda"),
                rolSeleccionado = "Observador de Pasillo",
                mensajeExito = "Cuenta creada para Cristopher Bustamante con éxito!"
            ),
            onNombreCambia = {},
            onCorreoCambia = {},
            onRolSeleccionado = {},
            onContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onRegistrar = {},
            onLimpiar = {},
            onIrMisSesiones = {},
            onIrCrearCuenta = {},
            onIrGestionAccesos = {},
            onIrResumenDiario = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "3. Registro Con Errores")
@Composable
private fun FormularioConErroresPreview() {
    RinconSurTheme {
        FormularioContent(
            usuarioActual = Usuario(
                id = 1L,
                nombre = "Rodrigo Saavedra",
                correo = "supervisor@rinconsur.cl",
                contrasena = "",
                rolId = Rol.ID_JEFE_TIENDA,
                rolNombre = "Jefe de Tienda"
            ),
            uiState = FormularioUiState(
                correo = "usuario@gmail.com",
                errorNombre = "El nombre completo es obligatorio",
                errorCorreo = "Debe ser un correo institucional (@rinconsur.cl)",
                errorRol = "Debes seleccionar un rol para el colaborador",
                errorContrasena = "Mínimo 8 caracteres",
                errorConfirmarContrasena = "Las contraseñas no coinciden"
            ),
            onNombreCambia = {},
            onCorreoCambia = {},
            onRolSeleccionado = {},
            onContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onRegistrar = {},
            onLimpiar = {},
            onIrMisSesiones = {},
            onIrCrearCuenta = {},
            onIrGestionAccesos = {},
            onIrResumenDiario = {},
            onCerrarSesion = {}
        )
    }
}