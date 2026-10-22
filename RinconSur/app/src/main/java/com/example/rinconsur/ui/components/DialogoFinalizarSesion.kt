package com.example.rinconsur.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.RojoAlerta

@Composable
fun DialogoFinalizarSesion(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(text = "¿Finalizar registro?", fontWeight = FontWeight.Bold) },
        text = { Text("Si cierras el registro no podrás volver a entrar. Asegúrate de haber completado tu bloque de conteo.") },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = RojoAlerta) // Usamos rojo para denotar una acción destructiva/final
            ) {
                Text("Sí, finalizar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}