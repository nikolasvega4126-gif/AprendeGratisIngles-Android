# Bluelingo V8.1.1 - Google Play

Este proyecto incluye el workflow `.github/workflows/build-play-aab.yml` para crear un Android App Bundle firmado.

## Secretos necesarios en GitHub

En el repositorio: Settings > Secrets and variables > Actions > New repository secret.

Crea estos 3 secretos:

- `BLUELINGO_UPLOAD_KEYSTORE_BASE64`: contenido completo de `bluelingo-upload-key-base64.txt`
- `BLUELINGO_UPLOAD_STORE_PASSWORD`: valor de `Keystore password` en `credentials.txt`
- `BLUELINGO_UPLOAD_KEY_PASSWORD`: valor de `Key password` en `credentials.txt`

No subas el archivo `.jks`, `credentials.txt` ni el Base64 al repositorio público.

## Compilar

GitHub > Actions > Build signed Play AAB > Run workflow.

Al terminar en verde, descarga el artefacto `Bluelingo-v8.1.1-play-aab`.
El archivo `Bluelingo-v8.1.1-play.aab` es el que se sube a Google Play Console.

## Clave de carga

Alias: `bluelingo_upload`

Guarda para siempre el `.jks` y `credentials.txt`. Se necesitarán para futuras actualizaciones de la app.
