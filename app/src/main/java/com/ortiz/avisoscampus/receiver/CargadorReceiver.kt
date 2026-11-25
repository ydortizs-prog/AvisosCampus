package com.ortiz.avisoscampus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Escucha cuando se conecta o desconecta el cargador.
// Igual que el del modo avión: onReceive es corto y solo pasa el dato.
class CargadorReceiver(private val alCambiar: (Boolean) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> alCambiar(true)
            Intent.ACTION_POWER_DISCONNECTED -> alCambiar(false)
        }
    }
}
