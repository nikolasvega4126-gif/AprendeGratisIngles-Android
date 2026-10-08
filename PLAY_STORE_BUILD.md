# Bluelingo 8.3.0: compilar para Google Play

- Paquete Android: `com.aprendegratisingles.app`
- `versionName`: `8.3.0`
- `versionCode`: **46** (posterior al código 42 de la prueba cerrada)
- `compileSdk / targetSdk`: 36

## 1. APK para probar en el teléfono

Sube **los archivos descomprimidos** de este proyecto al mismo repositorio GitHub, con su estructura de carpetas intacta. GitHub Actions → **Build and validate Android APK** → ejecuta o espera a que el push dispare el flujo. Si finaliza correctamente, descarga el artefacto `Bluelingo-v8.3.0-apk` e instala `app-debug.apk` en un dispositivo de prueba.

## 2. AAB para Google Play

GitHub Actions → **Build unsigned Play AAB v8.3.0** → `Run workflow`. El archivo AAB generado es **sin firma** y todavía NO se puede subir a Play Console. Debes firmarlo con el **keystore de subida original usado para la versión 42** y verificar la firma. No crees otro keystore, porque Google podría rechazar la actualización.

El flujo antiguo `build-play-aab.yml` dependía de secretos que antes dieron errores de reconstrucción/contraseña. Este ZIP incluye la ruta de compilación sin firma para mantener separados la compilación y el firmado.

## Seguridad

Nunca subas el `.jks`, contraseñas ni texto Base64 del keystore al repositorio. Esta entrega tampoco los incluye.
