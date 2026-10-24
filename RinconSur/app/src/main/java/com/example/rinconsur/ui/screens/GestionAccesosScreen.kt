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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.GestionAccesosUiState
import com.example.rinconsur.ui.components.BannerNotificacion
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.GestionAccesosViewModel
import kotlinx.coroutines.delay

@Composable
fun GestionAccesosScreen(
    onVolver: () -> Unit,
    viewModel: GestionAccesosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarDatos(contexto)
    }

    LaunchedEffect(uiState.mensajeExito) {
        if (uiState.mensajeExito != null) {
            delay(3000)
            viewModel.limpiarMensaje()
        }
    }

    GestionAccesosContent(
        uiState = uiState,
        onVolver = onVolver,
        onCambiarEstado = { acceso, estado ->
            viewModel.onCambiarEstadoAcceso(contexto, acceso, estado)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAccesosContent(
    uiState: GestionAccesosUiState,
    onVolver: () -> Unit,
    onCambiarEstado: (String, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gestion_accesos_titulo), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.gestion_accesos_cd_volver), tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            if (uiState.mensajeExito != null) {
                BannerNotificacion(
                    mensaje = uiState.mensajeExito,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (!uiState.esJefeTienda) {
                VistaAccesoDenegado(onVolver)
            } else {
                Text(
                    text = stringResource(R.string.gestion_accesos_seccion_titulo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.gestion_accesos_instrucciones),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                uiState.estadoAccesosMap.forEach { (acceso, habilitado) ->
                    ItemOpcionAcceso(
                        accesoNombre = acceso,
                        habilitado = habilitado,
                        onCambiarEstado = { nuevo -> onCambiarEstado(acceso, nuevo) }
                    )
                }
            }
        }
    }
}

// ----------------------------- COMPONENTES PRIVADOS -----------------------------

@Composable
private fun ItemOpcionAcceso(
    accesoNombre: String,
    habilitado: Boolean,
    onCambiarEstado: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.espacioMedio),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = accesoNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = if (habilitado) stringResource(R.string.gestion_accesos_estado_habilitado) else stringResource(R.string.gestion_accesos_estado_cerrado),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (habilitado) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Switch(
                checked = habilitado,
                onCheckedChange = onCambiarEstado,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BurdeoRincon
                )
            )
        }
    }
}

@Composable
private fun VistaAccesoDenegado(onVolver: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(
                text = stringResource(R.string.gestion_accesos_restringido_titulo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = stringResource(R.string.gestion_accesos_restringido_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            BotonPrincipal(texto = stringResource(R.string.gestion_accesos_boton_volver), onClick = onVolver, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GestionAccesosPreview() {
    RinconSurTheme {
        GestionAccesosContent(
            uiState = GestionAccesosUiState(
                estadoAccesosMap = mapOf(
                    "Acceso A1" to true,
                    "Acceso A2" to true,
                    "Acceso A3" to false,
                    "Acceso A4" to true
                ),
                esJefeTienda = true,
                mensajeExito = "Estado de Acceso A3 actualizado"
            ),
            onVolver = {},
            onCambiarEstado = { _, _ -> }
        )
    }
}