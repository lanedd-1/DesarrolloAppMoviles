package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.AvatarCircular
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.DialogoCerrarSesion
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.ui.theme.RojoAlerta
import com.example.rinconsur.ui.theme.VerdeExito
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSesionesScreen(
    onIniciarSesionConteo: (String) -> Unit = {},
    onIrCrearCuenta: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Obtenemos el contexto para acceder a la BD y las SharedPreferences
    val contexto = LocalContext.current

    // Estado para guardar el usuario que cargaremos de la base de datos
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    // Controla si se muestra el diálogo de confirmación al cerrar sesión
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Al cargar la pantalla, leemos el ID de la sesión y buscamos al usuario en SQLite
    LaunchedEffect(Unit) {
        val idUsuarioLogueado = SesionPreferencias(contexto).obtenerIdUsuario()
        if (idUsuarioLogueado != -1L) {
            val dbHelper = RinconDbHelper(contexto)
            usuarioActual = dbHelper.buscarPorId(idUsuarioLogueado)
        }
    }

    if (mostrarDialogo) {
        DialogoCerrarSesion(
            onConfirmar = {
                mostrarDialogo = false
                onCerrarSesion()
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Rincón Sur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Mis Sesiones") },
                    selected = true, // Está en esta pantalla
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                )

                // Se muestra si el rol es Administrador O Jefe de Tienda
                if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR || usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
                    NavigationDrawerItem(
                        label = { Text("Registrar Colaborador") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onIrCrearCuenta()
                        },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogo = true // abre el diálogo, NO cierra la sesión todavía
                    },
                    modifier = Modifier.padding(Dimens.espacioChico)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mis Sesiones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Abrir menú",
                                tint = Color.White
                            )
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
                // Encabezado con foto y datos del colaborador dinámicos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarCircular(
                            imagen = R.drawable.ic_persona,
                            descripcion = "Foto de perfil"
                        )
                        Spacer(modifier = Modifier.width(Dimens.espacioMedio))
                        Column {
                            // Mostramos el nombre real del usuario de la BD
                            Text(
                                text = usuarioActual?.nombre ?: "Cargando...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            // Mostramos el rol real de la BD
                            Text(
                                text = "${usuarioActual?.rolNombre ?: ""} · Turno",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Indicadores rápidos del día
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaEstadistica(
                            valor = "2 / 4",
                            etiqueta = "Sesiones Hoy"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaEstadistica(
                            valor = "142",
                            etiqueta = "Muestras Registradas"
                        )
                    }
                }

                // Lista de Sesiones Programadas
                TarjetaSeccion(titulo = "Sesiones Asignadas") {
                    ItemSesion(
                        puerta = "Acceso 3 - Estacionamiento",
                        horario = "11:00 - 11:30 hrs",
                        estado = "Pendiente",
                        esActiva = true,
                        onIniciar = { onIniciarSesionConteo("Acceso 3") }
                    )
                    Spacer(modifier = Modifier.height(Dimens.espacioMedio))
                    ItemSesion(
                        puerta = "Acceso 1 - Galería Principal",
                        horario = "09:30 - 10:00 hrs",
                        estado = "Finalizada",
                        esActiva = false,
                        onIniciar = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemSesion(
    puerta: String,
    horario: String,
    estado: String,
    esActiva: Boolean,
    onIniciar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esActiva) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = puerta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EtiquetaEstado(
                    texto = estado,
                    colorFondo = if (esActiva) BurdeoRincon.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                    colorTexto = if (esActiva) BurdeoRincon else Color.DarkGray
                )
            }
            Text(
                text = "Bloque: $horario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (esActiva) {
                Spacer(modifier = Modifier.height(Dimens.espacioChico))
                BotonPrincipal(
                    texto = "Iniciar conteo en puerta",
                    onClick = onIniciar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, heightDp = 800)
@Composable
private fun MisSesionesScreenPreview() {
    RinconSurTheme {
        MisSesionesScreen()
    }
}