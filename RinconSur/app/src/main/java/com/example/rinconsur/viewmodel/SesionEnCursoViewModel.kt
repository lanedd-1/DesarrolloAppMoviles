package com.example.rinconsur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.R
import com.example.rinconsur.data.ApiException
import com.example.rinconsur.data.VisitanteRepository
import com.example.rinconsur.model.ClasificacionPersona
import com.example.rinconsur.model.RangoEtarioEnum
import com.example.rinconsur.model.SesionEnCursoUiState
import com.example.rinconsur.model.TipoCompaniaEnum
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SesionEnCursoViewModel(
    private val repositorio: VisitanteRepository = VisitanteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionEnCursoUiState())
    val uiState: StateFlow<SesionEnCursoUiState> = _uiState.asStateFlow()

    private val historialRegistros = mutableListOf<String>()

    fun onClasificacionSeleccionada(opcion: ClasificacionPersona) {
        _uiState.update { it.copy(clasificacionSeleccionada = opcion) }
    }

    fun onRangoEtarioSeleccionado(rango: RangoEtarioEnum) {
        _uiState.update { it.copy(rangoEtarioSeleccionado = rango) }
    }

    fun onCompaniaSeleccionada(compania: TipoCompaniaEnum) {
        _uiState.update { it.copy(companiaSeleccionada = compania) }
    }

    fun onBolsasOtraTiendaCambia(traeBolsas: Boolean) {
        _uiState.update { it.copy(traeBolsasOtraTienda = traeBolsas) }
    }

    fun inicializarSesion(context: Context, idSesion: Long, nombreAcceso: String) {

        _uiState.update { it.copy(idSesionActual = idSesion, accesoNombre = nombreAcceso) }

        viewModelScope.launch {
            try {
                val registrosExistentes = repositorio.listarRegistros(idSesion)

                historialRegistros.clear()

                registrosExistentes.forEach { item ->
                    val clasif = try { ClasificacionPersona.valueOf(item.clasificacion).etiqueta } catch (e: Exception) { item.clasificacion }
                    val rango = try { RangoEtarioEnum.valueOf(item.rangoEtario).etiqueta } catch (e: Exception) { item.rangoEtario }
                    val comp = try { TipoCompaniaEnum.valueOf(item.compania).etiqueta } catch (e: Exception) { item.compania }

                    historialRegistros.add("$clasif · $rango · $comp")
                }

                val total = historialRegistros.size
                val ultimoResumen = historialRegistros.lastOrNull() ?: context.getString(R.string.sesion_curso_sin_registros)

                _uiState.update {
                    it.copy(
                        totalRegistrosSesion = total,
                        ultimoRegistroResumen = ultimoResumen,
                        permiteAnular = total > 0
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeError = textoDeError(e, context)) }
            }
        }
    }

    fun onGuardarRegistro(context: Context) {
        val estado = _uiState.value
        if (estado.cargando) return

        val resumen = "${estado.clasificacionSeleccionada.etiqueta} · ${estado.rangoEtarioSeleccionado.etiqueta} · ${estado.companiaSeleccionada.etiqueta}"
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val destinoFinal = if (estado.clasificacionSeleccionada == ClasificacionPersona.TRANSITO) {
            estado.destinoSeleccionado.codigo
        } else {
            null
        }

        _uiState.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                repositorio.guardarRegistro(
                    sesionId = estado.idSesionActual,
                    clasificacion = estado.clasificacionSeleccionada.name,
                    rangoEtario = estado.rangoEtarioSeleccionado.name,
                    compania = estado.companiaSeleccionada.name,
                    bolsas = estado.traeBolsasOtraTienda,
                    destino = destinoFinal,
                    hora = horaActual
                )

                historialRegistros.add(resumen)

                _uiState.update {
                    it.copy(
                        totalRegistrosSesion = it.totalRegistrosSesion + 1,
                        ultimoRegistroResumen = resumen,
                        permiteAnular = true,
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, context)) }
            }
        }
    }

    fun finalizarSesionConteo(context: Context) {
        val estado = _uiState.value
        if (estado.cargando) return

        _uiState.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                repositorio.finalizarSesion(estado.idSesionActual)
                _uiState.update { it.copy(cargando = false, sesionFinalizada = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, context)) }
            }
        }
    }

    fun onAnularUltimoRegistro(context: Context) {
        val estado = _uiState.value
        if (estado.cargando || historialRegistros.isEmpty()) return

        _uiState.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                repositorio.anularUltimoRegistro(estado.idSesionActual)

                historialRegistros.removeAt(historialRegistros.size - 1)
                val resumenAnterior = historialRegistros.lastOrNull() ?: context.getString(R.string.sesion_curso_sin_registros)

                _uiState.update {
                    it.copy(
                        totalRegistrosSesion = (it.totalRegistrosSesion - 1).coerceAtLeast(0),
                        ultimoRegistroResumen = resumenAnterior,
                        permiteAnular = historialRegistros.isNotEmpty(),
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensajeError = textoDeError(e, context)) }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    fun consumirSesionFinalizada() {
        _uiState.update { it.copy(sesionFinalizada = false) }
    }

    private fun textoDeError(e: Exception, context: Context): String =
        if (e is ApiException) e.textoParaUsuario(context) else context.getString(R.string.error_inesperado)
}