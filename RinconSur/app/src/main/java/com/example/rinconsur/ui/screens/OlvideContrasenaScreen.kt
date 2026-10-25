package com.example.rinconsur.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.permisos.Permisos
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.DialogoCuentaSuspendida
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.OlvideContrasenaUiState
import com.example.rinconsur.viewmodel.OlvideContrasenaViewModel

@Composable
fun OlvideContrasenaScreen(
    onCodigoVerificado: (String) -> Unit = {},
    onVolver: () -> Unit = {},
    viewModel: OlvideContrasenaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val permisoNoti = Permisos.NOTIFICACIONES

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
        }
    }

    LaunchedEffect(Unit) {
        if (permisoNoti.isNotEmpty() && !Permisos.tiene(contexto, permisoNoti)) {
            permisoLauncher.launch(permisoNoti)
        }
    }

    OlvideContrasenaContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        onCerrarDialogoSuspendido = viewModel::onCerrarDialogoSuspendido, // <-- NUEVO
        onEnviarCodigo = {
            if (permisoNoti.isEmpty() || Permisos.tiene(contexto, permisoNoti)) {
                viewModel.onEnviarCodigo(contexto)
            } else {
                Permisos.abrirAjustes(contexto)
            }
        },
        onVerificarCodigo = {
            if (viewModel.onVerificarCodigo(contexto)) {
                onCodigoVerificado(uiState.correo)
            }
        },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OlvideContrasenaContent(
    uiState: OlvideContrasenaUiState,
    onCorreoCambia: (String) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onCerrarDialogoSuspendido: () -> Unit,
    onEnviarCodigo: () -> Unit,
    onVerificarCodigo: () -> Unit,
    onVolver: () -> Unit = {}
) {
    if (uiState.mostrarDialogoSuspendido) {
        DialogoCuentaSuspendida(
            onEntendido = onCerrarDialogoSuspendido
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.olvide_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.olvide_cd_volver),
                            tint = Color.White
                        )
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))

            TarjetaSeccion(titulo = stringResource(R.string.olvide_seccion_validacion)) {
                Text(
                    text = stringResource(R.string.olvide_instrucciones),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = stringResource(R.string.olvide_etiqueta_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                if (uiState.codigoEnviado && uiState.mensajeExito != null) {
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Text(
                        text = uiState.mensajeExito,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                    CampoTexto(
                        valor = uiState.codigoCorreo,
                        onValorCambia = onCodigoCambia,
                        etiqueta = stringResource(R.string.olvide_etiqueta_codigo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCodigo,
                        tipoTeclado = KeyboardType.Number
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.espacioMini))
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            if (!uiState.codigoEnviado) {
                BotonPrincipal(
                    texto = stringResource(R.string.olvide_boton_enviar),
                    onClick = onEnviarCodigo,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else {
                BotonPrincipal(
                    texto = stringResource(R.string.olvide_boton_verificar),
                    onClick = onVerificarCodigo,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "1. Pantalla Inicial Correo")
@Composable
private fun OlvideContrasenaLimpioPreview() {
    RinconSurTheme {
        OlvideContrasenaContent(
            uiState = OlvideContrasenaUiState(),
            onCorreoCambia = {},
            onCodigoCambia = {},
            onCerrarDialogoSuspendido = {},
            onEnviarCodigo = {},
            onVerificarCodigo = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Con Código Enviado por Correo")
@Composable
private fun OlvideContrasenaEnviadoPreview() {
    RinconSurTheme {
        OlvideContrasenaContent(
            uiState = OlvideContrasenaUiState(
                correo = "brandon@rinconsur.cl",
                codigoEnviado = true,
                mensajeExito = "Código enviado al correo"
            ),
            onCorreoCambia = {},
            onCodigoCambia = {},
            onCerrarDialogoSuspendido = {},
            onEnviarCodigo = {},
            onVerificarCodigo = {},
            onVolver = {}
        )
    }
}