package com.ortiz.avisoscampus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ortiz.avisoscampus.model.Aviso
import com.ortiz.avisoscampus.model.Origen

/** Tarjeta Historial: no conoce el ViewModel, solo recibe la lista. */
@Composable
fun HistorialAvisos(avisos: List<Aviso>) {
    Tarjeta(
        titulo = "Historial",
        emoji = "🗂️",
        descripcion = if (avisos.isEmpty()) null else "Los ${minOf(avisos.size, 20)} más recientes, arriba lo último."
    ) {
        if (avisos.isEmpty()) {
            Text(
                text = "Sin avisos todavía",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Column con forEach (pocas filas): la pantalla ya se desplaza
            Column {
                avisos.take(20).forEachIndexed { i, aviso ->
                    if (i > 0) HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    FilaAviso(aviso)
                }
            }
        }
    }
}

@Composable
private fun FilaAviso(aviso: Aviso) {
    val (emoji, color) = estiloOrigen(aviso.origen)
    ListItem(
        overlineContent = {
            Text(aviso.origen.texto, color = color, fontWeight = FontWeight.Bold)
        },
        headlineContent = { Text(aviso.titulo, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(aviso.texto) },
        leadingContent = { IconoCirculo(emoji, color.copy(alpha = 0.15f), 38) },
        trailingContent = {
            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    text = aviso.hora,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// Un emoji y un color por cada origen
@Composable
private fun estiloOrigen(origen: Origen): Pair<String, Color> = when (origen) {
    Origen.BOTON -> "🔔" to MaterialTheme.colorScheme.primary
    Origen.RECORDATORIO -> "⏰" to MaterialTheme.colorScheme.tertiary
    Origen.MODO_AVION -> "✈️" to MaterialTheme.colorScheme.secondary
    Origen.CARGADOR -> "🔌" to Color(0xFF16A34A)
}
