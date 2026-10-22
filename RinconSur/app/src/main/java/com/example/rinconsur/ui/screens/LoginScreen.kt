package com.example.rinconsur.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit = {},
    onOlvidasteContrasena: () -> Unit = {},
    onVolver: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val dbHelper = remember { RinconDbHelper(contexto) }

    LoginContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = {
            if (viewModel.onIngresar(contexto)) {
                onLoginExitoso()
            }
        },
        onOlvidasteContrasena = onOlvidasteContrasena,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onOlvidasteContrasena: () -> Unit = {},
    onVolver: () -> Unit = {}
) {
    // 1. Scaffold limpio sin Drawer ni menú hamburguesa
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Rincón Sur",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                // 2. Quitamos el navigationIcon (la hamburguesa) por seguridad
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
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            TarjetaSeccion(titulo = "Credenciales de Acceso") {
                Spacer(modifier = Modifier.height(Dimens.espacioMini))

                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = "Correo corporativo (@RinconSur.cl)",
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                CampoContrasena(
                    valor = uiState.contrasena,
                    onValorCambia = onContrasenaCambia,
                    etiqueta = "Contraseña",
                    visible = uiState.contrasenaVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorContrasena
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(onClick = { /* Acción para recuperar contraseña */ }) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodySmall,
                            color = BurdeoRincon,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.espacioMini))

                BotonPrincipal(
                    texto = "Iniciar Sesión",
                    onClick = onIngresar,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    RinconSurTheme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {},
            onOlvidasteContrasena = {},
            onVolver = {}
        )
    }
}