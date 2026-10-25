package com.example.rinconsur.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.VerResumenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerResumenScreen(
    acceso: String,
    fecha: String,
    onVolver: () -> Unit,
    viewModel: VerResumenViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val esTemaOscuro = isSystemInDarkTheme()
    val fondoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.surface else BurdeoRincon
    val colorContenidoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.onSurface else Color.White

    // Lista de colores consistente para gráficos (Barras y Torta)
    val coloresGrafico = listOf(
        MaterialTheme.colorScheme.primary, // Primer ítem (TRANSITO -> Rosado/Burdeo pastel)
        Color(0xFFE65100),                  // Segundo ítem (VISITA -> Naranjo)
        Color(0xFF2E7D32),                  // Verde
        Color(0xFF1565C0),                  // Azul
        Color(0xFF6A1B9A)                   // Morado
    )

    LaunchedEffect(acceso, fecha) {
        viewModel.cargarDatos(contexto, acceso, fecha)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.ver_resumen_titulo), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colorContenidoTopAppBar)
                        Text("${uiState.acceso} · ${uiState.fecha}", style = MaterialTheme.typography.bodySmall, color = colorContenidoTopAppBar.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.ver_resumen_cd_volver), tint = colorContenidoTopAppBar)
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.espacioMedio).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = stringResource(R.string.ver_resumen_total_verificados), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(text = "${uiState.totalMuestras}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text(text = stringResource(R.string.ver_resumen_titulo_barras), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = null
            ) {
                Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                    if (uiState.distribucionClasificacion.isEmpty()) {
                        Text(stringResource(R.string.ver_resumen_sin_datos), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(Dimens.espacioMedio))
                    } else {
                        GraficoBarrasNativo(
                            datos = uiState.distribucionClasificacion,
                            colores = coloresGrafico
                        )
                    }
                }
            }

            Text(text = stringResource(R.string.ver_resumen_titulo_torta), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = null
            ) {
                Column(
                    modifier = Modifier
                        .padding(Dimens.espacioMedio)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    if (uiState.distribucionClasificacion.isEmpty()) {
                        Text(stringResource(R.string.ver_resumen_sin_datos), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(Dimens.espacioMedio))
                    } else {
                        GraficoCircularNativo(
                            datos = uiState.distribucionClasificacion,
                            colores = coloresGrafico
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LeyendaCircularNativa(
                            datos = uiState.distribucionClasificacion,
                            colores = coloresGrafico
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoBarrasNativo(datos: List<Pair<String, Int>>, colores: List<Color>) {
    val maxValor = datos.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.espacioMedio),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        datos.forEachIndexed { index, (categoria, valor) ->
            val alturaProporcional = (valor.toFloat() / maxValor.toFloat()).coerceIn(0.01f, 1f)
            val alturaBarra = 150.dp * alturaProporcional
            val colorActual = colores[index % colores.size]

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = valor.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colorActual)
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(alturaBarra)
                        .background(colorActual, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GraficoCircularNativo(datos: List<Pair<String, Int>>, colores: List<Color>) {
    val sumaTotal = datos.sumOf { it.second }.coerceAtLeast(1).toFloat()

    Box(
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var anguloInicio = -90f
            datos.forEachIndexed { index, (_, valor) ->
                val proporcion = valor.toFloat() / sumaTotal
                val anguloBarrido = proporcion * 360f
                val colorActual = colores[index % colores.size]

                drawArc(
                    color = colorActual,
                    startAngle = anguloInicio,
                    sweepAngle = anguloBarrido,
                    useCenter = true,
                    size = size
                )
                anguloInicio += anguloBarrido
            }
        }
    }
}

@Composable
fun LeyendaCircularNativa(datos: List<Pair<String, Int>>, colores: List<Color>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        datos.forEachIndexed { index, (categoria, valor) ->
            val colorActual = colores[index % colores.size]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colorActual, RoundedCornerShape(2.dp))
                    )
                    Text(text = categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(text = "$valor", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, heightDp = 900)
@Composable
private fun VerResumenScreenPreview() {
    RinconSurTheme {
        VerResumenScreen(
            acceso = "Acceso 1 - Galería Principal",
            fecha = "24/10/2026",
            onVolver = {}
        )
    }
}