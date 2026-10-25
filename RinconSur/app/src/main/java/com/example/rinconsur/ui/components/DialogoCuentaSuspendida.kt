package com.example.rinconsur.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.R
import com.example.rinconsur.ui.theme.BurdeoRincon

@Composable
fun DialogoCuentaSuspendida(
    onEntendido: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onEntendido,
        modifier = Modifier.border(
            width = 2.dp,
            color = BurdeoRincon,
            shape = RoundedCornerShape(16.dp)
        ),
        title = { Text(text = stringResource(R.string.dialogo_suspendida_titulo), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.login_error_cuenta_suspendida)) },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(
                onClick = onEntendido,
                colors = ButtonDefaults.buttonColors(containerColor = BurdeoRincon)
            ) {
                Text(stringResource(R.string.dialogo_suspendida_entendido), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}