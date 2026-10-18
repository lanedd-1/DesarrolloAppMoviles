package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.R
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme

//JOSE RECABARREN
@Composable
fun EncabezadoUsuario(
    rol: String,
    nombreUsuario: String,
    iniciales: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.espacioChico),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = rol.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = BurdeoRincon,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hola, $nombreUsuario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        AvatarCircular(
            imagen = R.drawable.ic_persona, // o la imagen de perfil que tengas en drawable
            descripcion = "Foto de perfil"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EncabezadoUsuarioPreview() {
    RinconSurTheme {
        EncabezadoUsuario(
            rol = "Perfil Observador",
            nombreUsuario = "Tamara R.",
            iniciales = "TR",
            modifier = Modifier.padding(16.dp)
        )
    }
}