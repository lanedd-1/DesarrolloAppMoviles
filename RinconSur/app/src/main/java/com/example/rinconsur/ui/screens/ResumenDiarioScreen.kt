package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.FlujoAcceso
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.viewmodel.ResumenDiarioUiState
import com.example.rinconsur.viewmodel.ResumenDiarioViewModel

@Composable
fun ResumenDiarioScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    onIrVerResumen: (String, String) -> Unit,
    onIrEditarResumen: (String, String) -> Unit,
    viewModel: ResumenDiarioViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuarioActual) {
        viewModel.cargarDatos(contexto, usuarioActual)
    }

    ResumenDiarioContent(
        uiState = uiState,
        onVolver = onVolver,
        onFechaSeleccionada = { nuevaFecha -> viewModel.seleccionarFecha(contexto, nuevaFecha) },
        onIrVerResumen = onIrVerResumen,
        onIrEditarResumen = onIrEditarResumen
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenDiarioContent(
    uiState: ResumenDiarioUiState,
    onVolver: () -> Unit,
    onFechaSeleccionada: (String) -> Unit,
    onIrVerResumen: (String, String) -> Unit,
    onIrEditarResumen: (String, String) -> Unit
) {
    var desplegarMenuFechas by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Métricas Rincón Sur", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Rincón Sur", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = Dimens.espacioMedio)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = uiState.iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = uiState.rolUsuario, style = MaterialTheme.typography.labelSmall, color = BurdeoRincon, fontWeight = FontWeight.Bold)
                    Text(text = uiState.nombreUsuario, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = "Sincronizado", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .clickable { desplegarMenuFechas = true }
                        .padding(Dimens.espacioMedio)
                ) {
                    Icon(painter = painterResource(android.R.drawable.ic_menu_today), contentDescription = null, tint = BurdeoRincon, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(Dimens.espacioChico))
                    Text(text = uiState.labelFecha, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(painter = painterResource(android.R.drawable.arrow_down_float), contentDescription = null, tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = desplegarMenuFechas,
                    onDismissRequest = { desplegarMenuFechas = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    uiState.fechasDisponibles.forEach { fechaOpcion ->
                        DropdownMenuItem(
                            text = { Text(text = fechaOpcion, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                desplegarMenuFechas = false
                                onFechaSeleccionada(fechaOpcion)
                            }
                        )
                    }
                }
            }

            EtiquetaSeccion(etiqueta = "P-01", titulo = "Conversión total")
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = null) {
                Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${uiState.totalIngresos}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(Dimens.espacioChico))
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(text = "Ingresos · ${uiState.totalAccesos} accesos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    Text(
                        text = "Ingresos de sensores. Las boletas son de toda la tienda, no de una puerta.",
                        style = MaterialTheme.typography.bodySmall, color = Color.Gray
                    )
                }
            }

            EtiquetaSeccion(etiqueta = "P-02", titulo = "Estimación por accesos")
            Text(
                text = "Sensores: ingresos medidos. Tránsito: proyección a partir de muestras validadas, no conteo exacto.",
                style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = Dimens.espacioChico)
            )

            if (uiState.flujos.isEmpty()) {
                Text("No hay registros para esta fecha.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Dimens.espacioMedio))
            } else {
                uiState.flujos.forEach { flujo ->
                    TarjetaAcceso(
                        flujo = flujo,
                        fechaSeleccionada = uiState.fechaSeleccionada,
                        onIrVer = onIrVerResumen,
                        onIrEditar = onIrEditarResumen
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(Dimens.espacioMedio)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(painter = painterResource(android.R.drawable.ic_dialog_info), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(Dimens.espacioChico))
                    Column {
                        Text(text = "La conversión depurada requiere más datos", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Debe descontar el tránsito estimado en todos los accesos. No se calcula con solo A4 y A1.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun EtiquetaSeccion(etiqueta: String, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Dimens.espacioChico)) {
        Box(modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
            Text(text = etiqueta, style = MaterialTheme.typography.labelSmall, color = BurdeoRincon, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(Dimens.espacioChico))
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TarjetaAcceso(
    flujo: FlujoAcceso,
    fechaSeleccionada: String,
    onIrVer: (String, String) -> Unit,
    onIrEditar: (String, String) -> Unit
) {
    val sigla = flujo.acceso.replace("Acceso ", "")
    val nombrePuerta = when(sigla) {
        "A1" -> "Pasillo Mall"
        "A2" -> "Entrada Norte"
        "A3" -> "Estacionamiento"
        "A4" -> "Conexión Ferretería"
        else -> "Puerta Principal"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.espacioMedio),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = null
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = sigla, style = MaterialTheme.typography.titleMedium, color = BurdeoRincon, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(Dimens.espacioMedio))
                Text(text = nombrePuerta, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(Dimens.espacioMedio))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Ingresos de sensor", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "${flujo.cantidad}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(Dimens.espacioMedio))

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Button(
                    onClick = { onIrVer(flujo.acceso, fechaSeleccionada) },
                    colors = ButtonDefaults.buttonColors(containerColor = BurdeoRincon),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver resumen", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onIrEditar(flujo.acceso, fechaSeleccionada) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BurdeoRincon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BurdeoRincon),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar resumen", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}