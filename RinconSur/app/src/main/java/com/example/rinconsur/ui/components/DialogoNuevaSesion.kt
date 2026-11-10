package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoNuevaSesion(
    listaObservadores: List<Usuario>,
    accesosCerrados: List<String>,
    onAsignarAcceso: (observadorId: Long, acceso: String) -> Unit,
    onCancelar: () -> Unit
) {
    val contexto = LocalContext.current
    val dbHelper = remember { RinconDbHelper(contexto) }

    var observadorSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var accesoSeleccionado by remember { mutableStateOf<String?>(null) }

    var expandedObservador by remember { mutableStateOf(false) }
    var expandedAcceso by remember { mutableStateOf(false) }

    val todosLosAccesos = listOf("Acceso A1", "Acceso A2", "Acceso A3", "Acceso A4")
    val accesosDisponibles = todosLosAccesos.filter { !accesosCerrados.contains(it) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = stringResource(R.string.dialogo_nueva_sesion_titulo),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.espacioMini),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                // Desplegable de Observador (O)
                ExposedDropdownMenuBox(
                    expanded = expandedObservador,
                    onExpandedChange = { expandedObservador = !expandedObservador }
                ) {
                    OutlinedTextField(
                        value = observadorSeleccionado?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_observador_o)) },
                        placeholder = { Text(stringResource(R.string.dialogo_hint_seleccionar_observador)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedObservador) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedObservador,
                        onDismissRequest = { expandedObservador = false }
                    ) {
                        if (listaObservadores.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sin_observadores_disponibles)) },
                                onClick = { expandedObservador = false }
                            )
                        } else {
                            listaObservadores.forEach { obs ->
                                val tieneSesionActiva = dbHelper.tieneSesionActiva(obs.id)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (tieneSesionActiva) "${obs.nombre} (Ocupado)" else obs.nombre,
                                            color = if (tieneSesionActiva) Color.Gray else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        if (!tieneSesionActiva) {
                                            observadorSeleccionado = obs
                                            expandedObservador = false
                                        }
                                    },
                                    enabled = !tieneSesionActiva
                                )
                            }
                        }
                    }
                }

                // Desplegable de Acceso (A)
                ExposedDropdownMenuBox(
                    expanded = expandedAcceso,
                    onExpandedChange = { expandedAcceso = !expandedAcceso }
                ) {
                    OutlinedTextField(
                        value = accesoSeleccionado ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_acceso_a)) },
                        placeholder = { Text(stringResource(R.string.dialogo_hint_seleccionar_acceso)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAcceso) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAcceso,
                        onDismissRequest = { expandedAcceso = false }
                    ) {
                        if (accesosDisponibles.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sin_accesos_disponibles)) },
                                onClick = { expandedAcceso = false }
                            )
                        } else {
                            accesosDisponibles.forEach { acceso ->
                                DropdownMenuItem(
                                    text = { Text(acceso) },
                                    onClick = {
                                        accesoSeleccionado = acceso
                                        expandedAcceso = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (observadorSeleccionado != null && accesoSeleccionado != null) {
                        onAsignarAcceso(observadorSeleccionado!!.id, accesoSeleccionado!!)
                    }
                },
                enabled = observadorSeleccionado != null && accesoSeleccionado != null
            ) {
                Text(
                    text = stringResource(R.string.boton_asignar_mayus),
                    color = if (observadorSeleccionado != null && accesoSeleccionado != null) BurdeoRincon else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = stringResource(R.string.dialogo_cancelar), color = Color.Gray)
            }
        }
    )
}