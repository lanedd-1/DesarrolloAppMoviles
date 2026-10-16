package com.example.rinconsur.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.TerracotaRincon

//Cristopher Bustamante

// Extensión para aplicar el alto estandarizado de los botones
fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.buttonHeight)

object EstilosBoton {
    // Forma y borde redondeado
    val forma = RoundedCornerShape(Dimens.cornerSmall)

    // Relleno interno
    val relleno = PaddingValues(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)

    // Estilos de colores para Botón Principal (Burdeo)
    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = BurdeoRincon,
        contentColor = Color.White,
        disabledContainerColor = BurdeoRincon.copy(alpha = 0.5f),
        disabledContentColor = Color.White.copy(alpha = 0.7f)
    )

    // Estilos de colores para Boton Secundario
    @Composable
    fun coloresSecundario(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = TerracotaRincon
    )

    // Borde para el Botón Secundario
    @Composable
    fun bordeSecundario(): BorderStroke = BorderStroke(1.dp, TerracotaRincon)

    // Sombra o elevación sutil para el boton principal
    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = 2.dp,
        pressedElevation = 4.dp,
        disabledElevation = 0.dp
    )
}