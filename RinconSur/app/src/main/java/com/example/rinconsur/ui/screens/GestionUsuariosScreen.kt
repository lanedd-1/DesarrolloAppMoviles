package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.BannerNotificacion
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.viewmodel.GestionUsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    usuarioActual: Usuario?,
    onVolver: () -> Unit,
    viewModel: GestionUsuariosViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mensajePlantilla = stringResource(id = R.string.gestion_usuarios_mensaje_modificado)

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios(contexto)
    }

    // BLOQUEO DE SEGURIDAD: Solo Administrador
    if (usuarioActual?.rolId != Rol.ID_ADMINISTRADOR) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.gestion_usuarios_restringido_titulo), color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(painterResource(R.drawable.ic_volver), contentDescription = null, tint = Color.White)
                        }
                    },
                    actions = {
                        LogoTopBarRinconSur()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BurdeoRincon)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(Dimens.espacioPantalla)) {
                Text(text = stringResource(id = R.string.gestion_usuarios_restringido_mensaje))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.gestion_usuarios_titulo), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = stringResource(id = R.string.gestion_usuarios_cd_volver), tint = Color.White)
                    }
                },
                actions = {
                    LogoTopBarRinconSur() // <-- LOGO AGREGADO AQUÍ
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BurdeoRincon)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = Dimens.espacioPantalla)
        ) {
            // EL BANNER NOTIFICACIÓN (Aparece y desaparece)
            if (uiState.mensajeExito != null) {
                Spacer(modifier = Modifier.height(Dimens.espacioMedio))
                BannerNotificacion(mensaje = uiState.mensajeExito!!)
            }

            Spacer(modifier = Modifier.height(Dimens.espacioMedio))

            if (uiState.usuarios.isEmpty()) {
                Text(stringResource(id = R.string.gestion_usuarios_sin_usuarios), color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    items(uiState.usuarios) { usuario ->
                        ItemUsuarioAdmin(
                            usuario = usuario,
                            usuarioActual = usuarioActual, // PASAMOS EL USUARIO ACTUAL
                            onCambiarEstado = { nuevoEstado ->
                                viewModel.cambiarEstadoUsuario(contexto, usuario, nuevoEstado, mensajePlantilla)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(Dimens.espacioGrande)) }
                }
            }
        }
    }
}

@Composable
fun ItemUsuarioAdmin(
    usuario: Usuario,
    usuarioActual: Usuario?,
    onCambiarEstado: (Boolean) -> Unit
) {
    // BLOQUEO: Evita modificar si es la propia cuenta del usuario o si es un Administrador
    val bloquearEdicion = usuario.id == usuarioActual?.id || usuario.rolId == Rol.ID_ADMINISTRADOR

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = usuario.rolNombre, style = MaterialTheme.typography.labelMedium, color = BurdeoRincon)
                Text(text = usuario.correo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = usuario.activo,
                    onCheckedChange = onCambiarEstado,
                    enabled = !bloquearEdicion, // DESACTIVA EL BOTÓN SI ES ADMIN
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = BurdeoRincon,
                        // Añadimos opacidad para que el Administrador se vea activo pero "bloqueado" visualmente
                        disabledCheckedTrackColor = BurdeoRincon.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = stringResource(id = if (usuario.activo) R.string.gestion_usuarios_estado_activo else R.string.gestion_usuarios_estado_suspendido),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (usuario.activo) {
                        if (bloquearEdicion) Color(0xFF2E7D32).copy(alpha = 0.5f) else Color(0xFF2E7D32)
                    } else {
                        Color.Red
                    }
                )
            }
        }
    }
}