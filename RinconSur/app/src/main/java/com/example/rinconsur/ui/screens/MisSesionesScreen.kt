package com.example.rinconsur.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.RinconDbHelper
import com.example.rinconsur.data.SesionItem
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
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.SesionViewModel
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
    onIrGestionUsuarios: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: SesionViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarDatos(contexto)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MisSesionesContent(
        usuarioActual = uiState.usuarioActual,
        listaSesiones = uiState.listaSesiones,
        totalMuestras = uiState.totalMuestras,
        sesionesCompletadas = uiState.sesionesCompletadas,
        sesionesTotales = uiState.sesionesTotales,
        fotoPerfilBitmap = uiState.fotoPerfilBitmap,
        mensajeExito = uiState.mensajeExito,
        onLimpiarMensaje = viewModel::limpiarMensaje,
        onGuardarFotoBitmap = { bitmap -> viewModel.guardarFotoBitmap(contexto, bitmap) },
        onGuardarFotoUri = { uri -> viewModel.guardarFotoUri(contexto, uri) },
        onIniciarSesionConteo = onIniciarSesionConteo,
        onIrCrearCuenta = onIrCrearCuenta,
        onIrResumenDiario = onIrResumenDiario,
        onIrGestionAccesos = onIrGestionAccesos,
        onIrGestionUsuarios = onIrGestionUsuarios,
        onCerrarSesion = onCerrarSesion,
        onCrearNuevaSesion = { accesoSeleccionado ->
            if (uiState.usuarioActual != null) {
                val dbHelper = RinconDbHelper(contexto)
                val cerrados = dbHelper.obtenerAccesosCerrados()

                if (!cerrados.contains(accesoSeleccionado)) {
                    val codigoAleatorio = "SES-${(100..999).random()}"
                    val fechaHoy = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                    dbHelper.crearSesion(
                        codigo = codigoAleatorio,
                        acceso = accesoSeleccionado,
                        fecha = fechaHoy,
                        usuarioId = uiState.usuarioActual!!.id
                    )
                    viewModel.cargarDatos(contexto)
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
    fotoPerfilBitmap: Bitmap?,
    mensajeExito: String?,
    onLimpiarMensaje: () -> Unit,
    onGuardarFotoBitmap: (Bitmap) -> Unit,
    onGuardarFotoUri: (android.net.Uri) -> Unit,
    onIniciarSesionConteo: (Long, String) -> Unit,
    onIrCrearCuenta: () -> Unit,
    onIrResumenDiario: () -> Unit,
    onIrGestionAccesos: () -> Unit,
    onIrGestionUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCrearNuevaSesion: (String) -> Unit
) {

    var mostrarDialogoSalirSistema by remember { mutableStateOf(false) }

    BackHandler {
        mostrarDialogoSalirSistema = true
    }

    if (mostrarDialogoSalirSistema) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalirSistema = false },
            title = {
                Text(text = "Cerrar sesión")
            },
            text = {
                Text(text = "¿Estás seguro que quieres cerrar sesión y salir de tu cuenta?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoSalirSistema = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoSalirSistema = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    // -------------------------------------

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarDialogoCerrar by remember { mutableStateOf(false) }
    var mostrarDialogoFoto by remember { mutableStateOf(false) }
    var mostrarDialogoNuevaSesion by remember { mutableStateOf(false) }
    var accesosCerrados by remember { mutableStateOf<List<String>>(emptyList()) }

    val esTemaOscuro = isSystemInDarkTheme()
    val fondoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.surface else BurdeoRincon
    val colorContenidoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.onSurface else Color.White

    LaunchedEffect(mensajeExito) {
        mensajeExito?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje, duration = SnackbarDuration.Short)
            onLimpiarMensaje()
        }
    }

    val camaraFotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val rotado = rotarBitmapSiEsNecesario(bitmap)
            onGuardarFotoBitmap(rotado)
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onGuardarFotoUri(uri)
        }
    }

    val camaraPermisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) camaraFotoLauncher.launch(null) else Permisos.abrirAjustes(contexto)
    }

    val galeriaPermisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) galeriaLauncher.launch("image/*") else Permisos.abrirAjustes(contexto)
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
                if (Permisos.tiene(contexto, Permisos.CAMARA)) camaraFotoLauncher.launch(null) else camaraPermisoLauncher.launch(Permisos.CAMARA)
            },
            onGaleria = {
                mostrarDialogoFoto = false
                if (Permisos.tiene(contexto, Permisos.IMAGENES)) galeriaLauncher.launch("image/*") else galeriaPermisoLauncher.launch(Permisos.IMAGENES)
            }
        )
    }

    if (mostrarDialogoNuevaSesion) {
        DialogoNuevaSesion(
            accesosCerrados = accesosCerrados,
            onSeleccionarAcceso = { acceso ->
                mostrarDialogoNuevaSesion = false
                if (!accesosCerrados.contains(acceso)) onCrearNuevaSesion(acceso)
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
                onIrGestionUsuarios = onIrGestionUsuarios,
                onMostrarCerrarSesion = { mostrarDialogoCerrar = true }
            )
        }
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.mis_sesiones_titulo), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colorContenidoTopAppBar) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(R.drawable.ic_menu), stringResource(R.string.mis_sesiones_cd_menu), tint = colorContenidoTopAppBar)
                        }
                    },
                    actions = {
                        LogoTopBarRinconSur()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = fondoTopAppBar,
                        titleContentColor = colorContenidoTopAppBar,
                        navigationIconContentColor = colorContenidoTopAppBar,
                        actionIconContentColor = colorContenidoTopAppBar
                    )
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
                TarjetaPerfil(
                    usuarioActual = usuarioActual,
                    fotoBitmap = fotoPerfilBitmap,
                    onCambiarFoto = { mostrarDialogoFoto = true }
                )

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

@Composable
private fun MenuLateral(
    usuarioActual: Usuario?,
    onCerrarMenu: () -> Unit,
    onIrResumenDiario: () -> Unit,
    onIrCrearCuenta: () -> Unit,
    onIrGestionAccesos: () -> Unit,
    onIrGestionUsuarios: () -> Unit,
    onMostrarCerrarSesion: () -> Unit
) {
    ModalDrawerSheet {
        Text(stringResource(R.string.login_subtitulo), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(Dimens.espacioMedio))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_lateral_Mis_Sesiones)) },
            selected = true,
            onClick = onCerrarMenu,
            modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
        )

        if (usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_lateral_gestionar_accesos)) },
                selected = false,
                onClick = { onCerrarMenu(); onIrGestionAccesos() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )
        }

        if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR || usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_lateral_resumen_dia)) },
                selected = false,
                onClick = { onCerrarMenu(); onIrResumenDiario() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )

            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_lateral_registrar_colaborador)) },
                selected = false,
                onClick = { onCerrarMenu(); onIrCrearCuenta() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )
        }

        if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_lateral_gestionar_usuarios)) },
                selected = false,
                onClick = { onCerrarMenu(); onIrGestionUsuarios() },
                modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_lateral_cerrar_sesion), color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = { onCerrarMenu(); onMostrarCerrarSesion() },
            modifier = Modifier.padding(Dimens.espacioChico)
        )
    }
}

@Composable
private fun TarjetaPerfil(
    usuarioActual: Usuario?,
    fotoBitmap: Bitmap?,
    onCambiarFoto: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(Dimens.espacioMedio), verticalAlignment = Alignment.CenterVertically) {

            if (fotoBitmap != null) {
                Image(
                    bitmap = fotoBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.mis_sesiones_cd_foto_perfil),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                AvatarCircular(imagen = R.drawable.ic_persona, descripcion = stringResource(R.string.mis_sesiones_cd_foto_perfil))
            }

            Spacer(modifier = Modifier.width(Dimens.espacioMedio))
            Column {
                Text(text = usuarioActual?.nombre ?: "Cargando...", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "${usuarioActual?.rolNombre ?: ""} · ${stringResource(R.string.mis_sesiones_turno)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(Dimens.espacioMini))
                Text(
                    text = stringResource(R.string.mis_sesiones_agregar_foto),
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
            TarjetaEstadistica(valor = "$completadas / $totales", etiqueta = stringResource(R.string.mis_sesiones_stat_sesiones_hoy))
        }
        Box(modifier = Modifier.weight(1f)) {
            TarjetaEstadistica(valor = muestras.toString(), etiqueta = stringResource(R.string.mis_sesiones_stat_muestras_registradas))
        }
    }
}

@Composable
private fun ListaDeSesiones(
    lista: List<SesionItem>,
    onIniciar: (Long, String) -> Unit,
    onCrearNueva: () -> Unit
) {
    TarjetaSeccion(titulo = stringResource(R.string.mis_sesiones_seccion_asignadas)) {
        if (lista.isEmpty()) {
            Text(
                text = stringResource(R.string.mis_sesiones_sin_sesiones),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Dimens.espacioMedio)
            )
        } else {
            lista.forEach { sesion ->
                ItemSesion(sesion = sesion, onIniciar = { onIniciar(sesion.id, sesion.acceso) })
                Spacer(modifier = Modifier.height(Dimens.espacioMedio))
            }
        }
        Spacer(modifier = Modifier.height(Dimens.espacioChico))
        BotonSecundario(texto = stringResource(R.string.mis_sesiones_boton_crear_nueva), onClick = onCrearNueva, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ItemSesion(sesion: SesionItem, onIniciar: () -> Unit) {
    val esActiva = sesion.estado != "FINALIZADA"
    val enPausa = esActiva && sesion.cantidadRegistros > 0

    val textoEstado = when {
        !esActiva -> stringResource(R.string.mis_sesiones_estado_finalizada)
        enPausa -> stringResource(R.string.mis_sesiones_estado_pausa)
        else -> stringResource(R.string.mis_sesiones_estado_curso)
    }

    val textoBoton = if (enPausa) {
        stringResource(R.string.mis_sesiones_boton_reanudar)
    } else {
        stringResource(R.string.mis_sesiones_item_boton_iniciar)
    }

    val fondoEtiquetaEstado = if (esActiva) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f)
    val textoEtiquetaEstado = if (esActiva) MaterialTheme.colorScheme.primary else Color.DarkGray

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (esActiva) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = sesion.acceso, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                EtiquetaEstado(
                    texto = textoEstado,
                    colorFondo = fondoEtiquetaEstado,
                    colorTexto = textoEtiquetaEstado
                )
            }
            Text(text = stringResource(R.string.mis_sesiones_item_codigo, sesion.codigo, sesion.fecha), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            if (esActiva) {
                Spacer(modifier = Modifier.height(Dimens.espacioChico))
                BotonPrincipal(texto = textoBoton, onClick = onIniciar, modifier = Modifier.fillMaxWidth())
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
            fotoPerfilBitmap = null,
            mensajeExito = null,
            onLimpiarMensaje = {},
            onGuardarFotoBitmap = {},
            onGuardarFotoUri = {},
            onIniciarSesionConteo = { _, _ -> },
            onIrCrearCuenta = {}, onIrResumenDiario = {}, onIrGestionAccesos = {}, onIrGestionUsuarios = {}, onCerrarSesion = {}, onCrearNuevaSesion = {}
        )
    }
}

private fun rotarBitmapSiEsNecesario(bitmap: Bitmap): Bitmap {
    val matriz = Matrix()
    if (bitmap.width > bitmap.height) {
        matriz.postRotate(270f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
}