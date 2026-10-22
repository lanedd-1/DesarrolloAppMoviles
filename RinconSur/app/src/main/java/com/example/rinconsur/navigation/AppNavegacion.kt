package com.example.rinconsur.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rinconsur.ui.screens.FormularioScreen
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.MisSesionesScreen
import com.example.rinconsur.ui.screens.SesionEnCursoScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {
        // 1. Pantalla de Inicio de Sesión (Sin Menú Lateral)
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        // 2. Mis Sesiones
        composable(Ruta.MisSesiones.ruta) {
            MisSesionesScreen(
                onIniciarSesionConteo = { acceso ->
                    navController.navigate(Ruta.SesionEnCurso.crear(acceso))
                },
                onIrCrearCuenta = { navController.navigate(Ruta.Formulario.ruta) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // 3. Sesión en Curso (Ahora con acceso al Menú Lateral)
        composable(
            route = Ruta.SesionEnCurso.ruta,
            arguments = listOf(navArgument("acceso") { type = NavType.StringType })
        ) {
            SesionEnCursoScreen(
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.MisSesiones.ruta) { inclusive = true } // Evita ciclos infinitos
                    }
                },
                onIrCrearCuenta = { navController.navigate(Ruta.Formulario.ruta) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // 4. Formulario (Ahora con acceso al Menú Lateral)
        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                onRegistroExitoso = { _ -> navController.navigateUp() },
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.MisSesiones.ruta) { inclusive = true }
                    }
                },
                onIrCrearCuenta = { /* Ya estamos aquí, no hace nada */ },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}