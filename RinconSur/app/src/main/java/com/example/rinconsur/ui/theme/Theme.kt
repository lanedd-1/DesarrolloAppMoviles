package com.example.rinconsur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//BRANDON BANDA

private val LightColorScheme = lightColorScheme(
    primary = BurdeoRincon,
    secondary = TerracotaRincon,
    background = FondoGris,
    surface = BlancoTarjeta,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
    error = RojoAlerta
)


private val DarkColorScheme = darkColorScheme(
    primary = BurdeoRincon,
    secondary = TerracotaRincon,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFFFFFFF),
    error = RojoAlerta
)

@Composable
fun RinconSurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val Azul = Color(0xFF1E5EFF)

// Colores para las barras y los trozos del gráfico circular
val PaletaGraficos = listOf(
    Color(0xFF1E5EFF), // azul
    Color(0xFFF59F00), // amarillo
    Color(0xFF12B886), // verde
    Color(0xFFE64980), // rosado
    Color(0xFF7950F2), // violeta
    Color(0xFF15AABF)  // celeste
)


@Composable
fun GraficosAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}



