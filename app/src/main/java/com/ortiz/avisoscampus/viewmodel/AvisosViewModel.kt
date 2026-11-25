package com.ortiz.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ortiz.avisoscampus.data.AvisosRepository
import com.ortiz.avisoscampus.model.Aviso
import com.ortiz.avisoscampus.model.Origen
import com.ortiz.avisoscampus.model.PermisoUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvisosViewModel(private val repo: AvisosRepository) : ViewModel() {

    val avisos: StateFlow<List<Aviso>> = repo.avisos

    // Evento de una sola vez: "muestra esta notificación".
    // El ViewModel NO tiene Context: lo recibe la pantalla.
    private val _eventos = MutableSharedFlow<Aviso>()
    val eventos: SharedFlow<Aviso> = _eventos.asSharedFlow()

    private val _permiso = MutableStateFlow<PermisoUi>(PermisoUi.SinPedir)
    val permiso: StateFlow<PermisoUi> = _permiso.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // ---------- Paso 2: permiso ----------
    fun permisoRespondido(concedido: Boolean) {
        _permiso.value = if (concedido) PermisoUi.Concedido else PermisoUi.Denegado
        if (concedido) _mensaje.value = null // con permiso, el mensaje de error se quita solo
    }

    // ---------- Paso 3: aviso con el botón ----------
    fun avisarAhora() {
        viewModelScope.launch {
            val aviso = repo.registrar("Tarea lista", "Tu entrega fue registrada", Origen.BOTON)
            _eventos.emit(aviso) // la pantalla lo recibe y muestra la notificación
        }
    }

    // ---------- Paso 5: recordatorio con cuenta regresiva ----------
    private var trabajo: Job? = null

    // Segundos que faltan, o null si no hay recordatorio activo
    private val _faltan = MutableStateFlow<Int?>(null)
    val faltan: StateFlow<Int?> = _faltan.asStateFlow()

    fun recordarEn(segundos: Int = 10) {
        if (trabajo?.isActive == true) return // ya hay uno corriendo
        trabajo = viewModelScope.launch {
            try {
                for (s in segundos downTo 1) {
                    _faltan.value = s
                    delay(1000)
                }
                val aviso = repo.registrar(
                    "Recordatorio", "Revisa tu laboratorio pendiente", Origen.RECORDATORIO
                )
                _eventos.emit(aviso.copy(urgente = true)) // el recordatorio va por el canal Urgentes
            } finally {
                _faltan.value = null // termine bien o cancelado
            }
        }
    }

    fun cancelarRecordatorio() {
        trabajo?.cancel() // no se emite ningún aviso
    }

    // ---------- Paso 6: receptor del modo avión ----------
    // El ViewModel solo guarda si se escucha o no; el registro lo hace la pantalla
    private val _escuchandoAvion = MutableStateFlow(false)
    val escuchandoAvion: StateFlow<Boolean> = _escuchandoAvion.asStateFlow()

    fun cambiarEscucha(valor: Boolean) {
        _escuchandoAvion.value = valor
    }

    fun alCambiarModoAvion(activo: Boolean) {
        viewModelScope.launch {
            val aviso = repo.registrar(
                "Modo avión", if (activo) "Activado" else "Desactivado", Origen.MODO_AVION
            )
            _eventos.emit(aviso)
        }
    }

    // ---------- Paso 8: permiso denegado ----------
    fun noSePudoMostrar() {
        _mensaje.value = "Sin permiso: el aviso se guardó en el historial, pero no se mostró"
    }
}
