package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(stringResource(R.string.detalle_dialogo_eliminar_confirmar), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { registroAEliminar = null }) {
                    Text(stringResource(R.string.detalle_dialogo_eliminar_cancelar), color = Color.Gray, fontWeight = FontWeight.Bold)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.detalle_resumen_titulo),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${uiState.acceso} · ${uiState.fecha}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.detalle_resumen_cd_volver), tint = Color.White)
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
                .padding(horizontal = Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioChico))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.espacioMedio).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = stringResource(R.string.detalle_resumen_total_muestras), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(text = "${uiState.totalRegistros}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = BurdeoRincon)
                    }
                    Box(modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = stringResource(R.string.detalle_resumen_validado), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(text = stringResource(R.string.detalle_resumen_historial_titulo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)

            if (uiState.registros.isEmpty()) {
                Text(
                    text = stringResource(R.string.detalle_resumen_sin_registros),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
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

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditar() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.detalle_item_hora, registro.hora), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BurdeoRincon)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = registro.clasificacion, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(Dimens.espacioChico))
                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.detalle_item_cd_editar), tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.espacioChico), color = Color(0xFFF0F0F0))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.detalle_item_perfil), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = "$edadFormato · $companiaFormato", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.detalle_item_bolsas_ext), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = if (registro.bolsas) stringResource(R.string.sesion_curso_opcion_si) else stringResource(R.string.sesion_curso_opcion_no), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }

            if (registro.clasificacion == "TRANSITO" && !registro.destino.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(R.string.detalle_item_destino), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                Text(stringResource(R.string.detalle_dialogo_editar_hora, registro.hora), style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
                colors = ButtonDefaults.buttonColors(containerColor = BurdeoRincon)
            ) {
                Text(stringResource(R.string.detalle_dialogo_editar_guardar))
            }
        },
        dismissButton = {
            IconButton(onClick = onEliminar) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.detalle_dialogo_editar_cd_eliminar), tint = Color.Red)
            }
        }
    )
}

@Composable
fun FiltroChipBasico(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (seleccionado) BurdeoRincon else Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) Color.White else Color.DarkGray,
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