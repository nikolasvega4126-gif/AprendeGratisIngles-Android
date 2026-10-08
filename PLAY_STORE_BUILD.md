# Bluelingo 8.4.0 · Build para pruebas y Play Store

- Android package / applicationId: `com.aprendegratisingles.app`
- `versionName`: `8.4.0`
- `versionCode`: `47`
- `compileSdk` y `targetSdk`: `36`

## Primera prueba: APK

1. Descomprimir el ZIP y subir el contenido a la raíz de tu repositorio GitHub `nikolasvega4126-gif/AprendeGratisIngles-Android` conservando carpetas.
2. GitHub > Actions > **Build and validate Android APK**.
3. Al terminar, descargar **Bluelingo-v8.4.0-apk** desde Artifacts, descomprimir e instalar `app-debug.apk` en Android.
4. Abrir Aventura, Practicar, Sonidos y Perfil, comprobar los seis dibujos, botones de sonido, cofres, lecciones y video inicial. Revisar artefacto `Bluelingo-v8.4.0-ui-validation` cuando la tarea de emulador termine.

## Actualizar prueba cerrada Google Play

1. Solo después de validar el APK, generar bundle `.aab` con **Build unsigned Play AAB v8.4.0**.
2. El bundle del workflow es **sin firma**. Debe firmarse con el mismo keystore de subida original usado para la versión 42.
3. Verificar la firma y subir el AAB de código **47** en una nueva versión de prueba cerrada.
4. No subir un APK debug ni el AAB sin firma a Play Console.

**No incluyas la clave .jks ni sus contraseñas dentro del repositorio o del ZIP.**
