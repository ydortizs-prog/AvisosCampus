package com.ortiz.avisoscampus

import android.app.Application
import com.ortiz.avisoscampus.notificacion.Notificador

class AvisosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // El canal se crea una sola vez, al iniciar la app
        Notificador.crearCanal(this)
    }
}
