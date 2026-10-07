# Aprende Gratis Inglés · V6.6.0 Rebuild nativo

Esta versión reemplaza la arquitectura de “captura completa + zonas invisibles”.

## Qué es real y clicable
- Ruta de Unidad 1 con 3 lecciones y examen.
- Desbloqueo progresivo guardado en SharedPreferences.
- Ejercicios de banco de palabras, selección, escucha y pronunciación.
- Text-to-Speech en inglés.
- Reconocimiento de voz con permiso de micrófono.
- XP, corazones, racha y progreso persistente.
- Navegación real: Ruta, Practicar, Logros y Perfil.
- Feedback correcto/incorrecto con reintento y avance.

## Unidad 1
1. Saludos en inglés
2. Di de dónde eres
3. Números del 1 al 10
4. Examen de unidad

Los ejemplos de países usan Polonia, Francia, España, Alemania, Italia e Inglaterra.

## Visual
Las ilustraciones de Londres se usan solamente como fondo decorativo. Los botones, tarjetas, palabras, navegación, progreso, parlante y micrófono son vistas Android reales.

## Validación
El workflow `Build and validate Android APK` hace dos trabajos:
1. Compila la APK.
2. Arranca un emulador Pixel 6, instala la app, verifica que aparezca `Saludos en inglés`, toca esa lección y confirma que se abre `Traduce esta oración`, `COMPROBAR` y `USAR MICRÓFONO`.

Además sube las capturas `01-route.png` y `02-exercise.png` como artefacto `AprendeGratisIngles-v6.6.0-ui-validation`.

Un build verde en `build` comprueba compilación. La validación completa exige también que `emulator-smoke-test` termine en verde.
