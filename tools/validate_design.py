"""Comprobaciones estáticas de seguridad visual del rediseño Bluelingo.
No reemplazan el build ni el smoke-test del emulador.
"""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root=Path(__file__).resolve().parents[1]
java=(root/'app/src/main/java/com/aprendegratisingles/app/MainActivity.java').read_text(encoding='utf-8')
gradle=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
assets=root/'app/src/main/res/drawable-nodpi'
checks={
    'versionCode 47': 'versionCode = 47' in gradle,
    'versionName 8.4.0': 'versionName = "8.4.0"' in gradle,
    'video de inicio presente': (root/'app/src/main/res/raw/startup.mp4').is_file(),
    'retorno de 3 minutos': 'REENTRY_SPLASH_MS' in java and '3L * 60L * 1000L' in java,
    'onboarding de cuatro pasos': all(x in java for x in [
        'showOnboardingUsername()', 'showOnboardingLevel()',
        'showOnboardingGoal()', 'showOnboardingDailyGoal()']),
    'pantallas de navegación': all(x in java for x in [
        'private void showRoute()', 'private void showPractice()',
        'private void showSounds()', 'private void showProfile()']),
    'controles nativos reales': 'gameMapNode(' in java and 'entry.setOnClickListener' in java,
    'bloqueo por progreso': 'prefs.getBoolean("lesson_"+(index-1),false)' in java,
    'cofre reclamable una vez': 'putBoolean("chest_"+section,true)' in java,
    'pestañas azul neón': 'private LinearLayout bottomNav' in java and 'NEON' in java,
    'dos fondos nocturnos': all((assets/f'game_london_night{suffix}.jpg').is_file() for suffix in ['', '_2']),
    'guía ilustrada': (assets/'game_guide_girl.png').is_file(),
    'seis figuras originales': all((assets/f'sound_{x}.png').is_file() for x in ['cat','cup','sit','see','bed','car']),
    'seis figuras asignadas a tarjetas': all(('R.drawable.sound_'+x) in java for x in ['cat','cup','sit','see','bed','car']),
    'pronunciación fácil': 'CÓMO SUENA' in java,
    'inglés EEUU': '"en-US"' in java and 'Locale.US' in java,
    'privacidad dentro de Perfil': 'politica-de-privacidad-de-bluelingo.html' in java,
    'sin WebView en las lecciones': 'new WebView' not in java and '.loadUrl(' not in java,
    'ejercicios interactivos': 'checkCurrentAnswer()' in java and 'chooseSoundAnswer(' in java,
    'scroll en la prueba de sonidos': 'quizScroll.addView(content' in java,
}
ET.parse(root/'app/src/main/AndroidManifest.xml')
checks['manifest XML válido']=True
for k,v in checks.items():
    print(('OK' if v else 'ERROR')+' | '+k)
if not all(checks.values()):raise SystemExit('VALIDACIÓN ESTÁTICA FALLIDA')
print(f'PASS: {len(checks)} comprobaciones estáticas.')
