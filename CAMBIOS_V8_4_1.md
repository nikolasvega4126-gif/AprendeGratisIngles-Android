# Bluelingo 8.4.1 | Actualización visual puntual

Base **idéntica** al proyecto 8.4.0 salvo los cambios listados.

1. En **Practicar**, reemplaza la portada anterior por la imagen aprobada `aprende_y_repite_en_londres.png`, convertida en `hero_practice_841.png`. La imagen está montada sobre un `ImageButton` nativo que llama a `startLessonFromPractice(firstUnlockedPracticeLesson())`.
2. En **Sonidos**, reemplaza la portada anterior por la imagen aprobada `mejora_tu_pronunciación_en_londres.png`, convertida en `hero_sounds_841.png`. Ese `ImageButton` llama a `startSoundQuiz()`.
3. Cambia icono/launcher y mascot del logo a `ave_azul_con_libro_británico.png` con fondo azul, **sin fondo blanco**, en densidades de Android y formato adaptive icon para Android 8+.
4. Avanza a `versionCode=48`, `versionName=8.4.1`.

Se conservan sin cambio el mapa jugable, onboarding, video, lecciones, microp, XP, recompensas, perfil, fondos, seis ilustraciones de CAT/CUP/SIT/SEE/BED/CAR, y pronunciación estadounidense.

Las portadas 2:1 contienen texto y botón dibujado; se muestran mediante `ImageButton` real, por lo que toda la tarjeta es una superficie táctil accesible que invoca la acción original. Se preserva la relación de aspecto. Para que el texto escale de modo independiente del arte, habría que crear una próxima versión con capas separadas.

Para crear APK de prueba, subir los archivos descomprimidos a GitHub y correr la acción **Build and validate Android APK**. No se incluye APK compilado, ni se afirma haber probado en teléfono. Para Google Play, usar AAB firmado con la misma clave de subida histórica y mantener el `versionCode` ascendente.
