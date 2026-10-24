package com.example.rinconsur.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.rinconsur.R

@Composable
fun DialogoMostrarFoto(
    onDismiss: () -> Unit,
    onCamara: () -> Unit,
    onGaleria: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialogo_foto_titulo)) },
        text = { Text(stringResource(R.string.dialogo_foto_mensaje)) },
        confirmButton = {
            TextButton(onClick = onCamara) {
                Text(stringResource(R.string.dialogo_foto_camara))
            }
        },
        dismissButton = {
            TextButton(onClick = onGaleria) {
                Text(stringResource(R.string.dialogo_foto_galeria))
            }
        }
    )
}