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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
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
            if (viewModel.onIngresar(dbHelper)) {
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Iniciar Sesión",
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))

            TarjetaSeccion(titulo = "Credenciales de Acceso") {
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

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BurdeoRincon,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onOlvidasteContrasena() }
                        .padding(vertical = Dimens.espacioMini)
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMini))
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            BotonPrincipal(
                texto = "Iniciar Sesión",
                onClick = onIngresar,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(Dimens.espacioPantalla))
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, showSystemUi = true, name = "1. Login Limpio (Centrado)")
@Composable
private fun LoginLimpioPreview() {
    RinconSurTheme {
        LoginContent(
            uiState = LoginUiState(
                correo = "BrandonRecabarren@RinconSur.cl"
            ),
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {},
            onOlvidasteContrasena = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Login Con Errores (Centrado)")
@Composable
private fun LoginConErroresPreview() {
    RinconSurTheme {
        LoginContent(
            uiState = LoginUiState(
                correo = "usuario@gmail.com",
                errorCorreo = "Debe ser un correo institucional (@RinconSur.cl)",
                errorContrasena = "La contraseña es obligatoria"
            ),
            onCorreoCambia = {},
            onContrasenaCambia = {},
            onAlternarVisibilidad = {},
            onIngresar = {},
            onOlvidasteContrasena = {},
            onVolver = {}
        )
    }
}