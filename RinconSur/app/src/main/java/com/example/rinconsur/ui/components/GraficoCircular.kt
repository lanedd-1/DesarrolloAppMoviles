package com.example.rinconsur.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.RinconSurTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gráfico circular animado con porcentajes dibujados en el centro de cada porción.
 */
@Composable
fun GraficoCircular(
    datos: List<Pair<String, Int>>,
    colores: List<Color>,
    modifier: Modifier = Modifier
) {
    val colorSeparador = MaterialTheme.colorScheme.surface
    val medidorTexto = rememberTextMeasurer()

    val progreso = remember(datos) { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Canvas(modifier = modifier.size(200.dp)) {
        val total = datos.sumOf { it.second }.toFloat()
        if (total == 0f) return@Canvas

        var anguloInicio = -90f // -90 = las 12 en punto
        val radioCentro = size.minDimension / 2f
        val centroX = size.width / 2f
        val centroY = size.height / 2f

        datos.forEachIndexed { indice, (_, valor) ->
            if (valor <= 0) return@forEachIndexed

            val proporcion = valor / total
            val barrido = proporcion * 360f * progreso.value
            val colorActual = colores[indice % colores.size]

            // 1. Trozo relleno de la torta
            drawArc(
                color = colorActual,
                startAngle = anguloInicio,
                sweepAngle = barrido,
                useCenter = true
            )

            // 2. Borde separador entre trozos
            drawArc(
                color = colorSeparador,
                startAngle = anguloInicio,
                sweepAngle = barrido,
                useCenter = true,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Dibujar porcentaje con drawText de Compose
            if (proporcion > 0.03f) {
                val anguloMedioRad = Math.toRadians((anguloInicio + (barrido / 2f)).toDouble())
                val distanciaTexto = radioCentro * 0.65f // Ubicación en el centro del sector
                val textoX = centroX + (distanciaTexto * cos(anguloMedioRad)).toFloat()
                val textoY = centroY + (distanciaTexto * sin(anguloMedioRad)).toFloat()

                val porcentajeTexto = "${(proporcion * 100).toInt()}%"
                val resultadoTexto = medidorTexto.measure(
                    text = porcentajeTexto,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                drawText(
                    textLayoutResult = resultadoTexto,
                    topLeft = Offset(
                        x = textoX - (resultadoTexto.size.width / 2f),
                        y = textoY - (resultadoTexto.size.height / 2f)
                    )
                )
            }

            anguloInicio += barrido
        }
    }
}

/** Leyenda: color + etiqueta + valor + porcentaje */
@Composable
fun LeyendaGrafico(
    datos: List<Pair<String, Int>>,
    colores: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = datos.sumOf { it.second }.coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        datos.forEachIndexed { indice, (etiqueta, valor) ->
            val porcentaje = valor * 100f / total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colores[indice % colores.size], CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$valor (${String.format("%.1f", porcentaje)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraficoCircularPreview() {
    val datosEjemplo = listOf(
        "CLIENTE" to 15,
        "TRANSITO" to 8,
        "OTRO" to 3
    )
    val coloresEjemplo = listOf(
        BurdeoRincon,
        Color(0xFFE65100),
        Color(0xFF2E7D32)
    )

    RinconSurTheme {
        Column(
            modifier = Modifier.size(300.dp, 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GraficoCircular(datos = datosEjemplo, colores = coloresEjemplo)
            LeyendaGrafico(datos = datosEjemplo, colores = coloresEjemplo)
        }
    }
}