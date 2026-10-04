# Aprende Gratis Inglés V6.0

V6.0 parte de la V5.2 estable y conserva todo el progreso/local storage existente.

## Cambios de V6.0

- Nombre de versión: `6.0` / `versionCode 11`.
- `compileSdk 36` y `targetSdk 36` para la publicación actual en Google Play.
- Android Gradle Plugin 8.9.1 y Gradle 8.11.1.
- El ejemplo de nombre de usuario ahora es `@Usuario`.
- El perfil muestra `Versión 6.0 · Aprende Gratis Inglés`.
- Corrección del cálculo de avatar para evitar índices negativos extremos.
- Ajuste de márgenes de sistema para Android moderno/edge-to-edge.
- Categoría Android: educación.
- GitHub Actions compila un APK de prueba y un AAB release.

## Funciones que conserva

- Intro animada.
- Creación de nombre local y avatar.
- Ruta de aprendizaje.
- XP, niveles, racha, vidas y Liga XP local.
- Prácticas y exámenes.
- Pronunciación con micrófono usando `SpeechRecognizer` dentro de la app.
- Texto a voz.
- Favoritos y búsqueda de lecciones.
- Contenido dinámico desde aprendegratisingles.com.
- Repetición espaciada y vocabulario personal.
- Conversaciones de práctica.
- Calendario de estudio.
- Copia/restauración del progreso.
- Recordatorios.

## Importante sobre la Liga XP

La clasificación de esta versión sigue siendo local. No representa usuarios reales de otros teléfonos. Para un ranking real hace falta un backend y control del XP en servidor.

## Google Play

El workflow genera `app-release.aab`, pero ese AAB no queda firmado si no se configura una clave de subida. Para Google Play, el AAB final debe firmarse con una upload key propia. No publiques ni compartas tu archivo keystore ni sus contraseñas.
