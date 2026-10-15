package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.ui.theme.VerdeExito

//Cristopher Bustamante
@Composable
fun EtiquetaEstado(
    texto: String,
    modifier: Modifier = Modifier,
    colorFondo: Color = BurdeoRincon.copy(alpha = 0.15f),
    colorTexto: Color = BurdeoRincon
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colorFondo,
        contentColor = colorTexto
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(
                horizontal = Dimens.espacioMedio,
                vertical = Dimens.espacioChico
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EtiquetaEstadoPreview() {
    RinconSurTheme {
        EtiquetaEstado(
            texto = "Programada",
            modifier = Modifier.padding(16.dp)
        )
    }
}