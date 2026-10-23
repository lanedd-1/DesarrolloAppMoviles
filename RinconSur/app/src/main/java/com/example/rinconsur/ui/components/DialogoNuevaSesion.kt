package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun DialogoNuevaSesion(
    onSeleccionarAcceso: (String) -> Unit,
    onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva Sesión de Conteo", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Selecciona el acceso asignado:", modifier = Modifier.padding(bottom = Dimens.espacioMedio))
                val accesosDisponibles = listOf("Acceso A1", "Acceso A2", "Acceso A3", "Acceso A4")

                accesosDisponibles.forEach { acceso ->
                    TextButton(onClick = { onSeleccionarAcceso(acceso) }, modifier = Modifier.fillMaxWidth()) {
                        Text(acceso, color = BurdeoRincon, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCancelar) { Text("Cancelar", color = Color.Gray) } }
    )
}