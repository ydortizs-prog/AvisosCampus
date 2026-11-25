# PROMPTS · Fase 2 (rama `mejora-ia`)

Mejoras hechas con ayuda de un asistente de IA sin romper la arquitectura **MVVM + Repository**.
La IA propone; yo reviso, pruebo en el emulador y corrijo lo que rompa las reglas:
nada de `Context` ni `NotificationManager` en el ViewModel, y todo receptor se quita en `onDispose`.

## Mejoras elegidas

| # | Mejora | Capas que cambian | Commit |
|---|--------|-------------------|--------|
| 1 | Dos canales: **Urgentes** (`IMPORTANCE_HIGH`) y **Avisos**; el recordatorio usa el urgente | Notificador + ViewModel (+ modelo) | `feat(canales): agrega canal Urgentes y el recordatorio lo usa` |
| 2 | Texto largo con `BigTextStyle` | Notificador | `feat(notificacion): muestra el texto completo con BigTextStyle` |
| 3 | Receptor del cargador (`ACTION_POWER_CONNECTED` / `DISCONNECTED`) con su propio switch | Receiver + ViewModel + View | `feat(receptor): escucha el cargador conectado y desconectado con su switch` |
| 4 | Nueva interfaz Material 3 | View (ui/ y theme/) | `feat(ui): nueva interfaz Material 3 con LargeTopAppBar, resumen y tarjetas elevadas` |

## Prompts

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|--------------------|---------------------|-----------------------|---------|
| «Agrega un segundo canal Urgentes con IMPORTANCE_HIGH y haz que el recordatorio use ese canal.» | Crear los dos canales en `crearCanal` y, dentro de `Notificador.mostrar`, elegir el canal con `if (aviso.origen == Origen.RECORDATORIO)`. | **Cambié** quién decide: agregué `urgente: Boolean = false` a `Aviso` y el ViewModel emite `aviso.copy(urgente = true)` en el recordatorio. El Notificador solo elige el canal según ese dato. Creé los dos canales con `createNotificationChannels`. | En MVVM el ViewModel decide **qué** se avisa y con qué urgencia; el Notificador solo **muestra**. Así el ViewModel sigue sin Context y la regla se puede probar sin Android. |
| «Haz que al expandir la notificación se vea el texto completo.» | Agregar `.setStyle(NotificationCompat.BigTextStyle().bigText(texto))`. | Lo acepté y agregué `setSubText(aviso.origen.texto)` para que se vea el origen junto al nombre de la app. | `BigTextStyle` no cambia ninguna otra capa y evita que un texto largo se corte con «…». |
| «Escucha cuando se conecta y desconecta el cargador y avisa, con su propio switch.» | Registrar el receptor en el `AndroidManifest.xml` con un `<receiver>`. | **Descarté** el registro en el Manifest: desde Android 8 `ACTION_POWER_CONNECTED` no llega a receptores declarados ahí. Lo registré en la pantalla con `DisposableEffect`, `RECEIVER_NOT_EXPORTED` y `unregisterReceiver` en `onDispose`, igual que el del modo avión. El ViewModel solo guarda `escuchandoCargador` y registra el aviso. | Funciona en Android moderno y no deja un receptor registrado al salir (no se filtra memoria). El `BroadcastReceiver` nunca entra al ViewModel. |
| «Cambia la interfaz a algo más llamativo con Material 3, sin tocar la lógica.» | `LargeTopAppBar` que se encoge al desplazar, tarjetas elevadas con ícono, resumen arriba, anillo de cuenta regresiva y `ListItem` para switches e historial. | Mantuve los textos del documento (Permitir notificaciones, Mostrar aviso, Recordar en 10 s, Cancelar, Faltan N s, Escuchar el modo avión, Sin avisos todavía) y el historial con `Column` + `forEach` en lugar de `LazyColumn`. Usé emojis en vez de agregar la librería de íconos. | La pantalla ya se desplaza: una lista con scroll dentro de otra da error. Sin dependencias extra y sin cambiar el ViewModel. |

## Revisión de lo que la IA generó

- El `PendingIntent` sigue con `FLAG_IMMUTABLE`: no necesita ser mutable.
- Los dos receptores (modo avión y cargador) se registran con `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)` y se quitan en `onDispose`.
- `viewmodel/AvisosViewModel.kt` no tiene `Context`, `NotificationManager` ni `BroadcastReceiver`.

## Verificación en el emulador (Pixel 7, Android 13+)

- Compila e instala.
- **Recordar en 10 s**: el anillo baja de 10 a 0 y el aviso llega como notificación **emergente** (canal Urgentes).
- Ajustes → Apps → AvisosCampus → Notificaciones: aparecen los canales **Avisos** y **Urgentes**.
