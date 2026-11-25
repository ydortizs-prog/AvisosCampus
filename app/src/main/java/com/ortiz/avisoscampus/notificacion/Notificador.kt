package com.ortiz.avisoscampus.notificacion

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ortiz.avisoscampus.MainActivity
import com.ortiz.avisoscampus.R
import com.ortiz.avisoscampus.model.Aviso

object Notificador {

    const val CANAL = "avisos"
    const val CANAL_URGENTE = "urgentes"

    // Android 8+ exige un canal para mostrar notificaciones.
    // Creamos dos: Avisos (normal) y Urgentes (emergente, aparece arriba de la pantalla)
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val avisos = NotificationChannel(CANAL, "Avisos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos de AvisosCampus"
            }
            val urgentes = NotificationChannel(CANAL_URGENTE, "Urgentes", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Recordatorios que aparecen como notificación emergente"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(avisos, urgentes)) // si ya existen, no hace nada
        }
    }

    // Android 13+ (API 33) pide el permiso POST_NOTIFICATIONS en tiempo de ejecución
    fun tienePermiso(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Devuelve true si se mostró; false si no hay permiso (sin lanzar error)
    @SuppressLint("MissingPermission") // el permiso se revisa con tienePermiso
    fun mostrar(context: Context, aviso: Aviso): Boolean {
        if (!tienePermiso(context)) return false

        // Al tocar la notificación se abre la app
        val intent = Intent(context, MainActivity::class.java)
        val pendiente = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // El ViewModel decide si el aviso es urgente; aquí solo elegimos el canal
        val canal = if (aviso.urgente) CANAL_URGENTE else CANAL
        val prioridad = if (aviso.urgente) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            // Al expandir la notificación se ve el texto completo, sin cortarse
            .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.texto))
            .setSubText(aviso.origen.texto) // muestra el origen junto al nombre de la app
            .setPriority(prioridad) // en Android 7 o menos, la prioridad hace el papel del canal
            .setContentIntent(pendiente)
            .setAutoCancel(true) // se quita al tocarla
            .build()

        // El id del aviso hace que cada notificación sea distinta
        NotificationManagerCompat.from(context).notify(aviso.id, notificacion)
        return true
    }
}
