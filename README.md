# Bluelingo 8.1.1 — sonidos, perfil y video de inicio

Evolución sobre la base estable 8.0.0.

## Cambios principales

- Marca visible corregida a **Bluelingo**.
- Video de inicio nativo (`res/raw/startup.mp4`) en cada arranque en frío.
- Si la app queda en segundo plano menos de 3 minutos, vuelve exactamente donde estaba sin mostrar el video.
- Si permanece fuera 3 minutos o más, el video se reproduce al regresar y después continúa en la misma pantalla.
- Barra inferior simplificada a **Ruta · Practicar · Sonidos · Perfil**.
- Nueva sección **Sonidos** con vocales y consonantes, símbolos fonéticos, ejemplos y progreso.
- Entrenamiento auditivo aleatorio de 10 preguntas por sesión, audio automático y botón para repetir.
- Banco amplio de pares auditivos; no usa siempre las mismas palabras.
- XP y progreso de pronunciación guardados localmente.
- Perfil rediseñado con avatar seleccionable desde la galería, estadísticas, nivel, logros y recordatorios.
- Practicar continúa siendo 100% nativo, sin WebView.
- Se mantienen Ruta, lecciones, TTS, micrófono, corazones, XP, racha, onboarding y notificaciones.

## Versión

- `versionName = 8.1.1`
- `versionCode = 41`


## Corrección V8.1.1
- Video de inicio renderizado con TextureView + MediaPlayer para evitar audio con pantalla negra.
- Video H.264 Baseline para mayor compatibilidad entre dispositivos Android.
- Prueba automática captura el splash durante la reproducción y rechaza una pantalla prácticamente negra.
