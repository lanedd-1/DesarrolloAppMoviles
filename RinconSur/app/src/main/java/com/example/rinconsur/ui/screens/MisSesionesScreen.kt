package com.example.rinconsur.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionItem
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.permisos.Permisos
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSesionesScreen(
    onIniciarSesionConteo: (Long, String) -> Unit = { _, _ -> }, // ¡AQUÍ RECIBE LOS 2 PARÁMETROS!
    onIrCrearCuenta: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarDialogoFoto by remember { mutableStateOf(false) }
    var mostrarDialogoNuevaSesion by remember { mutableStateOf(false) }

    var totalMuestras by remember { mutableIntStateOf(0) }
    var sesionesCompletadas by remember { mutableIntStateOf(0) }
    var sesionesTotales by remember { mutableIntStateOf(0) }

    var listaSesiones by remember { mutableStateOf<List<SesionItem>>(emptyList()) }

    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Permisos.abrirAjustes(contexto)
    }

    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Permisos.abrirAjustes(contexto)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val idUsuarioLogueado = SesionPreferencias(contexto).obtenerIdUsuario()
                if (idUsuarioLogueado != -1L) {
                    val dbHelper = RinconDbHelper(contexto)
                    usuarioActual = dbHelper.buscarPorId(idUsuarioLogueado)

                    totalMuestras = dbHelper.obtenerMuestrasRegistradasUsuario(idUsuarioLogueado)
                    val (completadas, totales) = dbHelper.obtenerEstadisticasSesiones(idUsuarioLogueado)
                    sesionesCompletadas = completadas
                    sesionesTotales = totales

                    listaSesiones = dbHelper.obtenerSesionesPorUsuario(idUsuarioLogueado)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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

    if (mostrarDialogoFoto) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoFoto = false },
            title = { Text(text = "Seleccionar Foto de Perfil") },
            text = { Text(text = "¿De dónde deseas obtener la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoFoto = false
                    if (!Permisos.tiene(contexto, Permisos.CAMARA)) camaraLauncher.launch(Permisos.CAMARA)
                }) { Text("Cámara") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoFoto = false
                    if (!Permisos.tiene(contexto, Permisos.IMAGENES)) galeriaLauncher.launch(Permisos.IMAGENES)
                }) { Text("Galería") }
            }
        )
    }

    if (mostrarDialogoNuevaSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevaSesion = false },
            title = { Text(text = "Nueva Sesión de Conteo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Selecciona el acceso asignado:", modifier = Modifier.padding(bottom = Dimens.espacioMedio))
                    val accesosDisponibles = listOf("Acceso A1", "Acceso A2", "Acceso A3", "Acceso A4")

                    accesosDisponibles.forEach { nombreAcceso ->
                        TextButton(
                            onClick = {
                                if (usuarioActual != null) {
                                    val dbHelper = RinconDbHelper(contexto)
                                    val codigoAleatorio = "SES-${(100..999).random()}"
                                    val fechaHoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                                    dbHelper.crearSesion(
                                        codigo = codigoAleatorio,
                                        acceso = nombreAcceso,
                                        fecha = fechaHoy,
                                        usuarioId = usuarioActual!!.id
                                    )

                                    listaSesiones = dbHelper.obtenerSesionesPorUsuario(usuarioActual!!.id)
                                    val (completadas, totales) = dbHelper.obtenerEstadisticasSesiones(usuarioActual!!.id)
                                    sesionesCompletadas = completadas
                                    sesionesTotales = totales
                                }
                                mostrarDialogoNuevaSesion = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = nombreAcceso, color = BurdeoRincon, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoNuevaSesion = false }) { Text("Cancelar", color = Color.Gray) }
            }
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
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                )

                if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR || usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
                    NavigationDrawerItem(
                        label = { Text("Registrar Colaborador") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrCrearCuenta() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; mostrarDialogo = true },
                    modifier = Modifier.padding(Dimens.espacioChico)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Mis Sesiones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = "Abrir menú", tint = Color.White)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarCircular(imagen = R.drawable.ic_persona, descripcion = "Foto de perfil")
                        Spacer(modifier = Modifier.width(Dimens.espacioMedio))
                        Column {
                            Text(text = usuarioActual?.nombre ?: "Cargando...", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "${usuarioActual?.rolNombre ?: ""} · Turno", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(Dimens.espacioMini))
                            Text(
                                text = "Agregar foto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { mostrarDialogoFoto = true }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaEstadistica(
                            valor = "$sesionesCompletadas / $sesionesTotales",
                            etiqueta = "Sesiones Hoy"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaEstadistica(
                            valor = totalMuestras.toString(),
                            etiqueta = "Muestras Registradas"
                        )
                    }
                }

                TarjetaSeccion(titulo = "Sesiones Asignadas") {
                    if (listaSesiones.isEmpty()) {
                        Text(
                            text = "No tienes sesiones registradas.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(Dimens.espacioMedio)
                        )
                    } else {
                        listaSesiones.forEach { sesion ->
                            ItemSesion(
                                puerta = sesion.acceso,
                                horario = "Código: ${sesion.codigo} ·${sesion.fecha}",
                                estado = sesion.estado,
                                esActiva = sesion.estado != "FINALIZADA",
                                onIniciar = { onIniciarSesionConteo(sesion.id, sesion.acceso) } // ¡SE ENVÍA EL ID Y EL ACCESO!
                            )
                            Spacer(modifier = Modifier.height(Dimens.espacioMedio))
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.espacioChico))
                    BotonSecundario(
                        texto = "+ Crear nueva sesión",
                        onClick = { mostrarDialogoNuevaSesion = true },
                        modifier = Modifier.fillMaxWidth()
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MisSesionesScreenPreview() {
    RinconSurTheme {
        MisSesionesScreen()
    }
}