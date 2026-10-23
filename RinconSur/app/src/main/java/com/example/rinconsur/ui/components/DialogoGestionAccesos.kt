package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun DialogoGestionAccesos(
    estadoAccesos: Map<String, Boolean>,
    onCambiarEstado: (String, Boolean) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Gestión de Accesos Operativos", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Text(
                    text = "Habilita o inhabilita las puertas de la tienda para los observadores:",
                    style = MaterialTheme.typography.bodyMedium
                )

                estadoAccesos.forEach { (acceso, habilitado) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.espacioMini),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = acceso, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (habilitado) "Habilitado" else "Cerrado / No disponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (habilitado) Color(0xFF2E7D32) else Color.Red
                            )
                        }
                        Switch(
                            checked = habilitado,
                            onCheckedChange = { nuevoEstado -> onCambiarEstado(acceso, nuevoEstado) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BurdeoRincon
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Listo", color = BurdeoRincon, fontWeight = FontWeight.Bold)
            }
        }
    )
}