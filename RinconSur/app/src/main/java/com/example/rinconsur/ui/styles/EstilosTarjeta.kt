package com.example.rinconsur.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.example.rinconsur.ui.theme.Dimens

// =====================================================================
//  EstilosTarjeta.kt — Estilo común para todas las Card de Rincón Sur
// =====================================================================

object EstilosTarjeta {

    val forma: Shape = RoundedCornerShape(Dimens.radioTarjeta)

    @Composable
    fun colores(): CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    @Composable
    fun elevacion(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = Dimens.elevacionTarjeta
    )
}