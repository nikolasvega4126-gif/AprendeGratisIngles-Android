# Bluelingo 8.3.0 · Aventura en Londres

Proyecto Android nativo (`com.aprendegratisingles.app`).

La primera fase del rediseño estilo videojuego implementa una **ruta de diez lecciones** en dos tramos, con ilustración nocturna de Londres, personaje azul, orbes 3D, niveles con desbloqueo real, XP, monedas, rachas y cofres reclamables una sola vez. Los nodos son vistas Android interactivas (no capturas con zonas táctiles falsas). Los ejercicios existentes conservan su funcionalidad, audio estadounidense y guías de pronunciación para principiantes.

Video de inicio y onboarding de jugador de 8.2.x se mantienen. Se agregó el acceso público a la política de privacidad desde Perfil.

**Compilación:** GitHub Actions → `Build and validate Android APK` → artifact `Bluelingo-v8.3.0-apk`; para la tienda `Build unsigned Play AAB v8.3.0` y firmar el AAB con **el keystore de subida original**. Código de versión `46` y nombre de versión `8.3.0`. El AAB sin firmar **no sirve para subirlo directamente a Play**.

Esta entrega contiene proyecto fuente y arte incluido; no incluye APK firmado ni certificación de que el build haya pasado un emulador. Consultar `REDISENO_AVENTURA_V8_3_0.md`.
