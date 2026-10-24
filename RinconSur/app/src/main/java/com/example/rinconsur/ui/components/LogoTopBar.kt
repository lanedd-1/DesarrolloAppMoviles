package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.R
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme

@Composable
fun LogoTopBarRinconSur(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_rinconsur),
        contentDescription = "Logo Rincón Sur",
        tint = Color.Unspecified, // Mantiene los colores originales de la imagen
        modifier = modifier
            .padding(end = Dimens.espacioGrande)
            .size(42.dp)
    )
}

// ----------------------------- PREVIEWS -----------------------------

@Preview(showBackground = true, name = "1. Solo el Logo")
@Composable
private fun LogoTopBarSoloPreview() {
    RinconSurTheme {
        LogoTopBarRinconSur()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "2. Logo en TopAppBar Completo")
@Composable
private fun LogoTopBarEnHeaderPreview() {
    RinconSurTheme {
        TopAppBar(
            title = {
                Text(
                    text = "Rincón Sur",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = "Menú",
                        tint = Color.White
                    )
                }
            },
            actions = {
                LogoTopBarRinconSur()
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BurdeoRincon)
        )
    }
}