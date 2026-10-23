package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun DialogoNuevaSesion(
    accesosCerrados: List<String>,
    onSeleccionarAcceso: (String) -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = "Nueva Sesión de Conteo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Selecciona el acceso asignado:",
                    modifier = Modifier.padding(bottom = Dimens.espacioMedio)
                )
                val accesosDisponibles = listOf("Acceso A1", "Acceso A2", "Acceso A3", "Acceso A4")

                accesosDisponibles.forEach { acceso ->
                    val estaCerrado = accesosCerrados.contains(acceso)

                    TextButton(
                        onClick = { if (!estaCerrado) onSeleccionarAcceso(acceso) },
                        enabled = !estaCerrado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (estaCerrado) "$acceso (No disponible)" else acceso,
                                color = if (estaCerrado) Color.Gray else BurdeoRincon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar", color = Color.Gray)
            }
        }
    )
}