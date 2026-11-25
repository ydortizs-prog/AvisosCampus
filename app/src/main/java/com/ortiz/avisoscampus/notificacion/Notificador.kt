package com.ortiz.avisoscampus.notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ortiz.avisoscampus.model.Aviso

object Notificador {

    const val CANAL = "avisos"

    // Android 8+ exige un canal para mostrar notificaciones
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL, "Avisos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos de AvisosCampus"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal) // si ya existe, no hace nada
        }
    }

    // TODO Paso 2: fun tienePermiso(context: Context): Boolean

    fun mostrar(context: Context, aviso: Aviso): Boolean {
        // TODO Paso 3: construir y mostrar la notificación
        return false
    }
}
