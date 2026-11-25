package com.ortiz.avisoscampus.ui

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ortiz.avisoscampus.model.PermisoUi
import com.ortiz.avisoscampus.notificacion.Notificador
import com.ortiz.avisoscampus.receiver.CargadorReceiver
import com.ortiz.avisoscampus.receiver.ModoAvionReceiver
import com.ortiz.avisoscampus.ui.theme.Gris
import com.ortiz.avisoscampus.ui.theme.Rojo
import com.ortiz.avisoscampus.ui.theme.Verde
import com.ortiz.avisoscampus.viewmodel.AvisosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAvisos(vm: AvisosViewModel) {
    val contexto = LocalContext.current
    val permiso by vm.permiso.collectAsState()
    val faltan by vm.faltan.collectAsState()
    val escuchando by vm.escuchandoAvion.collectAsState()
    val escuchandoCargador by vm.escuchandoCargador.collectAsState()
    val avisos by vm.avisos.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    // La barra grande se encoge al desplazar la pantalla
    val comportamiento = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Registra el receptor solo mientras el switch está activo y lo quita al salir
    DisposableEffect(escuchando) {
        if (escuchando) {
            val receptor = ModoAvionReceiver { activo -> vm.alCambiarModoAvion(activo) }
            ContextCompat.registerReceiver(
                contexto,
                receptor,
                IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose { contexto.unregisterReceiver(receptor) }
        } else {
            onDispose { }
        }
    }

    // Mejora: receptor del cargador, con su propio switch. También se quita en onDispose
    DisposableEffect(escuchandoCargador) {
        if (escuchandoCargador) {
            val receptor = CargadorReceiver { conectado -> vm.alCambiarCargador(conectado) }
            val filtro = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            ContextCompat.registerReceiver(contexto, receptor, filtro, ContextCompat.RECEIVER_NOT_EXPORTED)
            onDispose { contexto.unregisterReceiver(receptor) }
        } else {
            onDispose { }
        }
    }

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
            if (!mostrado) vm.noSePudoMostrar() // sin permiso: la app no se cierra
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(comportamiento.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("AvisosCampus", fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = "Notificaciones y anuncios del celular",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                scrollBehavior = comportamiento
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Resumen rápido arriba
            Resumen(
                total = avisos.size,
                permiso = permiso,
                receptores = listOf(escuchando, escuchandoCargador).count { it }
            )

            Tarjeta(
                titulo = "Permiso de notificaciones",
                emoji = "🔐",
                descripcion = "Android 13 o superior pide permiso para avisarte."
            ) {
                EtiquetaPermiso(permiso)
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            vm.permisoRespondido(true) // antes de Android 13 no se pide
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Permitir notificaciones")
                }
                // Recuadro de error solo si hay mensaje
                mensaje?.let { texto ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "⚠️  $texto",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedButton(onClick = { abrirAjustes(contexto) }) {
                                Text("Abrir ajustes")
                            }
                        }
                    }
                }
            }

            Tarjeta(
                titulo = "Avisos",
                emoji = "🔔",
                descripcion = "Muestra una notificación al instante."
            ) {
                FilledTonalButton(
                    onClick = { vm.avisarAhora() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mostrar aviso")
                }
            }

            Tarjeta(
                titulo = "Recordatorio",
                emoji = "⏰",
                descripcion = "Llega a los 10 segundos por el canal Urgentes."
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CuentaRegresiva(faltan = faltan, total = 10)
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { vm.recordarEn(10) },
                            enabled = faltan == null // se deshabilita al empezar
                        ) {
                            Text("Recordar en 10 s")
                        }
                        OutlinedButton(
                            onClick = { vm.cancelarRecordatorio() },
                            enabled = faltan != null // solo mientras corre
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
                faltan?.let {
                    Text(
                        text = "Faltan $it s",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Tarjeta(
                titulo = "Receptor de anuncios",
                emoji = "📡",
                descripcion = "Escucha lo que pasa en el celular mientras el switch esté activo."
            ) {
                FilaSwitch(
                    emoji = "✈️",
                    titulo = "Escuchar el modo avión",
                    detalle = "Avisa al activarlo o desactivarlo",
                    activo = escuchando,
                    alCambiar = { vm.cambiarEscucha(it) }
                )
                HorizontalDivider()
                FilaSwitch(
                    emoji = "🔌",
                    titulo = "Escuchar el cargador",
                    detalle = "Avisa al conectarlo o desconectarlo",
                    activo = escuchandoCargador,
                    alCambiar = { vm.cambiarEscuchaCargador(it) }
                )
            }

            HistorialAvisos(avisos)
        }
    }
}

/** Tarjeta elevada con ícono, título en azul y descripción: una por función. */
@Composable
fun Tarjeta(
    titulo: String,
    emoji: String = "",
    descripcion: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (emoji.isNotEmpty()) {
                    IconoCirculo(emoji, MaterialTheme.colorScheme.primaryContainer)
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    descripcion?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            contenido()
        }
    }
}

/** Círculo de color con un emoji dentro. */
@Composable
fun IconoCirculo(emoji: String, fondo: Color, tamano: Int = 42) {
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (tamano / 2).sp)
    }
}

/** Tres datos rápidos: avisos, permiso y receptores activos. */
@Composable
private fun Resumen(total: Int, permiso: PermisoUi, receptores: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DatoResumen("Avisos", total.toString(), MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
        DatoResumen(
            "Permiso",
            when (permiso) {
                PermisoUi.SinPedir -> "—"
                PermisoUi.Concedido -> "✓"
                PermisoUi.Denegado -> "✗"
            },
            MaterialTheme.colorScheme.secondaryContainer,
            Modifier.weight(1f)
        )
        DatoResumen("Receptores", "$receptores/2", MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
    }
}

@Composable
private fun DatoResumen(titulo: String, valor: String, fondo: Color, modifier: Modifier) {
    Surface(color = fondo, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Anillo de progreso con los segundos que faltan. */
@Composable
private fun CuentaRegresiva(faltan: Int?, total: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        CircularProgressIndicator(
            progress = { (faltan ?: 0) / total.toFloat() },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.tertiaryContainer,
            strokeWidth = 6.dp
        )
        Text(
            text = faltan?.toString() ?: "⏰",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Fila con emoji, texto y Switch (ListItem de Material 3). */
@Composable
private fun FilaSwitch(
    emoji: String,
    titulo: String,
    detalle: String,
    activo: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(titulo, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(detalle) },
        leadingContent = { IconoCirculo(emoji, MaterialTheme.colorScheme.secondaryContainer, 38) },
        trailingContent = { Switch(checked = activo, onCheckedChange = alCambiar) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

/** Etiqueta de color con el estado del permiso. */
@Composable
fun EtiquetaPermiso(permiso: PermisoUi) {
    val (texto, color) = when (permiso) {
        PermisoUi.SinPedir -> "Sin pedir" to Gris
        PermisoUi.Concedido -> "Permitido" to Verde
        PermisoUi.Denegado -> "Denegado" to Rojo
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = texto,
                color = color,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Abre la pantalla de notificaciones de la app en Ajustes. */
fun abrirAjustes(contexto: android.content.Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(android.net.Uri.fromParts("package", contexto.packageName, null))
    }
    contexto.startActivity(intent)
}
