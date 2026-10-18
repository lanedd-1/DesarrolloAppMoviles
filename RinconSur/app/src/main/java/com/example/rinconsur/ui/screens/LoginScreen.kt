package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.R
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit = {}
) {
    // Estado local visual (como enseña el profesor)
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Iniciar sesión",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BurdeoRincon
                )
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
            // Encabezado del login
            Column {
                Text(
                    text = "ACCESO INTERNO",
                    style = MaterialTheme.typography.labelSmall,
                    color = BurdeoRincon,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bienvenido/a",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Dimens.espacioMini))
                Text(
                    text = "Ingresa con tu cuenta para continuar con la operación de tienda.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Tarjeta principal del formulario
            TarjetaSeccion(titulo = "Tu cuenta de trabajo") {
                Spacer(modifier = Modifier.height(Dimens.espacioMini))

                // Campo Correo
                Column {
                    Text(
                        text = "Correo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(Dimens.espacioMini))
                    CampoTexto(
                        valor = correo,
                        onValorCambia = {
                            correo = it
                            errorCorreo = null
                        },
                        etiqueta = "Ingresa tu correo electrónico",
                        icono = R.drawable.ic_correo,
                        error = errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                // Campo Contraseña
                Column {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(Dimens.espacioMini))
                    CampoContrasena(
                        valor = contrasena,
                        onValorCambia = {
                            contrasena = it
                            errorContrasena = null
                        },
                        etiqueta = "Ingresa tu contraseña",
                        visible = contrasenaVisible,
                        onAlternarVisible = { contrasenaVisible = !contrasenaVisible },
                        error = errorContrasena
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                // Botón Iniciar Sesión
                BotonPrincipal(
                    texto = "Iniciar sesión",
                    onClick = {
                        // Validación básica como la del profesor
                        if (correo.isBlank()) {
                            errorCorreo = "El correo es obligatorio"
                        } else if (contrasena.isBlank()) {
                            errorContrasena = "La contraseña es obligatoria"
                        } else {
                            onLoginExitoso()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                Text(
                    text = "Accederás a las funciones de tu perfil asignado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Tarjeta de Ayuda / Soporte
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.cornerMedium))
                    .background(Color(0xFFF1F3F4))
                    .padding(Dimens.espacioMedio),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_correo),
                    contentDescription = null,
                    tint = BurdeoRincon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.espacioMedio))
                Column {
                    Text(
                        text = "¿Necesitas ayuda para ingresar?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Si no tienes cuenta u olvidaste tu contraseña, contacta a tu supervisor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.espacioChico))

            // Pie de página
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Observadores · Supervisores · Jefatura",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Muestreo operativo y flujo de tienda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    RinconSurTheme {
        LoginScreen()
    }
}