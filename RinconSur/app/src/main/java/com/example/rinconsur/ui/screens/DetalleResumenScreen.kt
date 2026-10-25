package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RegistroDetalleItem
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RangoEtarioEnum
import com.example.rinconsur.model.TipoCompaniaEnum
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.DetalleResumenViewModel

@Composable
fun DetalleResumenScreen(
    acceso: String,
    fecha: String,
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    viewModel: DetalleResumenViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var registroAEditar by remember { mutableStateOf<RegistroDetalleItem?>(null) }
    var registroAEliminar by remember { mutableStateOf<RegistroDetalleItem?>(null) }

    LaunchedEffect(acceso, fecha) {
        viewModel.cargarDetalles(contexto, acceso, fecha)
    }

    if (registroAEditar != null) {
        DialogoEditarRegistro(
            registro = registroAEditar!!,
            onDismiss = { registroAEditar = null },
            onGuardar = { clasificacion, rango, compania, bolsas ->
                viewModel.actualizarRegistro(contexto, registroAEditar!!.id, clasificacion, rango, compania, bolsas, registroAEditar!!.destino)
                registroAEditar = null
            },
            onEliminar = {
                registroAEliminar = registroAEditar
                registroAEditar = null
            }
        )
    }

    if (registroAEliminar != null) {
        AlertDialog(
            onDismissRequest = { registroAEliminar = null },
            title = { Text(stringResource(R.string.detalle_dialogo_eliminar_titulo), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.detalle_dialogo_eliminar_mensaje)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarRegistro(contexto, registroAEliminar!!.id)
                        registroAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.detalle_dialogo_eliminar_confirmar), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { registroAEliminar = null }) {
                    Text(stringResource(R.string.detalle_dialogo_eliminar_cancelar), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    DetalleResumenContent(
        uiState = uiState,
        onVolver = onVolver,
        onAbrirEdicion = { registroAEditar = it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleResumenContent(
    uiState: com.example.rinconsur.viewmodel.DetalleResumenUiState,
    onVolver: () -> Unit,
    onAbrirEdicion: (RegistroDetalleItem) -> Unit
) {
    val esTemaOscuro = isSystemInDarkTheme()
    val fondoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.surface else BurdeoRincon
    val colorContenidoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.onSurface else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.detalle_resumen_titulo),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorContenidoTopAppBar,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${uiState.acceso} · ${uiState.fecha}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorContenidoTopAppBar.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.detalle_resumen_cd_volver), tint = colorContenidoTopAppBar)
                    }
                },
                actions = {
                    LogoTopBarRinconSur()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = fondoTopAppBar,
                    titleContentColor = colorContenidoTopAppBar,
                    navigationIconContentColor = colorContenidoTopAppBar,
                    actionIconContentColor = colorContenidoTopAppBar
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioChico))

            // Tarjeta Superior (Total Muestras)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.espacioMedio).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = stringResource(R.string.detalle_resumen_total_muestras), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(text = "${uiState.totalRegistros}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = stringResource(R.string.detalle_resumen_validado), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(text = stringResource(R.string.detalle_resumen_historial_titulo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            if (uiState.registros.isEmpty()) {
                Text(
                    text = stringResource(R.string.detalle_resumen_sin_registros),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = Dimens.espacioMedio)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.registros) { registro ->
                        ItemRegistroDetalle(registro) { onAbrirEdicion(registro) }
                    }
                    item { Spacer(modifier = Modifier.height(Dimens.espacioGrande)) }
                }
            }
        }
    }
}

@Composable
private fun ItemRegistroDetalle(registro: RegistroDetalleItem, onEditar: () -> Unit) {
    val edadFormato = when(registro.rangoEtario) {
        "MENOR_18" -> stringResource(R.string.rango_menor_18)
        "RANGO_18_25" -> stringResource(R.string.rango_18_25)
        "RANGO_26_40" -> stringResource(R.string.rango_26_40)
        "RANGO_41_60" -> stringResource(R.string.rango_41_60)
        "MAYOR_60" -> stringResource(R.string.rango_mayor_60)
        else -> registro.rangoEtario
    }

    val companiaFormato = when(registro.compania) {
        "SOLO" -> stringResource(R.string.compania_solo)
        "PAREJA" -> stringResource(R.string.compania_pareja)
        "CON_NINOS" -> stringResource(R.string.compania_con_ninos)
        "GRUPO" -> stringResource(R.string.compania_grupo)
        else -> registro.compania
    }

    // Tarjetas del historial
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditar() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.detalle_item_hora, registro.hora), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = registro.clasificacion, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(Dimens.espacioChico))
                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.detalle_item_cd_editar), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.espacioChico), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.detalle_item_perfil), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(text = "$edadFormato · $companiaFormato", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.detalle_item_bolsas_ext), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(text = if (registro.bolsas) stringResource(R.string.sesion_curso_opcion_si) else stringResource(R.string.sesion_curso_opcion_no), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            if (registro.clasificacion == "TRANSITO" && !registro.destino.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(R.string.detalle_item_destino), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = registro.destino, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = Color(0xFFE65100))
                }
            }
        }
    }
}

@Composable
fun DialogoEditarRegistro(
    registro: RegistroDetalleItem,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, Boolean) -> Unit,
    onEliminar: () -> Unit
) {
    var clasificacionSeleccionada by remember { mutableStateOf(registro.clasificacion) }
    var rangoSeleccionado by remember { mutableStateOf(registro.rangoEtario) }
    var companiaSeleccionada by remember { mutableStateOf(registro.compania) }
    var traeBolsas by remember { mutableStateOf(registro.bolsas) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detalle_dialogo_editar_titulo), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                Text(stringResource(R.string.detalle_dialogo_editar_hora, registro.hora), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                Column {
                    Text(stringResource(R.string.sesion_curso_clasificacion), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ClasificacionPersona.entries.forEach { op ->
                            FiltroChipBasico(texto = op.etiqueta, seleccionado = clasificacionSeleccionada == op.name, onClick = { clasificacionSeleccionada = op.name })
                        }
                    }
                }

                Column {
                    Text(stringResource(R.string.sesion_curso_rango_etario), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        RangoEtarioEnum.entries.take(3).forEach { op ->
                            FiltroChipBasico(texto = op.etiqueta, seleccionado = rangoSeleccionado == op.name, onClick = { rangoSeleccionado = op.name })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        RangoEtarioEnum.entries.drop(3).forEach { op ->
                            FiltroChipBasico(texto = op.etiqueta, seleccionado = rangoSeleccionado == op.name, onClick = { rangoSeleccionado = op.name })
                        }
                    }
                }

                Column {
                    Text(stringResource(R.string.sesion_curso_compania), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TipoCompaniaEnum.entries.take(2).forEach { op ->
                            FiltroChipBasico(texto = op.etiqueta, seleccionado = companiaSeleccionada == op.name, onClick = { companiaSeleccionada = op.name })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        TipoCompaniaEnum.entries.drop(2).forEach { op ->
                            FiltroChipBasico(texto = op.etiqueta, seleccionado = companiaSeleccionada == op.name, onClick = { companiaSeleccionada = op.name })
                        }
                    }
                }

                Column {
                    Text(stringResource(R.string.detalle_dialogo_editar_bolsas), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FiltroChipBasico(texto = stringResource(R.string.sesion_curso_opcion_si), seleccionado = traeBolsas, onClick = { traeBolsas = true })
                        FiltroChipBasico(texto = stringResource(R.string.sesion_curso_opcion_no), seleccionado = !traeBolsas, onClick = { traeBolsas = false })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(clasificacionSeleccionada, rangoSeleccionado, companiaSeleccionada, traeBolsas) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.detalle_dialogo_editar_guardar), color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            IconButton(onClick = onEliminar) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.detalle_dialogo_editar_cd_eliminar), tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun FiltroChipBasico(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, heightDp = 800)
@Composable
private fun DetalleResumenScreenPreview() {
    RinconSurTheme {
        DetalleResumenContent(
            uiState = com.example.rinconsur.viewmodel.DetalleResumenUiState(
                acceso = "Acceso 1 - Galería Principal",
                fecha = "24/10/2026",
                totalRegistros = 12,
                registros = listOf(
                    RegistroDetalleItem(
                        id = 1L,
                        hora = "10:15",
                        clasificacion = "CLIENTE",
                        rangoEtario = "RANGO_26_40",
                        compania = "PAREJA",
                        bolsas = true,
                        destino = ""
                    ),
                    RegistroDetalleItem(
                        id = 2L,
                        hora = "10:30",
                        clasificacion = "TRANSITO",
                        rangoEtario = "RANGO_18_25",
                        compania = "SOLO",
                        bolsas = false,
                        destino = "Patio de Comidas"
                    )
                )
            ),
            onVolver = {},
            onAbrirEdicion = {}
        )
    }
}