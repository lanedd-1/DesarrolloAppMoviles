package com.example.rinconsur.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
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

    LaunchedEffect(acceso, fecha) {
        viewModel.cargarDatos(contexto, acceso, fecha)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Resumen Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${uiState.acceso} · ${uiState.fecha}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = "Volver", tint = Color.White)
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

            // Tarjeta de Resumen Numérico
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
                        Text(text = "Total Verificados", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(text = "${uiState.totalMuestras}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = BurdeoRincon)
                    }
                }
            }

            Text(text = "Distribución de Flujo (Barras)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)

            // Gráfico de Barras seguro con Card estándar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = null
            ) {
                Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                    if (uiState.distribucionClasificacion.isEmpty()) {
                        Text("No hay datos suficientes para graficar.", color = Color.Gray, modifier = Modifier.padding(Dimens.espacioMedio))
                    } else {
                        GraficoBarrasNativo(datos = uiState.distribucionClasificacion)
                    }
                }
            }

            // NUEVO: Título y Tarjeta del Gráfico Circular (Torta)
            Text(text = "Distribución Porcentual (Torta)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        Text("No hay datos suficientes para graficar.", color = Color.Gray, modifier = Modifier.padding(Dimens.espacioMedio))
                    } else {
                        // Paleta de colores armónica para las porciones de la torta
                        val coloresTorta = listOf(
                            BurdeoRincon,
                            Color(0xFFE65100),
                            Color(0xFF2E7D32),
                            Color(0xFF1565C0),
                            Color(0xFF6A1B9A)
                        )

                        GraficoCircularNativo(
                            datos = uiState.distribucionClasificacion,
                            colores = coloresTorta
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LeyendaCircularNativa(
                            datos = uiState.distribucionClasificacion,
                            colores = coloresTorta
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoBarrasNativo(datos: List<Pair<String, Int>>) {
    // Calculamos el valor más alto para que esa barra sea el 100% de la altura (mínimo 1)
    val maxValor = datos.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.espacioMedio),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom // Alineamos todo hacia la base
    ) {
        datos.forEach { (categoria, valor) ->
            // Evitamos usar fillMaxHeight. Calculamos la altura exacta en DP. (Altura máxima = 150.dp)
            val alturaProporcional = (valor.toFloat() / maxValor.toFloat()).coerceIn(0.01f, 1f)
            val alturaBarra = 150.dp * alturaProporcional

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Número sobre la barra
                Text(text = valor.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BurdeoRincon)
                Spacer(modifier = Modifier.height(4.dp))

                // Barra visual dibujada con altura exacta
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(alturaBarra)
                        .background(BurdeoRincon, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Etiqueta abajo de la barra
                Text(
                    text = categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
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
            var anguloInicio = -90f // Comienza arriba
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
                    Text(text = categoria, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
                Text(text = "$valor", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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