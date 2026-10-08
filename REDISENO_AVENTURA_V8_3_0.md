# Bluelingo 8.3.0: mapa jugable

## Implementado

- Nuevo mapa vertical nocturno con el estilo visual de las referencias aportadas por el creador.
- Paisaje y pájaro como arte de fondo; botones de nivel, etiquetas, candados, progreso, cofre y navegación como Vistas Android **independientes**, no zonas invisibles de la ilustración.
- Dos tramos de 5 lecciones cada uno; recorrido de abajo hacia arriba. Inicio situado automáticamente en el tramo que corresponde al progreso.
- Diez lecciones conectadas a los ejercicios originales. Estado inicial, desbloqueado y completado persistido en SharedPreferences.
- Los 2 cofres solo se abren cuando se completan las 5 lecciones anteriores; dan +35 monedas y +20 XP una sola vez.
- Barra superior con valores reales: vidas, racha, monedas y XP.
- Cabecera de juego con nivel siguiente, acceso a selector de tramo.
- Botones tipo arcade 3D dibujados de forma nativa, con brillo y estados.
- Video de inicio, onboarding, TTS de Estados Unidos, pantallas de estudio, Pronunciación sencilla, Sonidos y Perfil conservados.

## Estado y limitaciones

Este archivo es **código fuente**, no APK ni AAB firmados. Es necesario compilar con Android SDK/Gradle. El trabajo prioriza el mapa funcional en esta fase; los otros módulos aún usan los diseños de 8.2.1. Las ilustraciones preparadas a partir de las referencias conservan paisaje y mascota y llevan retocado el corredor central sobre el que se superponen los botones nativos. No se ha prometido identidad píxel a píxel con las imágenes conceptuales.

Si se sube una actualización a Google Play hay que compilar el AAB con versionCode 46 y firmar con la **misma clave** de subida anterior. No sustituir el keystore original.
- Botón de política de privacidad accesible desde Perfil que abre la URL pública en el navegador Android.
