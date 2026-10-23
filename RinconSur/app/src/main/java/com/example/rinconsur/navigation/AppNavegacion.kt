package com.example.rinconsur.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.ui.screens.CambiarContrasenaScreen
import com.example.rinconsur.ui.screens.FormularioScreen
import com.example.rinconsur.ui.screens.GestionAccesosScreen
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.MisSesionesScreen
import com.example.rinconsur.ui.screens.OlvideContrasenaScreen
import com.example.rinconsur.ui.screens.ResumenDiarioScreen
import com.example.rinconsur.ui.screens.SesionEnCursoScreen
import com.example.rinconsur.viewmodel.SesionViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val contexto = LocalContext.current

    val sesionViewModel: SesionViewModel = viewModel()

    // Si ya había una sesión guardada, la carga al abrir la app
    LaunchedEffect(Unit) {
        sesionViewModel.cargarUsuario(contexto)
    }

    val usuarioActual by sesionViewModel.usuarioActual.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    sesionViewModel.cargarUsuario(contexto)
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onOlvidasteContrasena = { navController.navigate(Ruta.OlvideContrasena.ruta) }
            )
        }

        composable(Ruta.OlvideContrasena.ruta) {
            OlvideContrasenaScreen(
                onCodigoVerificado = { correo ->
                    navController.navigate(Ruta.CambiarContrasena.crear(correo)) {
                        popUpTo(Ruta.OlvideContrasena.ruta) { inclusive = true }
                    }
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(
            route = Ruta.CambiarContrasena.ruta,
            arguments = listOf(navArgument("correo") { type = NavType.StringType })
        ) { entrada ->
            val correo = Uri.decode(entrada.arguments?.getString("correo").orEmpty())
            CambiarContrasenaScreen(
                correoUsuario = correo,
                onCambioExitoso = {
                    Toast.makeText(contexto, "Contraseña actualizada. Inicia sesión.", Toast.LENGTH_LONG).show()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.MisSesiones.ruta) {
            MisSesionesScreen(
                onIniciarSesionConteo = { id, acceso ->
                    navController.navigate(Ruta.SesionEnCurso.crear(id, acceso))
                },
                onIrCrearCuenta = { navController.navigate(Ruta.Formulario.ruta) },
                onIrResumenDiario = { navController.navigate(Ruta.ResumenDiario.ruta) },
                onIrGestionAccesos = { navController.navigate(Ruta.GestionAccesos.ruta) }, // <-- NUEVA CONEXIÓN
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    SesionPreferencias(contexto).limpiar()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Ruta.GestionAccesos.ruta) {
            GestionAccesosScreen(
                onVolver = { navController.navigateUp() }
            )
        }

        // PANTALLA DE RESUMEN DIARIO
        composable(Ruta.ResumenDiario.ruta) {
            ResumenDiarioScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() } // Para volver a Mis Sesiones
            )
        }

        composable(
            route = Ruta.SesionEnCurso.ruta,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("acceso") { type = NavType.StringType }
            )
        ) { entrada ->
            val idSesion = entrada.arguments?.getLong("id") ?: 0L
            val acceso = entrada.arguments?.getString("acceso") ?: ""

            SesionEnCursoScreen(
                idSesion = idSesion,
                acceso = acceso,
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) { popUpTo(Ruta.MisSesiones.ruta) { inclusive = true } }
                },
                onIrCrearCuenta = { navController.navigate(Ruta.Formulario.ruta) },
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    SesionPreferencias(contexto).limpiar()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                onRegistroExitoso = { usuario ->
                    Toast.makeText(contexto, "Cuenta creada para ${usuario.nombre}", Toast.LENGTH_LONG).show()
                    navController.navigateUp()
                },
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) { popUpTo(Ruta.MisSesiones.ruta) { inclusive = true } }
                },
                onIrCrearCuenta = { },
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    SesionPreferencias(contexto).limpiar()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}