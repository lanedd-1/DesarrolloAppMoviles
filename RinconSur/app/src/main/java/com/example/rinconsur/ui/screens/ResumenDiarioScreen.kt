package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rinconsur.R
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenDiarioScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen del Día", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BurdeoRincon)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text(
                text = "Métricas Generales - Hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BurdeoRincon
            )

            // DATOS DE PRUEBA (Hardcodeados por ahora)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Box(modifier = Modifier.weight(1f)) { TarjetaEstadistica(valor = "0", etiqueta = "Total Muestras") }
                Box(modifier = Modifier.weight(1f)) { TarjetaEstadistica(valor = "0", etiqueta = "Sesiones Activas") }
            }

            TarjetaSeccion(titulo = "Flujo por Accesos (Prueba)") {
                Text("Aquí irán los conteos agrupados por puerta.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.espacioMedio))
                FilaAcceso("Acceso A1", "0 pax", "Normal", Color(0xFFE8F5E9), Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun FilaAcceso(acceso: String, flujo: String, estado: String, colorFondo: Color, colorTexto: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = acceso, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = flujo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        EtiquetaEstado(texto = estado, colorFondo = colorFondo, colorTexto = colorTexto)
    }
}