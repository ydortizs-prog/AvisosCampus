package com.ortiz.avisoscampus.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ortiz.avisoscampus.model.PermisoUi
import com.ortiz.avisoscampus.notificacion.Notificador
import com.ortiz.avisoscampus.viewmodel.AvisosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAvisos(vm: AvisosViewModel) {
    val contexto = LocalContext.current
    val permiso by vm.permiso.collectAsState()

    // Cuadro de permiso que dibuja Android
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        vm.permisoRespondido(concedido)
    }

    // Al iniciar: si el permiso ya estaba concedido, se lo decimos al ViewModel
    LaunchedEffect(Unit) {
        if (Notificador.tienePermiso(contexto)) vm.permisoRespondido(true)
    }

    // Ya viene hecho: la pantalla recibe el evento y, como sí tiene Context, muestra la notificación
    LaunchedEffect(Unit) {
        vm.eventos.collect { aviso ->
            val mostrado = Notificador.mostrar(contexto, aviso)
            // TODO Paso 8: si no se mostró, avisar al ViewModel
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AvisosCampus") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Tarjeta("Permiso de notificaciones") {
                EtiquetaPermiso(permiso)
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.permisoRespondido(true) // antes de Android 13 no se pide
                    }
                }) {
                    Text("Permitir notificaciones")
                }
            }
            Tarjeta("Avisos") {
                // TODO Paso 3: botón "Mostrar aviso"
                Text("Sin funciones todavía")
            }
            Tarjeta("Recordatorio") {
                // TODO Paso 5: "Recordar en 10 s", "Cancelar" y "Faltan N s"
                Text("Sin funciones todavía")
            }
            Tarjeta("Receptor de anuncios") {
                // TODO Paso 6: Switch "Escuchar el modo avión"
                Text("Sin funciones todavía")
            }
            // TODO Paso 7: tarjeta Historial (HistorialAvisos)
        }
    }
}

/** Card con el título en azul: una por función. */
@Composable
fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}

/** Etiqueta de color con el estado del permiso. */
@Composable
fun EtiquetaPermiso(permiso: PermisoUi) {
    val (texto, color) = when (permiso) {
        PermisoUi.SinPedir -> "Sin pedir" to Color(0xFF757575)
        PermisoUi.Concedido -> "Permitido" to Color(0xFF2E7D32)
        PermisoUi.Denegado -> "Denegado" to Color(0xFFC62828)
    }
    Surface(color = color, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
