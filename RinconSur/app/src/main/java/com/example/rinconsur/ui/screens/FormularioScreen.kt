package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.FormularioUiState
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.FormularioViewModel

@Composable
fun FormularioScreen(
    onRegistroExitoso: (Usuario) -> Unit = {},
    onVolver: () -> Unit = {},
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FormularioContent(
        uiState = uiState,
        onNombreCambia = viewModel::onNombreCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onRolSeleccionado = viewModel::onRolSeleccionado,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onConfirmarContrasenaCambia = viewModel::onConfirmarContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onRegistrar = {
            viewModel.onRegistrar()?.let { usuario ->
                onRegistroExitoso(usuario)
            }
        },
        onLimpiar = viewModel::onLimpiar,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onRolSeleccionado: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onVolver: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear Cuenta Colaborador",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(titulo = "Datos del Nuevo Usuario") {
                // 1. Campo Nombre Completo
                CampoTexto(
                    valor = uiState.nombre,
                    onValorCambia = onNombreCambia,
                    etiqueta = "Nombre completo",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorNombre
                )

                // 2. Campo Correo Corporativo
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = "Correo corporativo (@RinconSur.cl)",
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                // 3. Desplegable de Rol/Cargo
                var expandido by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = it }
                ) {
                    OutlinedTextField(
                        value = uiState.rolSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo / Rol asignado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        isError = uiState.errorRol != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        uiState.rolesDisponibles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol) },
                                onClick = {
                                    onRolSeleccionado(rol)
                                    expandido = false
                                }
                            )
                        }
                    }
                }
                uiState.errorRol?.let { mensaje ->
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = Dimens.espacioChico)
                    )
                }

                // 4. Campo Contraseña
                CampoContrasena(
                    valor = uiState.contrasena,
                    onValorCambia = onContrasenaCambia,
                    etiqueta = "Contraseña",
                    visible = uiState.contrasenaVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorContrasena
                )

                // 5. Campo Confirmar Contraseña
                CampoContrasena(
                    valor = uiState.confirmarContrasena,
                    onValorCambia = onConfirmarContrasenaCambia,
                    etiqueta = "Confirmar contraseña",
                    visible = uiState.contrasenaVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorConfirmarContrasena
                )
            }

            // Botones de Acción
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                BotonSecundario(
                    texto = "Limpiar Campos",
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = "Crear Cuenta",
                    onClick = onRegistrar,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.espacioMedio))
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, showSystemUi = true, name = "1. Registro Limpio")
@Composable
private fun FormularioLimpioPreview() {
    RinconSurTheme {
        FormularioContent(
            uiState = FormularioUiState(
                nombre = "Brandon Matias Recabarren Bustamante",
                correo = "BrandonRecabarren@RinconSur.cl",
                rolSeleccionado = "Observador de Pasillo"
            ),
            onNombreCambia = {}, onCorreoCambia = {}, onRolSeleccionado = {},
            onContrasenaCambia = {}, onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Registro Con Errores")
@Composable
private fun FormularioConErroresPreview() {
    RinconSurTheme {
        FormularioContent(
            uiState = FormularioUiState(
                correo = "usuario@gmail.com",
                errorNombre = "El nombre completo es obligatorio",
                errorCorreo = "Debe ser un correo institucional (@RinconSur.cl)",
                errorRol = "Debes seleccionar un rol para el colaborador",
                errorContrasena = "Mínimo 8 caracteres y 1 carácter especial",
                errorConfirmarContrasena = "Las contraseñas no coinciden"
            ),
            onNombreCambia = {}, onCorreoCambia = {}, onRolSeleccionado = {},
            onContrasenaCambia = {}, onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}