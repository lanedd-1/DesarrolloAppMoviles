package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.R
import com.example.rinconsur.ui.components.AvatarCircular
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSesionesScreen(
    onIniciarSesionConteo: (String) -> Unit = {},
    onAbrirMenu: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Sesiones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Abrir menú",
                            tint = Color.White
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
            // Encabezado con foto y datos del colaborador
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.espacioMedio),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarCircular(
                        imagen = R.drawable.ic_persona,
                        descripcion = "Foto de perfil Tamara"
                    )
                    Spacer(modifier = Modifier.width(Dimens.espacioMedio))
                    Column {
                        Text(
                            text = "Tamara R.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Observador de Pasillo · Turno Mañana",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Indicadores rápidos del día (TarjetaEstadistica x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TarjetaEstadistica(
                        valor = "2 / 4",
                        etiqueta = "Sesiones Hoy"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TarjetaEstadistica(
                        valor = "142",
                        etiqueta = "Muestras Registradas"
                    )
                }
            }

            // Lista de Sesiones Programadas
            TarjetaSeccion(titulo = "Sesiones Asignadas") {
                // Item de Sesión 1: Activa / Pendiente
                ItemSesion(
                    puerta = "Acceso 3 - Estacionamiento",
                    horario = "11:00 - 11:30 hrs",
                    estado = "Pendiente",
                    esActiva = true,
                    onIniciar = { onIniciarSesionConteo("Acceso 3") }
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                // Item de Sesión 2: Finalizada
                ItemSesion(
                    puerta = "Acceso 1 - Galería Principal",
                    horario = "09:30 - 10:00 hrs",
                    estado = "Finalizada",
                    esActiva = false,
                    onIniciar = {}
                )
            }
        }
    }
}

@Composable
private fun ItemSesion(
    puerta: String,
    horario: String,
    estado: String,
    esActiva: Boolean,
    onIniciar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esActiva) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = puerta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EtiquetaEstado(
                    texto = estado,
                    colorFondo = if (esActiva) BurdeoRincon.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                    colorTexto = if (esActiva) BurdeoRincon else Color.DarkGray
                )
            }

            Text(
                text = "Bloque: $horario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (esActiva) {
                Spacer(modifier = Modifier.height(Dimens.espacioChico))
                BotonPrincipal(
                    texto = "Iniciar conteo en puerta",
                    onClick = onIniciar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ----------------------------- PREVIEW -----------------------------

@Preview(showBackground = true, showSystemUi = true, heightDp = 800)
@Composable
private fun MisSesionesScreenPreview() {
    RinconSurTheme {
        MisSesionesScreen()
    }
}