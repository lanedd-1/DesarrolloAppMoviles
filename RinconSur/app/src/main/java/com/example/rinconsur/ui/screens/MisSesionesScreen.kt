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
import com.example.rinconsur.ui.components.DialogoMostrarFoto
import com.example.rinconsur.ui.components.DialogoNuevaSesion
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

@Composable
fun MisSesionesScreen(
    onIniciarSesionConteo: (Long, String) -> Unit = { _, _ -> },
    onIrCrearCuenta: () -> Unit = {},
    onIrResumenDiario: () -> Unit = {},
    onIrGestionAccesos: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var listaSesiones by remember { mutableStateOf<List<SesionItem>>(emptyList()) }
    var totalMuestras by remember { mutableIntStateOf(0) }
    var sesionesCompletadas by remember { mutableIntStateOf(0) }
    var sesionesTotales by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val idLogueado = SesionPreferencias(contexto).obtenerIdUsuario()
                if (idLogueado != -1L) {
                    val dbHelper = RinconDbHelper(contexto)
                    usuarioActual = dbHelper.buscarPorId(idLogueado)
                    totalMuestras = dbHelper.obtenerMuestrasRegistradasUsuario(idLogueado)

                    val estadisticas = dbHelper.obtenerEstadisticasSesiones(idLogueado)
                    sesionesCompletadas = estadisticas.first
                    sesionesTotales = estadisticas.second

                    listaSesiones = dbHelper.obtenerSesionesPorUsuario(idLogueado)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MisSesionesContent(
        usuarioActual = usuarioActual,
        listaSesiones = listaSesiones,
        totalMuestras = totalMuestras,
        sesionesCompletadas = sesionesCompletadas,
        sesionesTotales = sesionesTotales,
        onIniciarSesionConteo = onIniciarSesionConteo,
        onIrCrearCuenta = onIrCrearCuenta,
        onIrResumenDiario = onIrResumenDiario,
        onIrGestionAccesos = onIrGestionAccesos,
        onCerrarSesion = onCerrarSesion,
        onCrearNuevaSesion = { accesoSeleccionado ->
            if (usuarioActual != null) {
                val dbHelper = RinconDbHelper(contexto)
                val cerrados = dbHelper.obtenerAccesosCerrados()

                if (!cerrados.contains(accesoSeleccionado)) {
                    val codigoAleatorio = "SES-${(100..999).random()}"
                    val fechaHoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                    dbHelper.crearSesion(
                        codigo = codigoAleatorio,
                        acceso = accesoSeleccionado,
                        fecha = fechaHoy,
                        usuarioId = usuarioActual!!.id
                    )

                    listaSesiones = dbHelper.obtenerSesionesPorUsuario(usuarioActual!!.id)
                    val stats = dbHelper.obtenerEstadisticasSesiones(usuarioActual!!.id)
                    sesionesCompletadas = stats.first
                    sesionesTotales = stats.second
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSesionesContent(
    usuarioActual: Usuario?,
    listaSesiones: List<SesionItem>,
    totalMuestras: Int,
    sesionesCompletadas: Int,
    sesionesTotales: Int,
    onIniciarSesionConteo: (Long, String) -> Unit,
    onIrCrearCuenta: () -> Unit,
    onIrResumenDiario: () -> Unit,
    onIrGestionAccesos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCrearNuevaSesion: (String) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current

    var mostrarDialogoCerrar by remember { mutableStateOf(false) }
    var mostrarDialogoFoto by remember { mutableStateOf(false) }
    var mostrarDialogoNuevaSesion by remember { mutableStateOf(false) }

    var accesosCerrados by remember { mutableStateOf<List<String>>(emptyList()) }

    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Permisos.abrirAjustes(contexto)
    }

    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Permisos.abrirAjustes(contexto)
    }

    if (mostrarDialogoCerrar) {
        DialogoCerrarSesion(
            onConfirmar = { mostrarDialogoCerrar = false; onCerrarSesion() },
            onCancelar = { mostrarDialogoCerrar = false }
        )
    }

    if (mostrarDialogoFoto) {
        DialogoMostrarFoto(
            onDismiss = { mostrarDialogoFoto = false },
            onCamara = {
                mostrarDialogoFoto = false
                // Lanzamos directamente; el sistema decide si muestra el popup o abre ajustes si fue denegado por completo
                camaraLauncher.launch(Permisos.CAMARA)
            },
            onGaleria = {
                mostrarDialogoFoto = false
                galeriaLauncher.launch(Permisos.IMAGENES)
            }
        )
    }

    if (mostrarDialogoNuevaSesion) {
        DialogoNuevaSesion(
            accesosCerrados = accesosCerrados,
            onSeleccionarAcceso = { acceso ->
                mostrarDialogoNuevaSesion = false
                if (!accesosCerrados.contains(acceso)) {
                    onCrearNuevaSesion(acceso)
                }
            },
            onCancelar = { mostrarDialogoNuevaSesion = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MenuLateral(
                usuarioActual = usuarioActual,
                onCerrarMenu = { scope.launch { drawerState.close() } },
                onIrResumenDiario = onIrResumenDiario,
                onIrCrearCuenta = onIrCrearCuenta,
                onIrGestionAccesos = onIrGestionAccesos,
                onMostrarCerrarSesion = { mostrarDialogoCerrar = true }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mis Sesiones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(R.drawable.ic_menu), "Abrir menú", tint = Color.White)
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
                TarjetaPerfil(usuarioActual) { mostrarDialogoFoto = true }

                EstadisticasResumen(sesionesCompletadas, sesionesTotales, totalMuestras)

                ListaDeSesiones(
                    lista = listaSesiones,
                    onIniciar = onIniciarSesionConteo,
                    onCrearNueva = {
                        val dbHelper = RinconDbHelper(contexto)
                        accesosCerrados = dbHelper.obtenerAccesosCerrados()
                        mostrarDialogoNuevaSesion = true
                    }
                )
            }
        }
    }
}

// ----------------------------- COMPONENTES PRIVADOS -----------------------------

@Composable
private fun MenuLateral(
    usuarioActual: Usuario?,
    onCerrarMenu: () -> Unit,
    onIrResumenDiario: () -> Unit,
    onIrCrearCuenta: () -> Unit,
    onIrGestionAccesos: () -> Unit,
    onMostrarCerrarSesion: () -> Unit
) {
    ModalDrawerSheet {
        Text("Rincón Sur", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(Dimens.espacioMedio))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("Mis Sesiones") },
            selected = true,
            onClick = onCerrarMenu,
            modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
        )

        if (usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
            NavigationDrawerItem(
                label = { Text("Gestionar Accesos (Puertas)") },
                selected = false,
                onClick = { onCerrarMenu(); onIrGestionAccesos() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )
        }

        if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR || usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
            NavigationDrawerItem(
                label = { Text("Resumen del Día") },
                selected = false,
                onClick = { onCerrarMenu(); onIrResumenDiario() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )

            NavigationDrawerItem(
                label = { Text("Registrar Colaborador") },
                selected = false,
                onClick = { onCerrarMenu(); onIrCrearCuenta() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = { onCerrarMenu(); onMostrarCerrarSesion() },
            modifier = Modifier.padding(Dimens.espacioChico)
        )
    }
}

@Composable
private fun TarjetaPerfil(usuarioActual: Usuario?, onCambiarFoto: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(Dimens.espacioMedio), verticalAlignment = Alignment.CenterVertically) {
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
                    modifier = Modifier.clickable(onClick = onCambiarFoto)
                )
            }
        }
    }
}

@Composable
private fun EstadisticasResumen(completadas: Int, totales: Int, muestras: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        Box(modifier = Modifier.weight(1f)) {
            TarjetaEstadistica(valor = "$completadas / $totales", etiqueta = "Sesiones Hoy")
        }
        Box(modifier = Modifier.weight(1f)) {
            TarjetaEstadistica(valor = muestras.toString(), etiqueta = "Muestras Registradas")
        }
    }
}

@Composable
private fun ListaDeSesiones(
    lista: List<SesionItem>,
    onIniciar: (Long, String) -> Unit,
    onCrearNueva: () -> Unit
) {
    TarjetaSeccion(titulo = "Sesiones Asignadas") {
        if (lista.isEmpty()) {
            Text("No tienes sesiones registradas.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Dimens.espacioMedio))
        } else {
            lista.forEach { sesion ->
                ItemSesion(
                    codigo = sesion.codigo,
                    acceso = sesion.acceso,
                    fecha = sesion.fecha,
                    estado = sesion.estado,
                    esActiva = sesion.estado != "FINALIZADA",
                    onIniciar = { onIniciar(sesion.id, sesion.acceso) }
                )
                Spacer(modifier = Modifier.height(Dimens.espacioMedio))
            }
        }
        Spacer(modifier = Modifier.height(Dimens.espacioChico))
        BotonSecundario(texto = "+ Crear nueva sesión", onClick = onCrearNueva, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ItemSesion(codigo: String, acceso: String, fecha: String, estado: String, esActiva: Boolean, onIniciar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (esActiva) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = acceso, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                EtiquetaEstado(
                    texto = estado.replace("_", " "),
                    colorFondo = if (esActiva) BurdeoRincon.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                    colorTexto = if (esActiva) BurdeoRincon else Color.DarkGray
                )
            }
            Text(text = "Código: $codigo · $fecha", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            if (esActiva) {
                Spacer(modifier = Modifier.height(Dimens.espacioChico))
                BotonPrincipal(texto = "Iniciar conteo en puerta", onClick = onIniciar, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MisSesionesPreview() {
    RinconSurTheme {
        MisSesionesContent(
            usuarioActual = null,
            listaSesiones = emptyList(),
            totalMuestras = 0, sesionesCompletadas = 0, sesionesTotales = 0,
            onIniciarSesionConteo = { _, _ -> },
            onIrCrearCuenta = {}, onIrResumenDiario = {}, onIrGestionAccesos = {}, onCerrarSesion = {}, onCrearNuevaSesion = {}
        )
    }
}