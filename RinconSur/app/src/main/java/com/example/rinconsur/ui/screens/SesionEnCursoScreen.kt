package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.AccesoDestinoEnum
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RangoEtarioEnum
import com.example.rinconsur.model.SesionEnCursoUiState
import com.example.rinconsur.model.TipoCompaniaEnum
import com.example.rinconsur.ui.components.AvatarCircular
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.ui.theme.TerracotaRincon
import com.example.rinconsur.viewmodel.SesionEnCursoViewModel

@Composable
fun SesionEnCursoScreen(
    onVolver: () -> Unit = {},
    viewModel: SesionEnCursoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SesionEnCursoContent(
        uiState = uiState,
        onVolver = onVolver,
        onClasificacionSeleccionada = viewModel::onClasificacionSeleccionada,
        onRangoEtarioSeleccionado = viewModel::onRangoEtarioSeleccionado,
        onCompaniaSeleccionada = viewModel::onCompaniaSeleccionada,
        onBolsasOtraTiendaCambia = viewModel::onBolsasOtraTiendaCambia,
        onDestinoSeleccionado = viewModel::onDestinoSeleccionado,
        onGuardarRegistro = viewModel::onGuardarRegistro,
        onAnularUltimoRegistro = viewModel::onAnularUltimoRegistro
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SesionEnCursoContent(
    uiState: SesionEnCursoUiState,
    onVolver: () -> Unit,
    onClasificacionSeleccionada: (ClasificacionPersona) -> Unit,
    onRangoEtarioSeleccionado: (RangoEtarioEnum) -> Unit,
    onCompaniaSeleccionada: (TipoCompaniaEnum) -> Unit,
    onBolsasOtraTiendaCambia: (Boolean) -> Unit,
    onDestinoSeleccionado: (AccesoDestinoEnum) -> Unit,
    onGuardarRegistro: () -> Unit,
    onAnularUltimoRegistro: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sesión en Curso",
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
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    AvatarCircular(
                        imagen = R.drawable.ic_persona,
                        descripcion = "Observador",
                        modifier = Modifier
                            .padding(end = Dimens.espacioMedio)
                            .size(36.dp)
                    )
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
            // Cabecera informativa del acceso y cronómetro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFD32F2F))
                        )
                        Spacer(modifier = Modifier.width(Dimens.espacioMini))
                        Text(
                            text = "En curso · ${uiState.accesoNombre}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${uiState.ubicacionAcceso} · ${uiState.observadorNombre} · ${uiState.fecha}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "⏱ ${uiState.tiempoTranscurridoMin} min",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Banner informativo de sincronización offline
            if (uiState.guardadoLocalActivo) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.cornerSmall))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.espacioChico))
                    Text(
                        text = "Guardado local activo · Sin conexión",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Contenedor principal de captura de muestra rápida
            TarjetaSeccion(titulo = "Nuevo registro") {
                Text(
                    text = "Solo toca opciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(Dimens.espacioMini))

                // 1. Clasificación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clasificación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Obligatoria",
                        style = MaterialTheme.typography.bodySmall,
                        color = BurdeoRincon,
                        fontWeight = FontWeight.Bold
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    ClasificacionPersona.entries.forEach { opcion ->
                        val seleccionada = uiState.clasificacionSeleccionada == opcion
                        BotonOpcionTactil(
                            texto = opcion.etiqueta,
                            seleccionado = seleccionada,
                            onClick = { onClasificacionSeleccionada(opcion) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                // 2. Rango Etario
                Text(
                    text = "Rango etario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    RangoEtarioEnum.entries.forEach { rango ->
                        val seleccionada = uiState.rangoEtarioSeleccionado == rango
                        BotonOpcionTactil(
                            texto = rango.etiqueta,
                            seleccionado = seleccionada,
                            onClick = { onRangoEtarioSeleccionado(rango) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                // 3. Compañía
                Text(
                    text = "Compañía",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    TipoCompaniaEnum.entries.forEach { comp ->
                        val seleccionada = uiState.companiaSeleccionada == comp
                        BotonOpcionTactil(
                            texto = comp.etiqueta,
                            seleccionado = seleccionada,
                            onClick = { onCompaniaSeleccionada(comp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.espacioChico))

                // 4. Bolsas de otra tienda
                Text(
                    text = "¿Trae bolsas de otra tienda?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonOpcionTactil(
                        texto = "Sí",
                        seleccionado = uiState.traeBolsasOtraTienda,
                        onClick = { onBolsasOtraTiendaCambia(true) },
                        modifier = Modifier.weight(1f)
                    )
                    BotonOpcionTactil(
                        texto = "No",
                        seleccionado = !uiState.traeBolsasOtraTienda,
                        onClick = { onBolsasOtraTiendaCambia(false) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5. Destino (visible solo si es Tránsito según RN-08)
                if (uiState.clasificacionSeleccionada == ClasificacionPersona.TRANSITO) {
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Destino",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Solo si es Tránsito",
                            style = MaterialTheme.typography.bodySmall,
                            color = TerracotaRincon
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        AccesoDestinoEnum.entries.forEach { dest ->
                            val seleccionada = uiState.destinoSeleccionado == dest
                            BotonOpcionTactil(
                                texto = dest.codigo,
                                seleccionado = seleccionada,
                                onClick = { onDestinoSeleccionado(dest) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.espacioMedio))

                // Botón principal de guardado táctil
                BotonPrincipal(
                    texto = "Guardar registro",
                    onClick = onGuardarRegistro,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Contador de sesión y botón de anulación inmediata
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registros en esta sesión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.totalRegistrosSesion}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = BurdeoRincon
                        )
                    }

                    Text(
                        text = "Último: ${uiState.ultimoRegistroResumen}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (uiState.permiteAnular) {
                        BotonSecundario(
                            texto = "Anular último registro",
                            onClick = onAnularUltimoRegistro,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Text(
                text = "Sincronización pendiente. Los registros guardados requieren validación posterior del supervisor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = Dimens.espacioGrande)
            )
        }
    }
}

// Botón de opción táctil reutilizable dentro de la pantalla
@Composable
private fun BotonOpcionTactil(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.cornerSmall))
            .background(if (seleccionado) BurdeoRincon.copy(alpha = 0.12f) else Color.White)
            .border(
                width = if (seleccionado) 1.5.dp else 1.dp,
                color = if (seleccionado) BurdeoRincon else Color(0xFFD6D6D6),
                shape = RoundedCornerShape(Dimens.cornerSmall)
            )
            .clickable { onClick() }
            .padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (seleccionado) {
                Text(
                    text = "✓ ",
                    color = BurdeoRincon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                color = if (seleccionado) BurdeoRincon else Color(0xFF1A1C1E)
            )
        }
    }
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, showSystemUi = true, name = "1. Sesión en Curso - Tránsito")
@Composable
private fun SesionEnCursoTransitoPreview() {
    RinconSurTheme {
        SesionEnCursoContent(
            uiState = SesionEnCursoUiState(),
            onVolver = {},
            onClasificacionSeleccionada = {},
            onRangoEtarioSeleccionado = {},
            onCompaniaSeleccionada = {},
            onBolsasOtraTiendaCambia = {},
            onDestinoSeleccionado = {},
            onGuardarRegistro = {},
            onAnularUltimoRegistro = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. Sesión en Curso - Visita (Sin Destino)")
@Composable
private fun SesionEnCursoVisitaPreview() {
    RinconSurTheme {
        SesionEnCursoContent(
            uiState = SesionEnCursoUiState(
                clasificacionSeleccionada = ClasificacionPersona.VISITA,
                totalRegistrosSesion = 0,
                ultimoRegistroResumen = "Sin registros previos",
                permiteAnular = false
            ),
            onVolver = {},
            onClasificacionSeleccionada = {},
            onRangoEtarioSeleccionado = {},
            onCompaniaSeleccionada = {},
            onBolsasOtraTiendaCambia = {},
            onDestinoSeleccionado = {},
            onGuardarRegistro = {},
            onAnularUltimoRegistro = {}
        )
    }
}