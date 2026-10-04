package com.aprendegratisingles.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class VisualGameActivity extends Activity {
    private static final String PREFS = "agi_prefs";
    private static final int MODE_ROUTE = 0;
    private static final int MODE_EXERCISE = 1;
    private static final int MODE_CORRECT = 2;
    private static final int MODE_WRONG = 3;

    private FrameLayout root;
    private FrameLayout controls;
    private ImageView screen;
    private int mode = MODE_ROUTE;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private SpeechRecognizer recognizer;
    private SharedPreferences prefs;
    private boolean choseDistractor = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        root = new FrameLayout(this);
        screen = new ImageView(this);
        screen.setScaleType(ImageView.ScaleType.FIT_XY); // Importante: coordenadas 1:1 con el mockup.
        root.addView(screen, new FrameLayout.LayoutParams(-1, -1));

        controls = new FrameLayout(this);
        root.addView(controls, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        showMode(MODE_ROUTE);
        root.post(() -> {
            safeHideSystemBars();
            initTts();
        });
    }

    private void safeHideSystemBars() {
        try {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        } catch (Throwable ignored) {}
    }

    private void initTts() {
        try {
            tts = new TextToSpeech(getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS && tts != null) {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(0.88f);
                    ttsReady = true;
                }
            });
        } catch (Throwable ignored) { ttsReady = false; }
    }

    private void showMode(int newMode) {
        mode = newMode;
        choseDistractor = false;
        controls.removeAllViews();

        if (mode == MODE_ROUTE) {
            screen.setImageResource(R.drawable.visual_route);
            buildRouteControls();
        } else if (mode == MODE_EXERCISE) {
            screen.setImageResource(R.drawable.visual_exercise);
            buildExerciseControls();
        } else if (mode == MODE_CORRECT) {
            screen.setImageResource(R.drawable.visual_correct);
            buildCorrectControls();
        } else {
            screen.setImageResource(R.drawable.visual_wrong);
            buildWrongControls();
        }
        screen.setAlpha(0f);
        screen.animate().alpha(1f).setDuration(160).start();
    }

    private void buildRouteControls() {
        // Saludos: nodo + tarjeta completos.
        hotspot(0.02f, 0.43f, 0.97f, 0.55f, "Abrir Saludos en inglés", () -> showMode(MODE_EXERCISE));

        // Lecciones bloqueadas.
        hotspot(0.12f, 0.56f, 0.98f, 0.68f, "Di de dónde eres", () -> blocked("Completa Saludos en inglés para desbloquear esta lección"));
        hotspot(0.10f, 0.68f, 0.98f, 0.81f, "Números del 1 al 10", () -> blocked("Completa las lecciones anteriores para desbloquear esta lección"));
        hotspot(0.08f, 0.80f, 0.98f, 0.91f, "Examen de unidad", () -> blocked("Completa las 3 lecciones para desbloquear el examen"));

        // Barra inferior. Ruta se queda; los demás vuelven a la app funcional existente.
        hotspot(0.00f, 0.92f, 0.25f, 1.00f, "Ruta", () -> {});
        hotspot(0.25f, 0.92f, 0.50f, 1.00f, "Practicar", this::returnToMain);
        hotspot(0.50f, 0.92f, 0.75f, 1.00f, "Logros", this::returnToMain);
        hotspot(0.75f, 0.92f, 1.00f, 1.00f, "Perfil", this::returnToMain);
    }

    private void buildExerciseControls() {
        hotspot(0.01f, 0.03f, 0.16f, 0.11f, "Cerrar", () -> showMode(MODE_ROUTE));
        hotspot(0.45f, 0.25f, 0.99f, 0.39f, "Escuchar frase", () -> {
            speak("Good morning, how are you?");
            bounce();
        });

        // Las cuatro palabras verdes son botones reales. Tocarlas da respuesta correcta.
        hotspot(0.03f, 0.51f, 0.27f, 0.58f, "Buenos", () -> chooseCorrect());
        hotspot(0.28f, 0.51f, 0.48f, 0.58f, "días", () -> chooseCorrect());
        hotspot(0.49f, 0.51f, 0.73f, 0.58f, "cómo", () -> chooseCorrect());
        hotspot(0.74f, 0.51f, 0.99f, 0.58f, "estás", () -> chooseCorrect());

        // Distractores.
        hotspot(0.03f, 0.65f, 0.27f, 0.72f, "gracias", () -> chooseWrong());
        hotspot(0.28f, 0.65f, 0.48f, 0.72f, "hola", () -> chooseWrong());
        hotspot(0.49f, 0.65f, 0.68f, 0.72f, "bien", () -> chooseWrong());

        hotspot(0.04f, 0.80f, 0.96f, 0.88f, "Comprobar", () -> {
            if (choseDistractor) showMode(MODE_WRONG);
            else { awardXp(10); showMode(MODE_CORRECT); }
        });
        hotspot(0.04f, 0.89f, 0.96f, 0.96f, "Usar micrófono", this::startListening);
    }

    private void buildCorrectControls() {
        hotspot(0.01f, 0.03f, 0.16f, 0.11f, "Cerrar", () -> showMode(MODE_ROUTE));
        hotspot(0.05f, 0.63f, 0.95f, 0.76f, "Escuchar traducción", () -> speak("Good morning, how are you?"));
        hotspot(0.05f, 0.86f, 0.95f, 0.95f, "Siguiente", () -> {
            prefs.edit().putBoolean("u1_done_0", true).apply();
            awardXp(25);
            showMode(MODE_ROUTE);
            Toast.makeText(this, "¡Lección completada! +25 XP", Toast.LENGTH_SHORT).show();
        });
    }

    private void buildWrongControls() {
        hotspot(0.01f, 0.03f, 0.16f, 0.11f, "Cerrar", () -> showMode(MODE_ROUTE));
        hotspot(0.05f, 0.60f, 0.95f, 0.73f, "Escuchar respuesta correcta", () -> speak("Good morning, how are you?"));
        hotspot(0.05f, 0.86f, 0.95f, 0.95f, "Intentar de nuevo", () -> showMode(MODE_EXERCISE));
    }

    private void chooseCorrect() {
        choseDistractor = false;
        Toast.makeText(this, "Palabra seleccionada", Toast.LENGTH_SHORT).show();
    }

    private void chooseWrong() {
        choseDistractor = true;
        Toast.makeText(this, "Palabra seleccionada", Toast.LENGTH_SHORT).show();
    }

    private void blocked(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        pulse();
    }

    private void returnToMain() {
        finish();
    }

    private void hotspot(float left, float top, float right, float bottom, String description, Runnable action) {
        Button b = new Button(this);
        b.setText("");
        b.setContentDescription(description);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setAlpha(0.01f); // Sigue siendo un Button real y clicable, pero no tapa el diseño.
        b.setGravity(Gravity.CENTER);
        b.setOnClickListener(v -> action.run());

        controls.post(() -> {
            int w = controls.getWidth();
            int h = controls.getHeight();
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    Math.max(1, Math.round((right-left)*w)),
                    Math.max(1, Math.round((bottom-top)*h))
            );
            lp.leftMargin = Math.round(left*w);
            lp.topMargin = Math.round(top*h);
            b.setLayoutParams(lp);
        });
        controls.addView(b);
    }

    private void speak(String phrase) {
        try {
            if (ttsReady && tts != null) tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "visual_" + System.currentTimeMillis());
            else Toast.makeText(this, "Preparando audio… toca de nuevo", Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) { Toast.makeText(this, "Audio no disponible", Toast.LENGTH_SHORT).show(); }
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "El reconocimiento de voz no está disponible", Toast.LENGTH_SHORT).show(); return;
        }
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 701); return;
        }
        try {
            if (recognizer != null) recognizer.destroy();
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { Toast.makeText(VisualGameActivity.this, "🎙️ Escuchando…", Toast.LENGTH_SHORT).show(); }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) { Toast.makeText(VisualGameActivity.this, "No te escuché. Intenta otra vez.", Toast.LENGTH_SHORT).show(); }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String heard = list != null && !list.isEmpty() ? list.get(0) : "";
                    if (similar(heard, "Good morning, how are you")) { awardXp(10); showMode(MODE_CORRECT); }
                    else showMode(MODE_WRONG);
                }
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            recognizer.startListening(intent);
        } catch (Throwable e) {
            Toast.makeText(this, "No se pudo iniciar el micrófono", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean similar(String heard, String expected) {
        String a = normalize(heard), b = normalize(expected);
        if (a.equals(b)) return true;
        int hit=0; String[] words=b.split(" ");
        for(String w:words) if(a.contains(w)) hit++;
        return words.length>0 && hit/(float)words.length>=0.65f;
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private void awardXp(int amount) {
        int xp=prefs.getInt("xp",0)+amount;
        int today=prefs.getInt("today_xp",0)+amount;
        prefs.edit().putInt("xp",xp).putInt("today_xp",today).apply();
    }

    private void bounce() {
        screen.animate().scaleX(1.01f).scaleY(1.01f).setDuration(80).withEndAction(() -> screen.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start();
    }

    private void pulse() {
        root.animate().alpha(0.92f).setDuration(70).withEndAction(() -> root.animate().alpha(1f).setDuration(110).start()).start();
    }

    @Override public void onBackPressed() { if(mode!=MODE_ROUTE) showMode(MODE_ROUTE); else super.onBackPressed(); }
    @Override protected void onDestroy() {
        if(tts!=null){try{tts.stop();tts.shutdown();}catch(Throwable ignored){}}
        if(recognizer!=null){try{recognizer.destroy();}catch(Throwable ignored){}}
        super.onDestroy();
    }
}
