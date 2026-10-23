package com.example.rinconsur.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DialogoMostrarFoto(
    onDismiss: () -> Unit,
    onCamara: () -> Unit,
    onGaleria: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Foto de Perfil") },
        text = { Text("¿De dónde deseas obtener la imagen?") },
        confirmButton = {
            TextButton(onClick = onCamara) {
                Text("Cámara")
            }
        },
        dismissButton = {
            TextButton(onClick = onGaleria) {
                Text("Galería")
            }
        }
    )
}