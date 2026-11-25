package com.ortiz.avisoscampus.model

// De dónde salió el aviso
enum class Origen(val texto: String) {
    BOTON("Botón"),
    RECORDATORIO("Recordatorio"),
    MODO_AVION("Modo avión"),
    CARGADOR("Cargador")
}

// Un aviso del historial
data class Aviso(
    val id: Int,
    val titulo: String,
    val texto: String,
    val origen: Origen,
    val hora: String,
    val urgente: Boolean = false // true = va por el canal Urgentes (emergente)
)

// Estado del permiso de notificaciones que ve la pantalla
sealed interface PermisoUi {
    data object SinPedir : PermisoUi
    data object Concedido : PermisoUi
    data object Denegado : PermisoUi
}
