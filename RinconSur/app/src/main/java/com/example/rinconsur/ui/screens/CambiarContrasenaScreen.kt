package com.example.rinconsur.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.CambiarContrasenaUiState
import com.example.rinconsur.viewmodel.CambiarContrasenaViewModel

@Composable
fun CambiarContrasenaScreen(
    correoUsuario: String = "",
    onCambioExitoso: () -> Unit = {},
    onVolver: () -> Unit = {},
    viewModel: CambiarContrasenaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val dbHelper = remember { RinconDbHelper(contexto) }

    CambiarContrasenaContent(
        uiState = uiState,
        onContrasenaNuevaCambia = viewModel::onContrasenaNuevaCambia,
        onConfirmarContrasenaCambia = viewModel::onConfirmarContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onGuardarContrasena = {
            if (viewModel.onGuardarContrasena(correoUsuario, dbHelper, contexto)) {
                onCambioExitoso()
            }
        },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarContrasenaContent(
    uiState: CambiarContrasenaUiState,
    onContrasenaNuevaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onGuardarContrasena: () -> Unit,
    onVolver: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.cambiar_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cambiar_cd_volver),
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

            TarjetaSeccion(titulo = stringResource(R.string.cambiar_seccion_titulo)) {
                Text(
                    text = stringResource(R.string.cambiar_instrucciones),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                CampoContrasena(
                    valor = uiState.contrasenaNueva,
                    onValorCambia = onContrasenaNuevaCambia,
                    etiqueta = stringResource(R.string.cambiar_etiqueta_nueva),
                    visible = uiState.contrasenaVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorContrasenaNueva
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                CampoContrasena(
                    valor = uiState.confirmarContrasena,
                    onValorCambia = onConfirmarContrasenaCambia,
                    etiqueta = stringResource(R.string.cambiar_etiqueta_confirmar),
                    visible = uiState.contrasenaVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorConfirmarContrasena
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMini))
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            BotonPrincipal(
                texto = stringResource(R.string.cambiar_boton_guardar),
                onClick = onGuardarContrasena,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "1. Cambiar Clave Limpio")
@Composable
private fun CambiarContrasenaLimpioPreview() {
    RinconSurTheme {
        CambiarContrasenaContent(
            uiState = CambiarContrasenaUiState(),
            onContrasenaNuevaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onGuardarContrasena = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Cambiar Clave Errores")
@Composable
private fun CambiarContrasenaErroresPreview() {
    RinconSurTheme {
        CambiarContrasenaContent(
            uiState = CambiarContrasenaUiState(
                contrasenaNueva = "rin",
                confirmarContrasena = "12345",
                errorContrasenaNueva = "Mínimo 8 caracteres, 1 mayuscula , 1 minuscula y 1 caracter especial",
                errorConfirmarContrasena = "Las contraseñas no coinciden"
            ),
            onContrasenaNuevaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onGuardarContrasena = {},
            onVolver = {}
        )
    }
}