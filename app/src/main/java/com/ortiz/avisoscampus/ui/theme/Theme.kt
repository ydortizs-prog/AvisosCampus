package com.ortiz.avisoscampus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Color.White,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = AzulOscuro,
    secondary = Turquesa,
    onSecondary = Color.White,
    secondaryContainer = TurquesaContenedor,
    onSecondaryContainer = Color(0xFF00423B),
    tertiary = Ambar,
    onTertiary = Color.White,
    tertiaryContainer = AmbarContenedor,
    onTertiaryContainer = Color(0xFF5C3A00),
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFE7EBF3),
    onSurfaceVariant = Color(0xFF4B5563),
    error = Rojo,
    errorContainer = Color(0xFFFDE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulNoche,
    onPrimary = AzulOscuro,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = AzulContenedor,
    secondary = Color(0xFF5EEAD4),
    secondaryContainer = Color(0xFF00423B),
    onSecondaryContainer = TurquesaContenedor,
    tertiary = Color(0xFFFCD34D),
    tertiaryContainer = Color(0xFF5C3A00),
    onTertiaryContainer = AmbarContenedor,
    background = FondoNoche,
    surface = SuperficieNoche,
    surfaceVariant = Color(0xFF263042),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFDE2E2)
)

// Bordes más redondeados en tarjetas y botones
private val Formas = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun AvisosCampusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}
