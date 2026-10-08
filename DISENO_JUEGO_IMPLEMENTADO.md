# Sistema visual Bluelingo · 8.4.0

El sistema reproduce la dirección artística de las referencias del usuario con fondo de islas/cascadas/Londres de noche y controles nativos separados.

| Sección | Implementación |
|---|---|
| Video y acceso | Video original, 3 minutos al volver, onboarding primer inicio |
| @Usuario | Campo editable, persistencia local |
| Nivel, objetivo, tiempo | Tarjetas de selección con progreso del onboarding |
| Aventura | HUD, cabecera azul, mapa con 10 niveles, cofres y estados de desbloqueo |
| Practicar | Portada con pajarito + guía, pestañas nativas, lecciones y pronunciación |
| Lecciones | Frases, traducciones y audios TTS en inglés de Estados Unidos |
| Ejercicios | Barra de progreso, palabras tocables, elección de opciones, micrófono |
| Sonidos | 6 imágenes originales exactamente de la referencia (no reemplazadas por emojis), más sonidos adicionales |
| Reto auditivo | 10 preguntas aleatorias, opciones reales, puntuación y XP |
| Perfil | Avatar personalizable, estadísticas guardadas, nivel, logros y privacidad |

## Imágenes conservadas (pestaña Sonidos)

- `sound_cat.png`: gato, A abierta de CAT.
- `sound_cup.png`: taza roja, A central de CUP.
- `sound_sit.png`: niño, I corta de SIT.
- `sound_see.png`: ojo, I larga de SEE.
- `sound_bed.png`: cama, E abierta de BED.
- `sound_car.png`: carro rojo, AR con R inglesa de CAR.

Los seis recursos fueron recortados de la referencia original suministrada y se muestran dentro de tarjetas reales Android. La lección de sonido usa TTS US. Las aproximaciones escritas ayudan, pero el audio es la referencia correcta.

## Limitaciones conocidas

- La interfaz usa degradados y bordes nativos para aproximar el brillo 3D. No se ha validado pixel por pixel contra la maqueta en un teléfono.
- No se ha compilado en este entorno porque no hay Android SDK; la CI de GitHub incluye una compilación y prueba de emulador.
- Monedas, XP, corazones y lecciones son datos locales, no pagos ni juego online.
