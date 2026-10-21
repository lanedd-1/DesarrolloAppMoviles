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
        // 1. Pantalla de Inicio de Sesión
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla Principal de Trabajo (Mis Sesiones)
        composable(Ruta.MisSesiones.ruta) {
            MisSesionesScreen(
                onIniciarSesionConteo = { acceso ->
                    navController.navigate(Ruta.SesionEnCurso.crear(acceso))
                },
                onAbrirMenu = {
                    navController.navigate(Ruta.Formulario.ruta)
                }
            )
        }

        // 3. Pantalla de Observación Táctil con parámetro en ruta (igual a Editar en el profesor)
        composable(
            route = Ruta.SesionEnCurso.ruta,
            arguments = listOf(navArgument("acceso") { type = NavType.StringType })
        ) {
            SesionEnCursoScreen(
                onVolver = { navController.navigateUp() }
            )
        }

        // 4. Formulario de Creación de Colaborador (5 Validaciones)
        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                onRegistroExitoso = { _ ->
                    navController.navigateUp()
                },
                onVolver = { navController.navigateUp() }
            )
        }
    }
}