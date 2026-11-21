package com.example.rinconsur.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    private const val URL_USUARIOS = "http://10.0.2.2:8081/"
    private const val URL_VISITANTES = "http://10.0.2.2:8082/"
    private const val URL_ADMINISTRACION = "http://10.0.2.2:8083/"
    private const val URL_DASHBOARD = "http://10.0.2.2:8084/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarioApi: UsuarioApi by lazy { crear(URL_USUARIOS).create(UsuarioApi::class.java) }

    val visitanteApi: VisitanteApi by lazy { crear(URL_VISITANTES).create(VisitanteApi::class.java) }

    val administracionApi: AdministracionApi by lazy { crear(URL_ADMINISTRACION).create(AdministracionApi::class.java) }

    val dashboardApi: DashboardApi by lazy { crear(URL_DASHBOARD).create(DashboardApi::class.java) }
}