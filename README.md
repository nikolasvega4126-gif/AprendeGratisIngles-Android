# Bluelingo 8.4.2 · Diseño de aventura (Android nativo)

Este proyecto continúa la app Bluelingo existente. Se conserva `applicationId=com.aprendegratisingles.app`.

## Diseño implementado

- Video H.264 original en `app/src/main/res/raw/startup.mp4`, entrada tras 3 min y onboarding persistente.
- Creación de jugador con @usuario, nivel, objetivo y minutos al día.
- Aventura vertical por Londres nocturno: fondos ilustrados + botones Android individuales (NO capturas táctiles), diez lecciones, candados, cofres y ruta luminosa.
- HUD compartido con vidas, racha, diamantes/monedas y XP.
- Navegación inferior azul neón: Aventura, Practicar, Sonidos, Perfil.
- Practicar con portada ilustrada, guía, pajarito y tarjetas de misiones reales.
- Sonidos con **las seis figuras originales del diseño aprobado** (gato, taza, niño, ojo, cama, carro), más las demás vocales y consonantes.
- Entrenamiento de escucha con audio, respuestas, progreso y botón Comprobar en ScrollView adaptable.
- Lecciones con audio estadounidense, pronunciación fácil sin IPA, traducción, ejercicios y micrófono.
- Perfil con XP/racha/progreso real y enlace a la política de privacidad.

## Android

- App: Bluelingo
- applicationId: `com.aprendegratisingles.app`
- VersionName: `8.4.2`
- VersionCode: `47`
- MinSdk: 24, TargetSdk: 36
- Sin WebView dentro de lecciones.

## Compilar y probar

1. Subir **los archivos descomprimidos** a la raíz del repositorio de GitHub existente.
2. GitHub > Actions > **Build and validate Android APK** > Run workflow.
3. Esperar `build` verde; descargar Artifact `Bluelingo-v8.4.2-apk`.
4. Si aparece verde el `emulator-smoke-test`, descargar `Bluelingo-v8.4.2-ui-validation` y revisar capturas.
5. Instalar el APK de pruebas en Android y comprobar los toques, el scroll, la voz y la navegación.
6. Para Google Play se requiere un **AAB release firmado con la misma clave de subida original**. El APK de depuración no se debe publicar.

El workflow alternativo `Build unsigned Play AAB` crea el bundle sin firma. Se debe firmar con la clave de subida original antes de enviarlo a Play Console.

## Alcance de la validación

`python tools/validate_design.py` verifica 21 invariantes de código/recursos y manifiesto. **No sustituye la compilación Android ni la prueba visual en teléfono.**


Para esta actualización puntual, ver `CAMBIOS_V8_4_1.md`.

### Novedades 8.4.2
- Todas las tarjetas de vocales y consonantes tienen una figura ilustrada.
- Primeras seis figuras conservadas sin modificaciones.
- Tocar una lección disponible la abre directamente.
- Botón de seis rayas cambia la sección del mapa sin diálogos.
