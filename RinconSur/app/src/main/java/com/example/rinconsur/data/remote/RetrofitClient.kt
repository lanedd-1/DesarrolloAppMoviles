package com.example.rinconsur.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    // Un solo punto de entrada: el API Gateway reparte cada ruta al microservicio que corresponde.
    // 10.0.2.2 es el computador visto desde el emulador. En un celular físico va la IP del computador (ej. 192.168.1.50).
    private const val URL_GATEWAY = "http://10.0.2.2:8080/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarioApi: UsuarioApi by lazy { crear(URL_GATEWAY).create(UsuarioApi::class.java) }

    val visitanteApi: VisitanteApi by lazy { crear(URL_GATEWAY).create(VisitanteApi::class.java) }

    val registroApi: RegistroApi by lazy { crear(URL_GATEWAY).create(RegistroApi::class.java) }

    val administracionApi: AdministracionApi by lazy { crear(URL_GATEWAY).create(AdministracionApi::class.java) }

    val dashboardApi: DashboardApi by lazy { crear(URL_GATEWAY).create(DashboardApi::class.java) }
}