package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.OlvideContrasenaUiState
import com.example.rinconsur.viewmodel.OlvideContrasenaViewModel

@Composable
fun OlvideContrasenaScreen(
    onCodigoVerificado: () -> Unit = {},
    onVolver: () -> Unit = {},
    viewModel: OlvideContrasenaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OlvideContrasenaContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        onEnviarCodigo = { viewModel.onEnviarCodigo() },
        onVerificarCodigo = {
            if (viewModel.onVerificarCodigo()) {
                onCodigoVerificado()
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
    onEnviarCodigo: () -> Unit,
    onVerificarCodigo: () -> Unit,
    onVolver: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recuperar Contraseña",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver), // O tu ícono de volver
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))

            TarjetaSeccion(titulo = "Validación de Identidad") {
                Text(
                    text = "Ingresa tu correo corporativo para enviarte un código SMS de verificación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))


                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = "Correo corporativo (@RinconSur.cl)",
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )


                if (uiState.codigoEnviado && uiState.mensajeExito != null) {
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Text(
                        text = uiState.mensajeExito,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32), // Verde éxito
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimens.espacioMedio))


                    CampoTexto(
                        valor = uiState.codigoSms,
                        onValorCambia = onCodigoCambia,
                        etiqueta = "Ingresa código SMS (ej: 4892)",
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCodigo,
                        tipoTeclado = KeyboardType.Number
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.espacioMini))
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            // Botones Dinámicos
            if (!uiState.codigoEnviado) {
                BotonPrincipal(
                    texto = "Enviar Código SMS",
                    onClick = onEnviarCodigo,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else {
                BotonPrincipal(
                    texto = "Verificar Código",
                    onClick = onVerificarCodigo,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "1. Pantalla Inicial Olvide Contraseña")
@Composable
private fun OlvideContrasenaLimpioPreview() {
    RinconSurTheme {
        OlvideContrasenaContent(
            uiState = OlvideContrasenaUiState(),
            onCorreoCambia = {},
            onCodigoCambia = {},
            onEnviarCodigo = {},
            onVerificarCodigo = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Con Código SMS Enviado")
@Composable
private fun OlvideContrasenaEnviadoPreview() {
    RinconSurTheme {
        OlvideContrasenaContent(
            uiState = OlvideContrasenaUiState(
                correo = "brandon@rinconsur.cl",
                codigoEnviado = true,
                mensajeExito = "¡Código SMS enviado a tu teléfono asociado!"
            ),
            onCorreoCambia = {},
            onCodigoCambia = {},
            onEnviarCodigo = {},
            onVerificarCodigo = {},
            onVolver = {}
        )
    }
}