package com.aprendegratisingles.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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
    private ImageView screen;
    private int mode = MODE_ROUTE;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private SpeechRecognizer recognizer;
    private SharedPreferences prefs;
    private boolean choseDistractor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Primero construimos y mostramos la pantalla. En algunos dispositivos MIUI,
        // manipular insets/barras antes de que exista el decorView puede cerrar la Activity.
        root = new FrameLayout(this);
        screen = new ImageView(this);
        screen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(screen, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.setOnTouchListener(this::handleTouch);
        setContentView(root);

        try {
            showMode(MODE_ROUTE);
        } catch (Throwable imageError) {
            Toast.makeText(this, "No se pudo cargar el diseño visual", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Funciones secundarias después de que la UI ya está visible.
        root.post(() -> {
            safeHideSystemBars();
            initTts();
        });
    }

    private void safeHideSystemBars() {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Throwable ignored) {}
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
                try {
                    if (status == TextToSpeech.SUCCESS && tts != null) {
                        tts.setLanguage(Locale.US);
                        tts.setSpeechRate(0.88f);
                        ttsReady = true;
                    }
                } catch (Throwable ignored) {}
            });
        } catch (Throwable ignored) {
            ttsReady = false;
        }
    }

    private void showMode(int newMode) {
        mode = newMode;
        choseDistractor = false;
        if (mode == MODE_ROUTE) screen.setImageResource(R.drawable.visual_route);
        else if (mode == MODE_EXERCISE) screen.setImageResource(R.drawable.visual_exercise);
        else if (mode == MODE_CORRECT) screen.setImageResource(R.drawable.visual_correct);
        else screen.setImageResource(R.drawable.visual_wrong);
        screen.setAlpha(0f);
        screen.animate().alpha(1f).setDuration(180).start();
    }

    private boolean handleTouch(View v, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float nx = event.getX() / Math.max(1f, v.getWidth());
        float ny = event.getY() / Math.max(1f, v.getHeight());

        if (mode == MODE_ROUTE) {
            // Primera lección: nodo y tarjeta
            if (ny > 0.42f && ny < 0.58f) {
                showMode(MODE_EXERCISE);
                return true;
            }
            // Segunda y tercera lección bloqueadas
            if (ny > 0.58f && ny < 0.81f) {
                Toast.makeText(this, "Completa Saludos en inglés para desbloquear esta lección", Toast.LENGTH_SHORT).show();
                pulse();
                return true;
            }
            // Examen
            if (ny > 0.80f && ny < 0.90f) {
                Toast.makeText(this, "Completa las 3 lecciones para desbloquear el examen", Toast.LENGTH_SHORT).show();
                pulse();
                return true;
            }
            // Navegación inferior
            if (ny > 0.90f) {
                if (nx < 0.25f) return true;
                finish();
                return true;
            }
        } else if (mode == MODE_EXERCISE) {
            // cerrar
            if (nx < 0.16f && ny < 0.14f) { showMode(MODE_ROUTE); return true; }
            // parlante
            if (nx > 0.44f && nx < 0.63f && ny > 0.28f && ny < 0.46f) {
                speak("Good morning, how are you?");
                bounce();
                return true;
            }
            // opciones distractoras inferiores
            if (ny > 0.58f && ny < 0.73f) {
                choseDistractor = true;
                pulse();
                return true;
            }
            // comprobar
            if (ny > 0.78f && ny < 0.88f) {
                if (choseDistractor) {
                    showMode(MODE_WRONG);
                } else {
                    awardXp(10);
                    showMode(MODE_CORRECT);
                }
                return true;
            }
            // micrófono
            if (ny > 0.88f) {
                startListening();
                return true;
            }
        } else if (mode == MODE_CORRECT) {
            // tocar traducción reproduce audio
            if (ny > 0.56f && ny < 0.76f) {
                speak("Good morning, how are you?");
                return true;
            }
            // siguiente
            if (ny > 0.82f) {
                prefs.edit().putBoolean("u1_done_0", true).apply();
                awardXp(25);
                showMode(MODE_ROUTE);
                Toast.makeText(this, "¡Lección completada! +25 XP", Toast.LENGTH_SHORT).show();
                return true;
            }
        } else if (mode == MODE_WRONG) {
            if (ny > 0.56f && ny < 0.76f) {
                speak("Good morning, how are you?");
                return true;
            }
            if (ny > 0.82f) {
                showMode(MODE_EXERCISE);
                return true;
            }
        }
        return true;
    }

    private void speak(String phrase) {
        try {
            if (ttsReady && tts != null)
                tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "visual_" + System.currentTimeMillis());
        } catch (Throwable ignored) {
            Toast.makeText(this, "Audio no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "El reconocimiento de voz no está disponible en este dispositivo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 701);
            return;
        }
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
                if (similar(heard, "Good morning, how are you")) {
                    awardXp(10);
                    showMode(MODE_CORRECT);
                } else {
                    showMode(MODE_WRONG);
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        recognizer.startListening(intent);
    }

    private boolean similar(String heard, String expected) {
        String a = normalize(heard);
        String b = normalize(expected);
        if (a.equals(b)) return true;
        int hit = 0;
        String[] words = b.split(" ");
        for (String w : words) if (a.contains(w)) hit++;
        return words.length > 0 && hit / (float) words.length >= 0.65f;
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private void awardXp(int amount) {
        int xp = prefs.getInt("xp", 0) + amount;
        int today = prefs.getInt("today_xp", 0) + amount;
        prefs.edit().putInt("xp", xp).putInt("today_xp", today).apply();
    }

    private void bounce() {
        screen.animate().scaleX(1.01f).scaleY(1.01f).setDuration(90).withEndAction(() -> screen.animate().scaleX(1f).scaleY(1f).setDuration(110).start()).start();
    }

    private void pulse() {
        root.animate().alpha(0.92f).setDuration(80).withEndAction(() -> root.animate().alpha(1f).setDuration(120).start()).start();
    }

    @Override public void onBackPressed() {
        if (mode != MODE_ROUTE) showMode(MODE_ROUTE); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (recognizer != null) recognizer.destroy();
        super.onDestroy();
    }
}
