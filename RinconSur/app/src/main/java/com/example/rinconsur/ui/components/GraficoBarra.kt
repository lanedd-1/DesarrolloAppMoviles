package com.example.rinconsur.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.RinconSurTheme

/**
 * Gráfico de barras dibujado con Canvas para Rincón Sur.
 */
@Composable
fun GraficoBarras(
    datos: List<Pair<String, Int>>,
    colores: List<Color>,
    modifier: Modifier = Modifier
) {
    val medidorTexto = rememberTextMeasurer()
    val colorTexto = MaterialTheme.colorScheme.onSurface
    val colorEje = MaterialTheme.colorScheme.outline

    // Animación: va de 0 a 1 cada vez que cambian los datos
    val progreso = remember(datos) { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (datos.isEmpty()) return@Canvas

        val margenSuperior = 20.dp.toPx()   // espacio para el número sobre la barra
        val margenInferior = 24.dp.toPx()   // espacio para la etiqueta de la categoría
        val altoDisponible = size.height - margenSuperior - margenInferior
        val baseY = size.height - margenInferior

        val valorMaximo = datos.maxOf { it.second }.coerceAtLeast(1)
        val anchoColumna = size.width / datos.size
        val anchoBarra = anchoColumna * 0.6f

        // Eje X
        drawLine(
            color = colorEje,
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 1.dp.toPx()
        )

        datos.forEachIndexed { indice, (etiqueta, valor) ->
            val altoBarra = altoDisponible * (valor.toFloat() / valorMaximo) * progreso.value
            val inicioColumna = indice * anchoColumna
            val x = inicioColumna + (anchoColumna - anchoBarra) / 2
            val y = baseY - altoBarra

            // Barra
            drawRoundRect(
                color = colores[indice % colores.size],
                topLeft = Offset(x, y),
                size = Size(anchoBarra, altoBarra),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // Valor sobre la barra
            val textoValor = medidorTexto.measure(
                text = valor.toString(),
                style = TextStyle(fontSize = 12.sp, color = colorTexto)
            )
            drawText(
                textLayoutResult = textoValor,
                topLeft = Offset(
                    x = x + (anchoBarra - textoValor.size.width) / 2,
                    y = y - textoValor.size.height - 2.dp.toPx()
                )
            )

            // Etiqueta bajo el eje
            val textoEtiqueta = medidorTexto.measure(
                text = etiqueta,
                style = TextStyle(fontSize = 12.sp, color = colorTexto)
            )
            drawText(
                textLayoutResult = textoEtiqueta,
                topLeft = Offset(
                    x = inicioColumna + (anchoColumna - textoEtiqueta.size.width) / 2,
                    y = baseY + 4.dp.toPx()
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraficoBarrasPreview() {
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
        GraficoBarras(
            datos = datosEjemplo,
            colores = coloresEjemplo,
            modifier = Modifier.fillMaxWidth()
        )
    }
}