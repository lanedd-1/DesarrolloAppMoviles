package com.example.rinconsur.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RangoEtarioEnum
import com.example.rinconsur.model.Rol
import com.example.rinconsur.model.SesionEnCursoUiState
import com.example.rinconsur.model.TipoCompaniaEnum
import com.example.rinconsur.model.Usuario
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.DialogoCerrarSesion
import com.example.rinconsur.ui.components.DialogoFinalizarSesion
import com.example.rinconsur.ui.components.DialogoPausarSesion
import com.example.rinconsur.ui.components.LogoTopBarRinconSur
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.BurdeoRincon
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.viewmodel.SesionEnCursoViewModel
import kotlinx.coroutines.launch

@Composable
fun SesionEnCursoScreen(
    idSesion: Long = 0L,
    acceso: String = "",
    usuarioActual: Usuario?,
    onVolver: () -> Unit = {},
    onIrMisSesiones: () -> Unit = {},
    onIrCrearCuenta: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onIrGestionAccesos: () -> Unit = {},
    onIrGestionUsuarios: () -> Unit = {},
    onIrResumenDiario: () -> Unit = {},
    viewModel: SesionEnCursoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(idSesion) {
        viewModel.inicializarSesion(contexto, idSesion, acceso)
    }

    SesionEnCursoContent(
        uiState = uiState,
        usuarioActual = usuarioActual,
        onClasificacionSeleccionada = viewModel::onClasificacionSeleccionada,
        onRangoEtarioSeleccionado = viewModel::onRangoEtarioSeleccionado,
        onCompaniaSeleccionada = viewModel::onCompaniaSeleccionada,
        onBolsasOtraTiendaCambia = viewModel::onBolsasOtraTiendaCambia,
        onGuardarRegistro = { viewModel.onGuardarRegistro(contexto) },
        onAnularUltimoRegistro = { viewModel.onAnularUltimoRegistro(contexto) },
        onPausarSesion = onVolver,
        onFinalizarSesion = {
            viewModel.finalizarSesionConteo(contexto)
            onVolver()
        },
        onIrMisSesiones = onIrMisSesiones,
        onIrCrearCuenta = onIrCrearCuenta,
        onCerrarSesion = onCerrarSesion,
        onIrGestionAccesos = onIrGestionAccesos,
        onIrGestionUsuarios = onIrGestionUsuarios,
        onIrResumenDiario = onIrResumenDiario
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesionEnCursoContent(
    uiState: SesionEnCursoUiState,
    usuarioActual: Usuario?,
    onClasificacionSeleccionada: (ClasificacionPersona) -> Unit,
    onRangoEtarioSeleccionado: (RangoEtarioEnum) -> Unit,
    onCompaniaSeleccionada: (TipoCompaniaEnum) -> Unit,
    onBolsasOtraTiendaCambia: (Boolean) -> Unit,
    onGuardarRegistro: () -> Unit,
    onAnularUltimoRegistro: () -> Unit,
    onPausarSesion: () -> Unit = {},
    onFinalizarSesion: () -> Unit = {},
    onIrMisSesiones: () -> Unit = {},
    onIrCrearCuenta: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onIrGestionAccesos: () -> Unit = {},
    onIrGestionUsuarios: () -> Unit = {},
    onIrResumenDiario: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var mostrarDialogoPausa by remember { mutableStateOf(false) }
    var mostrarDialogoFinalizar by remember { mutableStateOf(false) }
    var mostrarDialogoCerrar by remember { mutableStateOf(false) }

    val esTemaOscuro = isSystemInDarkTheme()
    val fondoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.surface else BurdeoRincon
    val colorContenidoTopAppBar = if (esTemaOscuro) MaterialTheme.colorScheme.onSurface else Color.White

    BackHandler { mostrarDialogoPausa = true }

    if (mostrarDialogoCerrar) {
        DialogoCerrarSesion(
            onConfirmar = { mostrarDialogoCerrar = false; onCerrarSesion() },
            onCancelar = { mostrarDialogoCerrar = false }
        )
    }

    if (mostrarDialogoPausa) {
        DialogoPausarSesion(
            onConfirmar = { mostrarDialogoPausa = false; onPausarSesion() },
            onCancelar = { mostrarDialogoPausa = false }
        )
    }

    if (mostrarDialogoFinalizar) {
        DialogoFinalizarSesion(
            onConfirmar = { mostrarDialogoFinalizar = false; onFinalizarSesion() },
            onCancelar = { mostrarDialogoFinalizar = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(stringResource(R.string.login_subtitulo), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(Dimens.espacioMedio))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.gestion_accesos_boton_volver)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onIrMisSesiones() },
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                )

                if (usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_lateral_gestionar_accesos)) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrGestionAccesos() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR || usuarioActual?.rolId == Rol.ID_JEFE_TIENDA) {

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_lateral_resumen_dia)) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrResumenDiario() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_lateral_registrar_colaborador)) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrCrearCuenta() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                if (usuarioActual?.rolId == Rol.ID_ADMINISTRADOR) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.gestion_usuarios_titulo)) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onIrGestionUsuarios() },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_lateral_cerrar_sesion), color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogoCerrar = true
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
                        Column {
                            Text(stringResource(R.string.sesion_curso_titulo), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colorContenidoTopAppBar)
                            Text(stringResource(R.string.sesion_curso_subtitulo), style = MaterialTheme.typography.bodyMedium, color = colorContenidoTopAppBar.copy(alpha = 0.8f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = stringResource(R.string.sesion_curso_cd_menu), tint = colorContenidoTopAppBar)
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
                CabeceraSesion(uiState, usuarioActual)

                if (uiState.guardadoLocalActivo) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Dimens.cornerSmall)).background(Color(0xFFE8F5E9)).padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Dimens.espacioChico))
                        Text(text = stringResource(R.string.sesion_curso_guardado_local), style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    }
                }

                FormularioRegistro(
                    uiState = uiState,
                    onClasificacion = onClasificacionSeleccionada,
                    onRango = onRangoEtarioSeleccionado,
                    onCompania = onCompaniaSeleccionada,
                    onBolsas = onBolsasOtraTiendaCambia,
                    onGuardar = onGuardarRegistro,
                    onPausar = { mostrarDialogoPausa = true },
                    onFinalizar = { mostrarDialogoFinalizar = true }
                )

                ResumenRegistros(uiState, onAnularUltimoRegistro)

                Text(
                    text = stringResource(R.string.sesion_curso_pie_sincronizacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = Dimens.espacioGrande)
                )
            }
        }
    }
}


@Composable
private fun CabeceraSesion(uiState: SesionEnCursoUiState, usuarioActual: Usuario?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Color(0xFFD32F2F)))
                Spacer(modifier = Modifier.width(Dimens.espacioMini))
                Text(text = stringResource(R.string.sesion_curso_estado_en_curso, uiState.accesoNombre), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "${uiState.ubicacionAcceso} · ${usuarioActual?.nombre ?: "Cargando..."}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormularioRegistro(
    uiState: SesionEnCursoUiState,
    onClasificacion: (ClasificacionPersona) -> Unit,
    onRango: (RangoEtarioEnum) -> Unit,
    onCompania: (TipoCompaniaEnum) -> Unit,
    onBolsas: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onPausar: () -> Unit,
    onFinalizar: () -> Unit
) {
    TarjetaSeccion(titulo = stringResource(R.string.sesion_curso_seccion_nuevo)) {
        Text(stringResource(R.string.sesion_curso_subtitulo_tactil), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(Dimens.espacioMini))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.sesion_curso_clasificacion), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.sesion_curso_obligatoria), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            ClasificacionPersona.entries.forEach { opcion ->
                BotonOpcionTactil(texto = opcion.etiqueta, seleccionado = uiState.clasificacionSeleccionada == opcion, onClick = { onClasificacion(opcion) })
            }
        }

        Spacer(modifier = Modifier.height(Dimens.espacioChico))
        Text(stringResource(R.string.sesion_curso_rango_etario), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            RangoEtarioEnum.entries.forEach { rango ->
                BotonOpcionTactil(texto = rango.etiqueta, seleccionado = uiState.rangoEtarioSeleccionado == rango, onClick = { onRango(rango) })
            }
        }

        Spacer(modifier = Modifier.height(Dimens.espacioChico))
        Text(stringResource(R.string.sesion_curso_compania), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            TipoCompaniaEnum.entries.forEach { comp ->
                BotonOpcionTactil(texto = comp.etiqueta, seleccionado = uiState.companiaSeleccionada == comp, onClick = { onCompania(comp) })
            }
        }

        Spacer(modifier = Modifier.height(Dimens.espacioChico))
        Text(stringResource(R.string.sesion_curso_pregunta_bolsas), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            BotonOpcionTactil(texto = stringResource(R.string.sesion_curso_opcion_si), seleccionado = uiState.traeBolsasOtraTienda, onClick = { onBolsas(true) }, modifier = Modifier.weight(1f))
            BotonOpcionTactil(texto = stringResource(R.string.sesion_curso_opcion_no), seleccionado = !uiState.traeBolsasOtraTienda, onClick = { onBolsas(false) }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Dimens.espacioMedio))
        BotonPrincipal(texto = stringResource(R.string.sesion_curso_boton_guardar), onClick = onGuardar, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(Dimens.espacioChico))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            BotonSecundario(texto = stringResource(R.string.sesion_curso_boton_pausar), onClick = onPausar, modifier = Modifier.weight(1f))
            BotonSecundario(texto = stringResource(R.string.sesion_curso_boton_finalizar), onClick = onFinalizar, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResumenRegistros(uiState: SesionEnCursoUiState, onAnular: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.sesion_curso_resumen_titulo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${uiState.totalRegistrosSesion}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(stringResource(R.string.sesion_curso_resumen_ultimo, uiState.ultimoRegistroResumen), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            if (uiState.permiteAnular) {
                BotonSecundario(texto = stringResource(R.string.sesion_curso_boton_anular), onClick = onAnular, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun BotonOpcionTactil(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esTemaOscuro = isSystemInDarkTheme()

    val colorFondo = when {
        seleccionado -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        esTemaOscuro -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.White
    }

    val colorBorde = when {
        seleccionado -> MaterialTheme.colorScheme.primary
        esTemaOscuro -> MaterialTheme.colorScheme.outline
        else -> Color(0xFFD6D6D6)
    }

    val colorTexto = when {
        seleccionado -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.cornerSmall))
            .background(colorFondo)
            .border(
                width = if (seleccionado) 1.5.dp else 1.dp,
                color = colorBorde,
                shape = RoundedCornerShape(Dimens.cornerSmall)
            )
            .clickable { onClick() }
            .padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (seleccionado) {
                Text(text = "✓ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                color = colorTexto
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SesionEnCursoPreview() {
    RinconSurTheme {
        SesionEnCursoScreen(usuarioActual = null)
    }
}