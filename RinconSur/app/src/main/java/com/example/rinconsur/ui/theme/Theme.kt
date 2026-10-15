package com.example.rinconsur.ui.theme

import androidx.compose.material3.MaterialTheme
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

@Composable
fun RinconSurTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography, // Toma los estilos de Type.kt
        content = content
    )
}


