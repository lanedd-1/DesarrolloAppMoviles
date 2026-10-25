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
import com.example.rinconsur.ui.screens.DetalleResumenScreen
import com.example.rinconsur.ui.screens.FormularioScreen
import com.example.rinconsur.ui.screens.GestionAccesosScreen
import com.example.rinconsur.ui.screens.GestionUsuariosScreen
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.MisSesionesScreen
import com.example.rinconsur.ui.screens.OlvideContrasenaScreen
import com.example.rinconsur.ui.screens.ResumenDiarioScreen
import com.example.rinconsur.ui.screens.SesionEnCursoScreen
import com.example.rinconsur.ui.screens.VerResumenScreen
import com.example.rinconsur.viewmodel.SesionViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val contexto = LocalContext.current
    val sesionViewModel: SesionViewModel = viewModel()

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
                onIrGestionAccesos = { navController.navigate(Ruta.GestionAccesos.ruta) },
                onIrGestionUsuarios = { navController.navigate(Ruta.GestionUsuarios.ruta) },
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    SesionPreferencias(contexto).limpiar()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Ruta.GestionAccesos.ruta) {
            GestionAccesosScreen(onVolver = { navController.navigateUp() })
        }


        composable(Ruta.ResumenDiario.ruta) {
            ResumenDiarioScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() },
                onIrVerResumen = { acceso, fecha ->
                    navController.navigate(Ruta.VerResumen.crear(Uri.encode(acceso), Uri.encode(fecha)))
                },
                onIrEditarResumen = { acceso, fecha ->
                    navController.navigate(Ruta.DetalleResumen.crear(Uri.encode(acceso), Uri.encode(fecha)))
                }
            )
        }

        composable(Ruta.GestionUsuarios.ruta) {
            GestionUsuariosScreen(
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(
            route = Ruta.VerResumen.ruta,
            arguments = listOf(
                navArgument("acceso") { type = NavType.StringType },
                navArgument("fecha") { type = NavType.StringType }
            )
        ) { entrada ->
            val acceso = Uri.decode(entrada.arguments?.getString("acceso").orEmpty())
            val fecha = Uri.decode(entrada.arguments?.getString("fecha").orEmpty())

            VerResumenScreen(
                acceso = acceso,
                fecha = fecha,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(
            route = Ruta.DetalleResumen.ruta,
            arguments = listOf(
                navArgument("acceso") { type = NavType.StringType },
                navArgument("fecha") { type = NavType.StringType }
            )
        ) { entrada ->
            val acceso = Uri.decode(entrada.arguments?.getString("acceso").orEmpty())
            val fecha = Uri.decode(entrada.arguments?.getString("fecha").orEmpty())

            DetalleResumenScreen(
                acceso = acceso,
                fecha = fecha,
                usuarioActual = usuarioActual,
                onVolver = { navController.navigateUp() }
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
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.MisSesiones.ruta) { inclusive = true }
                    }
                },
                onIrCrearCuenta = { navController.navigate(Ruta.Formulario.ruta) },
                onIrGestionUsuarios = { navController.navigate(Ruta.GestionUsuarios.ruta) },
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    SesionPreferencias(contexto).limpiar()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                usuarioActual = usuarioActual,
                onRegistroExitoso = { _ -> },
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.MisSesiones.ruta) { inclusive = true }
                    }
                },
                onIrCrearCuenta = { },
                onIrResumenDiario = { navController.navigate(Ruta.ResumenDiario.ruta) },
                onIrGestionAccesos = { navController.navigate(Ruta.GestionAccesos.ruta) },
                onIrGestionUsuarios = { navController.navigate(Ruta.GestionUsuarios.ruta) },
                onCerrarSesion = {
                    sesionViewModel.cerrarSesion()
                    SesionPreferencias(contexto).limpiar()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}