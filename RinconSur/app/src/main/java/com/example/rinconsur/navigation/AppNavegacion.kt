package com.example.rinconsur.navigation

import MisSesionesScreen
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rinconsur.ui.screens.CambiarContrasenaScreen
import com.example.rinconsur.ui.screens.FormularioScreen
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.OlvideContrasenaScreen
import com.example.rinconsur.ui.screens.SesionEnCursoScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val contexto = LocalContext.current

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
                },
                onOlvidasteContrasena = {
                    navController.navigate(Ruta.OlvideContrasena.ruta)
                }
            )
        }

        // 2. Recuperar contraseña: correo + código de verificación
        composable(route = Ruta.OlvideContrasena.ruta) {
            OlvideContrasenaScreen(
                onCodigoVerificado = { correoUsuario ->
                    // Le pasamos el correo verificado a la siguiente pantalla
                    navController.navigate(route = Ruta.CambiarContrasena.crear(correoUsuario)) {
                        popUpTo(route = Ruta.OlvideContrasena.ruta) { inclusive = true }
                    }
                },
                onVolver = { navController.navigateUp() }
            )
        }

        // 3. Nueva contraseña (recibe el correo verificado)
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

        // 4. Mis Sesiones
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

        // 5. Sesión en Curso (con acceso al Menú Lateral)
        composable(
            route = Ruta.SesionEnCurso.ruta,
            arguments = listOf(navArgument("acceso") { type = NavType.StringType })
        ) {
            SesionEnCursoScreen(
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.MisSesiones.ruta) { inclusive = true }
                    }
                },
                onIrCrearCuenta = { navController.navigate(Ruta.Formulario.ruta) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // 6. Formulario (con acceso al Menú Lateral)
        composable(Ruta.Formulario.ruta) {
            FormularioScreen(
                onRegistroExitoso = { usuario ->
                    Toast.makeText(contexto, "Cuenta creada para ${usuario.nombre}", Toast.LENGTH_LONG).show()
                    navController.navigateUp()
                },
                onVolver = { navController.navigateUp() },
                onIrMisSesiones = {
                    navController.navigate(Ruta.MisSesiones.ruta) {
                        popUpTo(Ruta.MisSesiones.ruta) { inclusive = true }
                    }
                },
                onIrCrearCuenta = { /* Ya estamos aquí */ },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}