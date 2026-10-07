package com.aprendegratisingles.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String PREFS = "agi_native_v660";
    private static final int BLUE = Color.rgb(13, 120, 217);
    private static final int BLUE_DARK = Color.rgb(6, 64, 140);
    private static final int LIME = Color.rgb(99, 229, 50);
    private static final int GREEN = Color.rgb(33, 197, 107);
    private static final int RED = Color.rgb(239, 83, 96);
    private static final int CARD = Color.rgb(250, 253, 255);
    private static final int TEXT = Color.rgb(7, 63, 141);
    private static final int MUTED = Color.rgb(86, 106, 129);
    private static final int BORDER = Color.rgb(203, 224, 244);

    private static final int TYPE_WORD_BANK = 0;
    private static final int TYPE_CHOICE = 1;
    private static final int TYPE_LISTEN = 2;
    private static final int TYPE_SPEAK = 3;
    private static final String NOTIFICATION_CHANNEL = "daily_english";
    private static final int REQ_NOTIFICATIONS = 702;

    private SharedPreferences prefs;
    private FrameLayout root;
    private TextToSpeech tts;
    private boolean ttsReady;
    private SpeechRecognizer recognizer;

    private int currentLesson = -1;
    private int currentQuestion = 0;
    private Exercise currentExercise;
    private final ArrayList<String> selectedWords = new ArrayList<>();
    private String selectedChoice = "";
    private FlowLayout answerFlow;
    private FlowLayout bankFlow;
    private ProgressBar exerciseProgress;
    private TextView heartsText;
    private TextView checkButton;
    private TextView micButton;

    private final Lesson[] lessons = new Lesson[]{
            new Lesson("Primeros pasos", "Saludos básicos y frases para comenzar", new Exercise[]{
                    Exercise.wordBank("Traduce esta oración", "Good morning, how are you?", "Buenos días ¿cómo estás?", "Buenos", "días", "¿cómo", "estás?", "noche", "gracias"),
                    Exercise.choice("¿Qué significa “Hello”?", "Hello", "Hola", "Hola", "Gracias", "Adiós", "Por favor"),
                    Exercise.listen("Escucha y elige la traducción", "Good night", "Buenas noches", "Buenas noches", "Buenos días", "Hasta luego", "Gracias"),
                    Exercise.speak("Repite la frase", "Nice to meet you", "Mucho gusto")
            }),
            new Lesson("Preséntate", "Tu nombre y presentaciones sencillas", new Exercise[]{
                    Exercise.wordBank("Traduce esta oración", "My name is Ana", "Mi nombre es Ana", "Mi", "nombre", "es", "Ana", "Hola", "gracias"),
                    Exercise.choice("¿Qué significa esta pregunta?", "What is your name?", "¿Cómo te llamas?", "¿Cómo te llamas?", "¿De dónde eres?", "¿Cómo estás?", "¿Qué hora es?"),
                    Exercise.listen("Escucha y elige", "I am Daniel", "Soy Daniel", "Soy Daniel", "Me llamo Ana", "Estoy bien", "Adiós"),
                    Exercise.speak("Repite la frase", "My name is Sofia", "Mi nombre es Sofia")
            }),
            new Lesson("Di de dónde eres", "Países europeos y la pregunta Where are you from?", new Exercise[]{
                    Exercise.wordBank("Traduce esta oración", "I am from Poland", "Soy de Polonia", "Soy", "de", "Polonia", "Francia", "España"),
                    Exercise.choice("¿Qué significa esta pregunta?", "Where are you from?", "¿De dónde eres?", "¿De dónde eres?", "¿Cómo te llamas?", "¿Dónde trabajas?", "¿Qué hora es?"),
                    Exercise.choice("Elige la traducción correcta", "France", "Francia", "Francia", "Alemania", "Italia", "Inglaterra"),
                    Exercise.speak("Repite la frase", "I am from Germany", "Soy de Alemania")
            }),
            new Lesson("Números del 1 al 10", "Reconoce, escucha y pronuncia los números básicos", new Exercise[]{
                    Exercise.choice("¿Qué número es “Seven”?", "Seven", "7", "7", "5", "8", "10"),
                    Exercise.choice("¿Qué número es “Ten”?", "Ten", "10", "10", "2", "6", "9"),
                    Exercise.listen("Escucha y elige el número", "Four", "4", "4", "3", "5", "8"),
                    Exercise.speak("Repite los números", "One, two, three, four, five", "Uno, dos, tres, cuatro, cinco")
            }),
            new Lesson("Colores básicos", "Los colores más usados en inglés", new Exercise[]{
                    Exercise.choice("¿Qué significa “Blue”?", "Blue", "Azul", "Azul", "Rojo", "Verde", "Negro"),
                    Exercise.wordBank("Traduce esta frase", "A red car", "Un carro rojo", "Un", "carro", "rojo", "azul", "casa"),
                    Exercise.listen("Escucha y elige", "Yellow", "Amarillo", "Amarillo", "Morado", "Blanco", "Marrón"),
                    Exercise.speak("Repite la frase", "My favorite color is green", "Mi color favorito es verde")
            }),
            new Lesson("Mi familia", "Vocabulario para hablar de tu familia", new Exercise[]{
                    Exercise.choice("¿Qué significa “Mother”?", "Mother", "Madre", "Madre", "Hermana", "Hija", "Tía"),
                    Exercise.wordBank("Traduce esta oración", "He is my brother", "Él es mi hermano", "Él", "es", "mi", "hermano", "padre", "amigo"),
                    Exercise.listen("Escucha y elige", "Sister", "Hermana", "Hermana", "Madre", "Hija", "Prima"),
                    Exercise.speak("Repite la frase", "This is my family", "Esta es mi familia")
            }),
            new Lesson("La casa", "Habitaciones y objetos cotidianos", new Exercise[]{
                    Exercise.choice("¿Qué significa “Kitchen”?", "Kitchen", "Cocina", "Cocina", "Baño", "Dormitorio", "Puerta"),
                    Exercise.wordBank("Traduce esta oración", "This is my house", "Esta es mi casa", "Esta", "es", "mi", "casa", "mesa", "puerta"),
                    Exercise.listen("Escucha y elige", "Bathroom", "Baño", "Baño", "Cocina", "Ventana", "Mesa"),
                    Exercise.speak("Repite la pregunta", "Where is the bathroom?", "¿Dónde está el baño?")
            }),
            new Lesson("Días y meses", "Días de la semana y meses básicos", new Exercise[]{
                    Exercise.choice("¿Qué día viene después de Monday?", "Tuesday", "Tuesday", "Tuesday", "Friday", "Sunday", "Wednesday"),
                    Exercise.listen("Escucha y elige", "Friday", "Viernes", "Viernes", "Lunes", "Martes", "Domingo"),
                    Exercise.choice("¿Qué significa “January”?", "January", "Enero", "Enero", "Junio", "Julio", "Marzo"),
                    Exercise.speak("Repite la pregunta", "What day is it today?", "¿Qué día es hoy?")
            }),
            new Lesson("Preguntas básicas", "What, Where, Who, When y How", new Exercise[]{
                    Exercise.choice("¿Qué palabra significa “Dónde”?", "Where", "Where", "Where", "When", "Who", "What"),
                    Exercise.wordBank("Ordena la pregunta", "How are you?", "¿Cómo estás?", "¿Cómo", "estás?", "¿Dónde", "eres?", "Hola"),
                    Exercise.listen("Escucha y elige", "Who is he?", "¿Quién es él?", "¿Quién es él?", "¿Dónde está?", "¿Cómo estás?", "¿Qué es eso?"),
                    Exercise.speak("Repite la pregunta", "What is your name?", "¿Cómo te llamas?")
            }),
            new Lesson("Repaso y examen", "Repasa todo lo aprendido en las primeras lecciones", new Exercise[]{
                    Exercise.choice("¿Qué significa “Good morning”?", "Good morning", "Buenos días", "Buenos días", "Buenas noches", "Gracias", "Adiós"),
                    Exercise.wordBank("Traduce esta oración", "I am from Italy", "Soy de Italia", "Soy", "de", "Italia", "Polonia", "Francia"),
                    Exercise.listen("Escucha y elige", "Nine", "9", "9", "6", "7", "10"),
                    Exercise.choice("¿Qué significa “Kitchen”?", "Kitchen", "Cocina", "Cocina", "Baño", "Familia", "Azul"),
                    Exercise.choice("¿Qué significa “Where”?", "Where", "Dónde", "Dónde", "Cuándo", "Quién", "Cómo"),
                    Exercise.speak("Repite la frase", "Nice to meet you", "Mucho gusto")
            })
    };

    private final String[] lessonIcons = {"👋", "🙋", "🌍", "🔢", "🎨", "👨‍👩‍👧", "🏠", "📅", "❓", "🏆"};
    private WebView lessonsWebView;
    private boolean showingLessonsWeb = false;
    private boolean notificationPermissionFromOnboarding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        ensureDefaults();
        createNotificationChannel();
        if (prefs.getBoolean("notifications_enabled", false)) scheduleDailyReminder();
        initTts();
        showSplash();
    }

    private void ensureDefaults() {
        SharedPreferences.Editor e = prefs.edit();
        if (!prefs.contains("hearts")) e.putInt("hearts", 5);
        if (!prefs.contains("daily_minutes")) e.putInt("daily_minutes", 10);
        e.apply();
    }

    private void showSplash() {
        setRootWithArt(R.drawable.london_route_art);
        View shade = new View(this);
        shade.setBackgroundColor(Color.argb(80, 0, 73, 150));
        root.addView(shade, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout box = cardColumn();
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(26), dp(24), dp(26), dp(24));
        TextView logo = label("🇬🇧", 54, BLUE_DARK, true);
        logo.setGravity(Gravity.CENTER);
        box.addView(logo);
        TextView title = label("Aprende Gratis Inglés", 29, BLUE_DARK, true);
        title.setGravity(Gravity.CENTER);
        box.addView(title);
        TextView sub = label("Tu aventura para hablar inglés empieza aquí", 15, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(8), 0, dp(18));
        box.addView(sub);

        ProgressBar loading = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loading.setIndeterminate(false);
        loading.setMax(100);
        loading.setProgress(4);
        loading.getProgressDrawable().setTint(LIME);
        box.addView(loading, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(18)));
        TextView loadingText = label("Cargando tu ruta…", 13, BLUE, true);
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setPadding(0, dp(10), 0, 0);
        box.addView(loadingText);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        lp.leftMargin = dp(24);
        lp.rightMargin = dp(24);
        root.addView(box, lp);

        final int[] progress = {4};
        Runnable ticker = new Runnable() {
            @Override public void run() {
                progress[0] = Math.min(100, progress[0] + 8);
                loading.setProgress(progress[0]);
                if (progress[0] < 100) loading.postDelayed(this, 100);
            }
        };
        loading.post(ticker);
        root.postDelayed(() -> {
            if (prefs.getBoolean("onboarded_v7", false)) showRoute();
            else showOnboardingUsername();
        }, 1600);
    }

    private LinearLayout onboardingPage(String step, String title, String subtitle) {
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(18), dp(18), dp(18), dp(18));
        root.addView(page, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout card = cardColumn();
        card.setPadding(dp(24), dp(22), dp(24), dp(24));
        card.addView(label(step, 13, BLUE, true));
        TextView t = label(title, 29, BLUE_DARK, true);
        t.setPadding(0, dp(5), 0, 0);
        card.addView(t);
        TextView st = label(subtitle, 16, MUTED, false);
        st.setPadding(0, dp(8), 0, dp(18));
        card.addView(st);
        page.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return card;
    }

    private void showOnboardingUsername() {
        LinearLayout card = onboardingPage("PASO 1 DE 5", "Crea tu nombre de usuario", "Este será tu nombre dentro de la app y en las ligas cuando conectemos el ranking online.");
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(20);
        input.setTextColor(BLUE_DARK);
        input.setHint("@Usuario");
        input.setHintTextColor(Color.rgb(140, 160, 178));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(dp(16), dp(14), dp(16), dp(14));
        input.setBackground(roundRect(Color.WHITE, 16, BORDER, 2));
        card.addView(input, matchWrapMargin(0, 0, 0, 14));
        TextView next = actionButton("CONTINUAR", LIME, BLUE_DARK);
        next.setOnClickListener(v -> {
            String raw = input.getText().toString().trim().replaceAll("[^A-Za-z0-9_]", "");
            if (raw.length() < 3) {
                Toast.makeText(this, "Escribe un usuario de al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString("username", "@" + raw).apply();
            showOnboardingLevel();
        });
        card.addView(next);
    }

    private void showOnboardingLevel() {
        LinearLayout card = onboardingPage("PASO 2 DE 5", "¿Cuál es tu nivel de inglés?", "Adaptaremos la experiencia inicial a tu nivel actual.");
        String[] options = {"🌱 Principiante", "📘 Básico", "🚀 Intermedio", "🏅 Avanzado"};
        for (String option : options) {
            TextView b = actionButton(option, Color.WHITE, BLUE_DARK);
            b.setBackground(roundRect(Color.WHITE, 16, BLUE, 2));
            b.setOnClickListener(v -> {
                prefs.edit().putString("english_level", option.substring(option.indexOf(' ') + 1)).apply();
                showOnboardingGoal();
            });
            card.addView(b, matchWrapMargin(0, 0, 0, 10));
        }
    }

    private void showOnboardingGoal() {
        LinearLayout card = onboardingPage("PASO 3 DE 5", "¿Para qué quieres aprender inglés?", "Elegiremos ejercicios y misiones que encajen mejor con tu objetivo.");
        String[] options = {"🗣 Hablar", "✈️ Viajar", "💼 Trabajar", "🎓 Estudiar", "📝 Preparar un examen", "🌍 Vivir en otro país"};
        for (String option : options) {
            TextView b = actionButton(option, Color.WHITE, BLUE_DARK);
            b.setBackground(roundRect(Color.WHITE, 16, BLUE, 2));
            b.setOnClickListener(v -> {
                prefs.edit().putString("goal", option.substring(option.indexOf(' ') + 1)).apply();
                showOnboardingDailyGoal();
            });
            card.addView(b, matchWrapMargin(0, 0, 0, 9));
        }
    }

    private void showOnboardingDailyGoal() {
        LinearLayout card = onboardingPage("PASO 4 DE 5", "Elige tu meta diaria", "Una meta pequeña y constante suele ganar a una semana heroica seguida de tres meses de abandono.");
        int[] minutes = {5, 10, 15, 20};
        for (int minute : minutes) {
            TextView b = actionButton(minute + " MINUTOS AL DÍA", minute == 10 ? LIME : Color.WHITE, BLUE_DARK);
            if (minute != 10) b.setBackground(roundRect(Color.WHITE, 16, BLUE, 2));
            b.setOnClickListener(v -> {
                prefs.edit().putInt("daily_minutes", minute).apply();
                showOnboardingNotifications();
            });
            card.addView(b, matchWrapMargin(0, 0, 0, 10));
        }
    }

    private void showOnboardingNotifications() {
        LinearLayout card = onboardingPage("PASO 5 DE 5", "Activa tus recordatorios", "Podemos avisarte una vez al día para que no pierdas tu racha. Tú decides si permites las notificaciones.");
        card.addView(label("🔔  Recordatorio diario de estudio\n🔥  Aviso para proteger tu racha\n🏆  Futuras alertas de liga y logros", 16, BLUE_DARK, true), matchWrapMargin(0, 0, 0, 18));
        TextView allow = actionButton("PERMITIR NOTIFICACIONES", LIME, BLUE_DARK);
        allow.setOnClickListener(v -> requestNotificationPermission(true));
        card.addView(allow, matchWrapMargin(0, 0, 0, 10));
        TextView skip = actionButton("AHORA NO", Color.WHITE, BLUE);
        skip.setBackground(roundRect(Color.WHITE, 16, BLUE, 2));
        skip.setOnClickListener(v -> finishOnboarding(false));
        card.addView(skip);
    }

    private void finishOnboarding(boolean notificationsEnabled) {
        prefs.edit().putBoolean("onboarded_v7", true).putBoolean("notifications_enabled", notificationsEnabled).apply();
        if (notificationsEnabled) scheduleDailyReminder();
        showRoute();
    }

    private void initTts() {
        try {
            tts = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.UK);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setLanguage(Locale.US);
                    }
                    tts.setSpeechRate(0.90f);
                    ttsReady = true;
                }
            });
        } catch (Throwable ignored) {
            ttsReady = false;
        }
    }

    private void speak(String text) {
        if (!ttsReady || tts == null) {
            Toast.makeText(this, "El audio todavía se está preparando", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "agi_" + System.currentTimeMillis());
        } catch (Throwable ignored) {
            Toast.makeText(this, "No pude reproducir el audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRootWithArt(int drawable) {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(232, 246, 255));

        ImageView art = new ImageView(this);
        art.setImageResource(drawable);
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        art.setAlpha(0.96f);
        root.addView(art, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View veil = new View(this);
        veil.setBackgroundColor(Color.argb(16, 255, 255, 255));
        root.addView(veil, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    private void showRoute() {
        currentLesson = -1;
        showingLessonsWeb = false;
        lessonsWebView = null;
        setRootWithArt(R.drawable.london_route_art);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(10), dp(14), dp(0));
        root.addView(page, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        page.addView(buildHud());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        sp.topMargin = dp(8);
        page.addView(scroll, sp);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(6), dp(8), dp(6), dp(28));
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout intro = cardColumn();
        intro.setPadding(dp(22), dp(18), dp(22), dp(18));
        intro.addView(label("🇬🇧  TU RUTA EN LONDRES", 14, BLUE, true));
        TextView routeTitle = label("Aprende inglés paso a paso", 29, BLUE_DARK, true);
        routeTitle.setPadding(0, dp(6), 0, 0);
        intro.addView(routeTitle);
        TextView routeSub = label("Completa cada lección para desbloquear la siguiente. Hay 10 lecciones en esta primera ruta.", 17, MUTED, false);
        routeSub.setPadding(0, dp(6), 0, 0);
        intro.addView(routeSub);
        body.addView(intro, matchWrapMargin(0, 0, 0, 18));

        for (int i = 0; i < lessons.length; i++) {
            boolean unlocked = i == 0 || prefs.getBoolean("lesson_" + (i - 1), false);
            body.addView(lessonRow(i, lessonIcons[i], unlocked));
            if (i < lessons.length - 1) body.addView(connector());
        }

        page.addView(bottomNav(0));
    }

    private LinearLayout buildHud() {
        LinearLayout hud = new LinearLayout(this);
        hud.setOrientation(LinearLayout.HORIZONTAL);
        hud.setGravity(Gravity.CENTER_VERTICAL);
        hud.setPadding(dp(14), dp(10), dp(14), dp(10));
        hud.setBackground(roundRect(Color.argb(236, 7, 63, 141), 24, Color.argb(90, 255, 255, 255), 1));
        hud.setElevation(dp(4));

        int xp = prefs.getInt("xp", 0);
        String username = prefs.getString("username", "@Usuario");
        TextView level = label("🥉  Nivel " + levelForXp(xp) + " · Bronce\n" + username + " · " + xp + " XP", 14, Color.WHITE, true);
        hud.addView(level, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView streak = pill("🔥 " + prefs.getInt("streak", 0), Color.argb(225, 31, 88, 157), Color.WHITE);
        hud.addView(streak, wrapMargin(5, 0, 0, 0));
        TextView hearts = pill("❤️ " + prefs.getInt("hearts", 5), Color.argb(225, 31, 88, 157), Color.WHITE);
        hud.addView(hearts, wrapMargin(6, 0, 0, 0));
        return hud;
    }

    private int levelForXp(int xp) {
        return 1 + xp / 150;
    }

    private View lessonRow(int index, String icon, boolean unlocked) {
        Lesson lesson = lessons[index];
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView node = label(unlocked ? (index == lessons.length - 1 ? "🏆" : "▶") : "🔒", index == lessons.length - 1 ? 31 : 28, unlocked ? Color.WHITE : Color.rgb(114, 132, 148), true);
        node.setGravity(Gravity.CENTER);
        int nodeColor = unlocked ? (index == lessons.length - 1 ? Color.rgb(255, 187, 0) : GREEN) : Color.rgb(218, 227, 236);
        node.setBackground(roundRect(nodeColor, 100, Color.WHITE, 5));
        node.setElevation(dp(8));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(dp(82), dp(82));
        np.rightMargin = dp(12);
        row.addView(node, np);

        LinearLayout card = cardColumn();
        card.setPadding(dp(18), dp(14), dp(18), dp(14));
        card.addView(label("LECCIÓN " + (index + 1), 12, BLUE, true));
        TextView lessonTitle = label(icon + "  " + lesson.title, 20, BLUE_DARK, true);
        lessonTitle.setPadding(0, dp(3), 0, 0);
        card.addView(lessonTitle);
        TextView desc = label(lesson.subtitle, 14, MUTED, false);
        desc.setPadding(0, dp(5), 0, 0);
        card.addView(desc);
        TextView state = label(unlocked ? (prefs.getBoolean("lesson_" + index, false) ? "REPETIR LECCIÓN" : "JUGAR · SIGUIENTE") : "BLOQUEADA", 12, unlocked ? GREEN : MUTED, true);
        state.setPadding(0, dp(6), 0, 0);
        card.addView(state);
        card.setAlpha(unlocked ? 1f : 0.94f);
        row.addView(card, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View.OnClickListener click = v -> {
            if (!unlocked) {
                Toast.makeText(this, "Completa la lección anterior para desbloquear esta", Toast.LENGTH_SHORT).show();
                pulse(card);
                return;
            }
            startLesson(index);
        };
        node.setOnClickListener(click);
        card.setOnClickListener(click);
        row.setOnClickListener(click);
        return row;
    }

    private View connector() {
        View line = new View(this);
        line.setBackgroundColor(Color.argb(180, 75, 177, 232));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(5), dp(34));
        lp.leftMargin = dp(40);
        lp.topMargin = dp(3);
        lp.bottomMargin = dp(3);
        line.setLayoutParams(lp);
        return line;
    }

    private LinearLayout bottomNav(int selected) {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(4), dp(7), dp(4), dp(7));
        nav.setBackground(roundRect(Color.argb(248, 255, 255, 255), 22, BORDER, 1));
        nav.setElevation(dp(12));

        String[] icons = {"🗺️", "🧠", "🥇", "🏆", "👤"};
        String[] labels = {"Ruta", "Practicar", "Liga", "Logros", "Perfil"};
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            TextView item = label(icons[i] + "\n" + labels[i], 13, i == selected ? BLUE : BLUE_DARK, true);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(6), dp(6), dp(6), dp(6));
            if (i == selected) item.setBackground(roundRect(Color.rgb(232, 246, 255), 16, Color.TRANSPARENT, 0));
            item.setOnClickListener(v -> {
                if (idx == 0) showRoute();
                else if (idx == 1) showPractice();
                else if (idx == 2) showLeague();
                else if (idx == 3) showAchievements();
                else showProfile();
            });
            nav.addView(item, new LinearLayout.LayoutParams(0, dp(58), 1f));
        }
        return nav;
    }

    private void showPractice() {
        currentLesson = -1;
        showingLessonsWeb = false;
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        page.addView(buildHud());
        LinearLayout card = cardColumn();
        card.setPadding(dp(24), dp(24), dp(24), dp(24));
        card.addView(label("🧠 Practicar", 30, BLUE_DARK, true));
        TextView copy = label("Refuerza lo que ya desbloqueaste. La práctica usa ejercicios reales de tus lecciones completadas.", 17, MUTED, false);
        copy.setPadding(0, dp(10), 0, dp(18));
        card.addView(copy);

        TextView btn = actionButton("PRACTICAR AHORA", LIME, BLUE_DARK);
        btn.setOnClickListener(v -> startLesson(firstUnlockedPracticeLesson()));
        card.addView(btn);

        TextView all = actionButton("VER TODAS LAS LECCIONES", BLUE, Color.WHITE);
        all.setBackground(roundRect(BLUE, 18, BLUE_DARK, 1));
        all.setOnClickListener(v -> showAllLessonsWeb());
        card.addView(all, matchWrapMargin(0, 12, 0, 0));

        TextView hint = label("Abre aprendegratisingles.com dentro de la app", 13, MUTED, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(8), 0, 0);
        card.addView(hint);

        page.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(1));
        root.addView(page);
    }

    private int firstUnlockedPracticeLesson() {
        int lastCompleted = -1;
        for (int i = 0; i < lessons.length; i++) {
            if (prefs.getBoolean("lesson_" + i, false)) lastCompleted = i;
            else break;
        }
        return Math.max(0, lastCompleted);
    }

    private void showAllLessonsWeb() {
        currentLesson = -1;
        showingLessonsWeb = true;
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        root.addView(page, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(12), dp(10), dp(12), dp(10));
        top.setBackgroundColor(BLUE_DARK);
        TextView back = pill("‹", BLUE, Color.WHITE);
        back.setTextSize(26);
        back.setOnClickListener(v -> showPractice());
        top.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = label("Todas las lecciones", 20, Color.WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.leftMargin = dp(12);
        top.addView(title, tp);
        page.addView(top);

        lessonsWebView = new WebView(this);
        WebSettings settings = lessonsWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        lessonsWebView.setWebViewClient(new WebViewClient());
        lessonsWebView.loadUrl("https://www.aprendegratisingles.com/p/lecciones.html");
        page.addView(lessonsWebView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private void showLeague() {
        currentLesson = -1;
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        page.addView(buildHud());

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(6), dp(12), dp(6), dp(18));
        scroll.addView(body);

        int xp = prefs.getInt("xp", 0);
        String league = leagueForXp(xp);
        int next = nextLeagueXp(xp);
        LinearLayout hero = cardColumn();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(22), dp(22), dp(22), dp(22));
        hero.addView(label("🥇", 52, BLUE_DARK, true));
        TextView title = label("Liga " + league, 29, BLUE_DARK, true);
        title.setGravity(Gravity.CENTER);
        hero.addView(title);
        TextView user = label(prefs.getString("username", "@Usuario") + " · " + xp + " XP", 17, BLUE, true);
        user.setGravity(Gravity.CENTER);
        user.setPadding(0, dp(6), 0, dp(10));
        hero.addView(user);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(Math.max(1, next));
        progress.setProgress(Math.min(xp, next));
        progress.getProgressDrawable().setTint(LIME);
        hero.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(18)));
        TextView toNext = label(next > xp ? (next - xp) + " XP para la siguiente liga" : "Liga máxima alcanzada", 13, MUTED, true);
        toNext.setGravity(Gravity.CENTER);
        toNext.setPadding(0, dp(8), 0, 0);
        hero.addView(toNext);
        body.addView(hero, matchWrapMargin(0, 0, 0, 12));

        body.addView(statCard("🏅 Puntuación competitiva", competitiveScore() + " puntos"));
        body.addView(statCard("✅ Lecciones completadas", completedCount() + "/" + lessons.length));
        body.addView(statCard("🔥 Racha actual", prefs.getInt("streak", 0) + " días"));
        LinearLayout online = cardColumn();
        online.setPadding(dp(20), dp(18), dp(20), dp(18));
        online.addView(label("🌐 Ranking entre usuarios", 21, BLUE_DARK, true));
        TextView explanation = label("Tu perfil y puntuación ya están preparados. Para comparar posiciones con personas de otros teléfonos necesitamos conectar un backend seguro. No mostramos rivales inventados como si fueran reales.", 15, MUTED, false);
        explanation.setPadding(0, dp(7), 0, 0);
        online.addView(explanation);
        body.addView(online, matchWrapMargin(0, 0, 0, 12));

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(2));
        root.addView(page);
    }

    private int competitiveScore() {
        return prefs.getInt("xp", 0) + completedCount() * 100 + prefs.getInt("streak", 0) * 25;
    }

    private String leagueForXp(int xp) {
        if (xp < 500) return "Bronce";
        if (xp < 1200) return "Plata";
        if (xp < 2500) return "Oro";
        if (xp < 5000) return "Zafiro";
        return "Diamante";
    }

    private int nextLeagueXp(int xp) {
        if (xp < 500) return 500;
        if (xp < 1200) return 1200;
        if (xp < 2500) return 2500;
        if (xp < 5000) return 5000;
        return Math.max(5000, xp);
    }

    private void showAchievements() {
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        page.addView(buildHud());
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(6), dp(12), dp(6), dp(18));
        scroll.addView(body);
        body.addView(statCard("🏆 Primer paso", prefs.getBoolean("lesson_0", false) ? "COMPLETADO" : "Completa Primeros pasos"));
        body.addView(statCard("🔥 Constancia", prefs.getInt("streak", 0) + " días de racha"));
        body.addView(statCard("⭐ Experiencia", prefs.getInt("xp", 0) + " XP acumulados"));
        body.addView(statCard("🇬🇧 Ruta inicial", completedCount() + "/" + lessons.length + " lecciones completadas"));
        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(3));
        root.addView(page);
    }

    private View statCard(String title, String sub) {
        LinearLayout card = cardColumn();
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        card.addView(label(title, 21, BLUE_DARK, true));
        TextView s = label(sub, 15, MUTED, false);
        s.setPadding(0, dp(5), 0, 0);
        card.addView(s);
        return withMargin(card, 0, 0, 0, 12);
    }

    private int completedCount() {
        int c = 0;
        for (int i = 0; i < lessons.length; i++) if (prefs.getBoolean("lesson_" + i, false)) c++;
        return c;
    }

    private void showProfile() {
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        page.addView(buildHud());
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(4), dp(12), dp(4), dp(18));
        scroll.addView(body);

        LinearLayout profile = cardColumn();
        profile.setGravity(Gravity.CENTER_HORIZONTAL);
        profile.setPadding(dp(24), dp(24), dp(24), dp(24));
        profile.addView(label("👤", 52, BLUE_DARK, true));
        TextView name = label(prefs.getString("username", "@Usuario"), 27, BLUE_DARK, true);
        name.setGravity(Gravity.CENTER);
        profile.addView(name);
        String levelName = prefs.getString("english_level", "Principiante");
        String goal = prefs.getString("goal", "Hablar");
        int daily = prefs.getInt("daily_minutes", 10);
        TextView personal = label(levelName + " · Objetivo: " + goal + "\nMeta diaria: " + daily + " min", 15, MUTED, false);
        personal.setGravity(Gravity.CENTER);
        personal.setPadding(0, dp(8), 0, dp(12));
        profile.addView(personal);
        TextView stats = label("Nivel " + levelForXp(prefs.getInt("xp", 0)) + " · " + prefs.getInt("xp", 0) + " XP\n" + completedCount() + " lecciones · " + leagueForXp(prefs.getInt("xp", 0)), 17, BLUE, true);
        stats.setGravity(Gravity.CENTER);
        profile.addView(stats);
        body.addView(profile, matchWrapMargin(0, 0, 0, 12));

        LinearLayout notificationCard = cardColumn();
        notificationCard.setPadding(dp(20), dp(18), dp(20), dp(18));
        notificationCard.addView(label("🔔 Notificaciones", 21, BLUE_DARK, true));
        boolean enabled = prefs.getBoolean("notifications_enabled", false);
        TextView ns = label(enabled ? "Recordatorio diario activado aproximadamente a las 19:00." : "Las notificaciones están desactivadas.", 14, MUTED, false);
        ns.setPadding(0, dp(6), 0, dp(12));
        notificationCard.addView(ns);
        TextView notify = actionButton(enabled ? "DESACTIVAR NOTIFICACIONES" : "PERMITIR NOTIFICACIONES", enabled ? Color.WHITE : LIME, enabled ? RED : BLUE_DARK);
        if (enabled) notify.setBackground(roundRect(Color.WHITE, 16, RED, 2));
        notify.setOnClickListener(v -> {
            if (prefs.getBoolean("notifications_enabled", false)) {
                cancelDailyReminder();
                prefs.edit().putBoolean("notifications_enabled", false).apply();
                showProfile();
            } else {
                requestNotificationPermission(false);
            }
        });
        notificationCard.addView(notify);
        body.addView(notificationCard, matchWrapMargin(0, 0, 0, 12));

        TextView refill = actionButton("RECARGAR CORAZONES", Color.WHITE, BLUE);
        refill.setBackground(roundRect(Color.WHITE, 18, BLUE, 2));
        refill.setOnClickListener(v -> {
            prefs.edit().putInt("hearts", 5).apply();
            Toast.makeText(this, "Corazones restaurados", Toast.LENGTH_SHORT).show();
            showProfile();
        });
        body.addView(refill);

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(4));
        root.addView(page);
    }

    private LinearLayout pageColumn() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(10), dp(14), dp(0));
        page.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return page;
    }

    private void startLesson(int lessonIndex) {
        if (lessonIndex < 0 || lessonIndex >= lessons.length) return;
        currentLesson = lessonIndex;
        currentQuestion = 0;
        showExercise();
    }

    private void showExercise() {
        Lesson lesson = lessons[currentLesson];
        if (currentQuestion >= lesson.exercises.length) {
            completeLesson();
            return;
        }
        currentExercise = lesson.exercises[currentQuestion];
        selectedChoice = "";
        selectedWords.clear();

        setRootWithArt(R.drawable.london_exercise_art);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(8), dp(14), dp(12));
        root.addView(page, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView close = pill("✕", BLUE, Color.WHITE);
        close.setGravity(Gravity.CENTER);
        close.setOnClickListener(v -> showRoute());
        top.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));

        exerciseProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        exerciseProgress.setMax(lesson.exercises.length);
        exerciseProgress.setProgress(currentQuestion + 1);
        exerciseProgress.getProgressDrawable().setTint(LIME);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, dp(20), 1f);
        pp.leftMargin = dp(10);
        pp.rightMargin = dp(10);
        top.addView(exerciseProgress, pp);

        top.addView(pill("🔥 " + prefs.getInt("streak", 0), BLUE_DARK, Color.WHITE));
        heartsText = pill("❤️ " + prefs.getInt("hearts", 5), BLUE_DARK, Color.WHITE);
        top.addView(heartsText, wrapMargin(6, 0, 0, 0));
        page.addView(top);

        LinearLayout titleCard = cardColumn();
        titleCard.setPadding(dp(18), dp(14), dp(18), dp(14));
        titleCard.addView(label("Lección " + (currentLesson + 1) + " · " + lesson.title, 17, BLUE, true));
        TextView prompt = label(currentExercise.prompt, 28, BLUE_DARK, true);
        prompt.setPadding(0, dp(5), 0, 0);
        titleCard.addView(prompt);
        page.addView(titleCard, matchWrapMargin(0, 10, 0, 10));

        ScrollView workScroll = new ScrollView(this);
        workScroll.setFillViewport(true);
        LinearLayout work = new LinearLayout(this);
        work.setOrientation(LinearLayout.VERTICAL);
        work.setPadding(dp(2), dp(4), dp(2), dp(8));
        workScroll.addView(work);
        page.addView(workScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView speech = label("🔊   " + currentExercise.english, 22, BLUE_DARK, true);
        speech.setGravity(Gravity.CENTER_VERTICAL);
        speech.setPadding(dp(18), dp(18), dp(18), dp(18));
        speech.setBackground(roundRect(Color.argb(248, 255, 255, 255), 22, Color.rgb(178, 219, 249), 2));
        speech.setElevation(dp(5));
        speech.setOnClickListener(v -> speak(currentExercise.english));
        work.addView(speech, matchWrapMargin(0, 2, 0, 14));

        if (currentExercise.type == TYPE_WORD_BANK) buildWordBank(work);
        else if (currentExercise.type == TYPE_CHOICE || currentExercise.type == TYPE_LISTEN) buildChoices(work);
        else buildSpeechPractice(work);

        checkButton = actionButton(currentExercise.type == TYPE_SPEAK ? "COMPROBAR PRONUNCIACIÓN" : "COMPROBAR", LIME, BLUE_DARK);
        checkButton.setOnClickListener(v -> checkCurrentAnswer());
        page.addView(checkButton, matchWrapMargin(0, 8, 0, 6));

        micButton = actionButton("🎙  USAR MICRÓFONO", Color.WHITE, BLUE);
        micButton.setBackground(roundRect(Color.WHITE, 18, BLUE, 2));
        micButton.setOnClickListener(v -> startListening());
        page.addView(micButton);
    }

    private void buildWordBank(LinearLayout work) {
        TextView helper = label("Toca las palabras en orden", 14, MUTED, true);
        helper.setPadding(dp(4), 0, 0, dp(7));
        work.addView(helper);

        answerFlow = new FlowLayout(this);
        answerFlow.setPadding(dp(8), dp(8), dp(8), dp(8));
        answerFlow.setMinimumHeight(dp(76));
        answerFlow.setBackground(roundRect(Color.argb(238, 255, 255, 255), 18, Color.rgb(178, 201, 222), 2));
        work.addView(answerFlow, matchWrapMargin(0, 0, 0, 16));

        bankFlow = new FlowLayout(this);
        bankFlow.setPadding(dp(3), dp(3), dp(3), dp(3));
        work.addView(bankFlow);

        List<String> shuffled = new ArrayList<>(Arrays.asList(currentExercise.options));
        Collections.shuffle(shuffled);
        for (String word : shuffled) addBankChip(word);
    }

    private void addBankChip(String word) {
        TextView chip = chip(word, true);
        chip.setOnClickListener(v -> {
            selectedWords.add(word);
            bankFlow.removeView(chip);
            addAnswerChip(word);
        });
        bankFlow.addView(chip);
    }

    private void addAnswerChip(String word) {
        TextView chip = chip(word, false);
        chip.setOnClickListener(v -> {
            int idx = selectedWords.indexOf(word);
            if (idx >= 0) selectedWords.remove(idx);
            answerFlow.removeView(chip);
            addBankChip(word);
        });
        answerFlow.addView(chip);
    }

    private void buildChoices(LinearLayout work) {
        if (currentExercise.type == TYPE_LISTEN) {
            TextView listen = actionButton("🔊  ESCUCHAR", Color.WHITE, BLUE);
            listen.setBackground(roundRect(Color.WHITE, 18, BLUE, 2));
            listen.setOnClickListener(v -> speak(currentExercise.english));
            work.addView(listen, matchWrapMargin(0, 0, 0, 10));
        }
        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.VERTICAL);
        work.addView(choices);
        for (String option : currentExercise.options) {
            TextView opt = label(option, 19, BLUE_DARK, true);
            opt.setGravity(Gravity.CENTER_VERTICAL);
            opt.setPadding(dp(18), dp(15), dp(18), dp(15));
            opt.setBackground(roundRect(Color.argb(248, 255, 255, 255), 18, BORDER, 2));
            opt.setElevation(dp(3));
            opt.setOnClickListener(v -> {
                selectedChoice = option;
                for (int i = 0; i < choices.getChildCount(); i++) {
                    View child = choices.getChildAt(i);
                    if (child instanceof TextView) child.setBackground(roundRect(Color.argb(248, 255, 255, 255), 18, BORDER, 2));
                }
                opt.setBackground(roundRect(Color.rgb(231, 247, 255), 18, BLUE, 3));
            });
            choices.addView(opt, matchWrapMargin(0, 0, 0, 9));
        }
    }

    private void buildSpeechPractice(LinearLayout work) {
        LinearLayout card = cardColumn();
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        TextView t = label("🎙️ Escucha la frase y repítela. El reconocimiento comparará las palabras principales.", 17, MUTED, false);
        card.addView(t);
        work.addView(card);
    }

    private void checkCurrentAnswer() {
        if (currentExercise.type == TYPE_SPEAK) {
            startListening();
            return;
        }

        boolean correct;
        String given;
        if (currentExercise.type == TYPE_WORD_BANK) {
            given = join(selectedWords);
            if (selectedWords.isEmpty()) {
                Toast.makeText(this, "Selecciona las palabras primero", Toast.LENGTH_SHORT).show();
                return;
            }
            correct = normalize(given).equals(normalize(currentExercise.answer));
        } else {
            given = selectedChoice;
            if (given.isEmpty()) {
                Toast.makeText(this, "Elige una respuesta primero", Toast.LENGTH_SHORT).show();
                return;
            }
            correct = normalize(given).equals(normalize(currentExercise.answer));
        }
        showFeedback(correct, given);
    }

    private void showFeedback(boolean correct, String given) {
        if (correct) awardXp(10);
        else loseHeart();

        final FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.argb(100, 0, 25, 55));
        root.addView(overlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(20), dp(22), dp(20));
        sheet.setBackground(roundRect(Color.WHITE, 28, Color.TRANSPARENT, 0));
        sheet.setElevation(dp(18));

        TextView headline = label(correct ? "✅  ¡Correcto!" : "❌  Incorrecto", 28, correct ? GREEN : RED, true);
        sheet.addView(headline);
        TextView detail = label(correct ? "+10 XP" : "Respuesta correcta: " + currentExercise.answer, 18, BLUE_DARK, true);
        detail.setPadding(0, dp(8), 0, dp(5));
        sheet.addView(detail);
        if (!correct && given != null && !given.trim().isEmpty()) {
            sheet.addView(label("Tu respuesta: " + given, 15, MUTED, false));
        }
        TextView audio = label("🔊  " + currentExercise.english, 17, BLUE, true);
        audio.setPadding(0, dp(10), 0, dp(10));
        audio.setOnClickListener(v -> speak(currentExercise.english));
        sheet.addView(audio);

        TextView next = actionButton(correct ? "SIGUIENTE" : "INTENTAR DE NUEVO", LIME, BLUE_DARK);
        next.setOnClickListener(v -> {
            root.removeView(overlay);
            if (correct) {
                currentQuestion++;
                showExercise();
            } else {
                showExercise();
            }
        });
        sheet.addView(next);

        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        sp.leftMargin = dp(12);
        sp.rightMargin = dp(12);
        sp.bottomMargin = dp(12);
        overlay.addView(sheet, sp);
    }

    private void completeLesson() {
        boolean wasDone = prefs.getBoolean("lesson_" + currentLesson, false);
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean("lesson_" + currentLesson, true);
        if (!wasDone) e.putInt("xp", prefs.getInt("xp", 0) + (currentLesson == lessons.length - 1 ? 100 : 50));
        e.apply();
        updateStreak();

        setRootWithArt(R.drawable.london_route_art);
        LinearLayout done = cardColumn();
        done.setGravity(Gravity.CENTER);
        done.setPadding(dp(26), dp(30), dp(26), dp(30));
        done.addView(label(currentLesson == lessons.length - 1 ? "🏆" : "🎉", 64, BLUE_DARK, true));
        TextView h = label(currentLesson == lessons.length - 1 ? "¡Ruta inicial completada!" : "¡Lección completada!", 30, BLUE_DARK, true);
        h.setGravity(Gravity.CENTER);
        done.addView(h);
        TextView xp = label(currentLesson == lessons.length - 1 ? "+100 XP" : "+50 XP", 22, GREEN, true);
        xp.setGravity(Gravity.CENTER);
        xp.setPadding(0, dp(8), 0, dp(18));
        done.addView(xp);
        TextView btn = actionButton("VOLVER A LA RUTA", LIME, BLUE_DARK);
        btn.setOnClickListener(v -> showRoute());
        done.addView(btn);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        lp.leftMargin = dp(18);
        lp.rightMargin = dp(18);
        root.addView(done, lp);
    }

    private void awardXp(int amount) {
        prefs.edit().putInt("xp", prefs.getInt("xp", 0) + amount).apply();
    }

    private void loseHeart() {
        int hearts = Math.max(0, prefs.getInt("hearts", 5) - 1);
        prefs.edit().putInt("hearts", hearts).apply();
        if (heartsText != null) heartsText.setText("❤️ " + hearts);
    }

    private void updateStreak() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = fmt.format(new Date());
        String last = prefs.getString("last_study", "");
        if (today.equals(last)) return;

        int next = 1;
        if (last != null && !last.isEmpty()) {
            try {
                Date lastDate = fmt.parse(last);
                Calendar cal = Calendar.getInstance();
                cal.setTime(lastDate);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                if (today.equals(fmt.format(cal.getTime()))) next = prefs.getInt("streak", 0) + 1;
            } catch (Exception ignored) {}
        }
        prefs.edit().putString("last_study", today).putInt("streak", next).apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "Recordatorios de estudio", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Recordatorios para estudiar inglés y mantener tu racha");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission(boolean fromOnboarding) {
        notificationPermissionFromOnboarding = fromOnboarding;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
            return;
        }
        prefs.edit().putBoolean("notifications_enabled", true).apply();
        scheduleDailyReminder();
        if (fromOnboarding) finishOnboarding(true);
        else showProfile();
    }

    private PendingIntent reminderPendingIntent() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(this, 9001, intent, flags);
    }

    private void scheduleDailyReminder() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 19);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);
        manager.cancel(reminderPendingIntent());
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), AlarmManager.INTERVAL_DAY, reminderPendingIntent());
    }

    private void cancelDailyReminder() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (manager != null) manager.cancel(reminderPendingIntent());
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "El reconocimiento de voz no está disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 701);
            return;
        }
        try {
            if (recognizer != null) recognizer.destroy();
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { Toast.makeText(MainActivity.this, "🎙️ Escuchando…", Toast.LENGTH_SHORT).show(); }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) { Toast.makeText(MainActivity.this, "No te escuché. Intenta otra vez.", Toast.LENGTH_SHORT).show(); }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String heard = list != null && !list.isEmpty() ? list.get(0) : "";
                    boolean ok = similar(heard, currentExercise.english);
                    showFeedback(ok, heard);
                }
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB");
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            recognizer.startListening(intent);
        } catch (Throwable t) {
            Toast.makeText(this, "No pude iniciar el micrófono", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 701 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
            return;
        }
        if (requestCode == REQ_NOTIFICATIONS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            prefs.edit().putBoolean("notifications_enabled", granted).apply();
            if (granted) scheduleDailyReminder();
            if (notificationPermissionFromOnboarding) finishOnboarding(granted);
            else showProfile();
        }
    }

    private boolean similar(String heard, String expected) {
        String a = normalize(heard);
        String b = normalize(expected);
        if (a.equals(b)) return true;
        String[] words = b.split(" ");
        int hits = 0;
        for (String w : words) if (w.length() > 1 && a.contains(w)) hits++;
        return words.length > 0 && hits / (float) words.length >= 0.65f;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private String join(List<String> words) {
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(w);
        }
        return sb.toString();
    }

    private TextView chip(String text, boolean source) {
        TextView v = label(text, 17, BLUE_DARK, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(15), dp(10), dp(15), dp(10));
        v.setBackground(roundRect(source ? Color.rgb(235, 250, 225) : Color.rgb(230, 245, 255), 16, source ? Color.rgb(109, 206, 69) : BLUE, 2));
        v.setElevation(dp(2));
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        v.setLayoutParams(lp);
        return v;
    }

    private TextView actionButton(String text, int bg, int fg) {
        TextView b = label(text, 17, fg, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(15), dp(16), dp(15));
        b.setBackground(roundRect(bg, 18, Color.argb(80, 0, 0, 0), 1));
        b.setElevation(dp(5));
        return b;
    }

    private TextView pill(String text, int bg, int fg) {
        TextView v = label(text, 14, fg, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(11), dp(8), dp(11), dp(8));
        v.setBackground(roundRect(bg, 18, Color.argb(45, 255, 255, 255), 1));
        return v;
    }

    private TextView label(String text, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        v.setLineSpacing(0f, 1.06f);
        return v;
    }

    private LinearLayout cardColumn() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundRect(Color.argb(247, 255, 255, 255), 24, Color.rgb(181, 218, 239), 1));
        card.setElevation(dp(6));
        return card;
    }

    private GradientDrawable roundRect(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), strokeColor);
        return g;
    }

    private void pulse(View v) {
        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    private LinearLayout.LayoutParams matchWrapMargin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private LinearLayout.LayoutParams wrapMargin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private View withMargin(View v, int l, int t, int r, int b) {
        v.setLayoutParams(matchWrapMargin(l, t, r, b));
        return v;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (showingLessonsWeb) {
            if (lessonsWebView != null && lessonsWebView.canGoBack()) lessonsWebView.goBack();
            else showPractice();
            return;
        }
        if (currentLesson >= 0) showRoute();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        try {
            if (tts != null) { tts.stop(); tts.shutdown(); }
            if (recognizer != null) recognizer.destroy();
        } catch (Throwable ignored) {}
        super.onDestroy();
    }

    private static class Exercise {
        final int type;
        final String prompt;
        final String english;
        final String answer;
        final String[] options;

        Exercise(int type, String prompt, String english, String answer, String[] options) {
            this.type = type;
            this.prompt = prompt;
            this.english = english;
            this.answer = answer;
            this.options = options;
        }

        static Exercise wordBank(String prompt, String english, String answer, String... words) {
            return new Exercise(TYPE_WORD_BANK, prompt, english, answer, words);
        }

        static Exercise choice(String prompt, String english, String answer, String... choices) {
            return new Exercise(TYPE_CHOICE, prompt, english, answer, choices);
        }

        static Exercise listen(String prompt, String english, String answer, String... choices) {
            return new Exercise(TYPE_LISTEN, prompt, english, answer, choices);
        }

        static Exercise speak(String prompt, String english, String answer) {
            return new Exercise(TYPE_SPEAK, prompt, english, answer, new String[0]);
        }
    }

    private static class Lesson {
        final String title;
        final String subtitle;
        final Exercise[] exercises;

        Lesson(String title, String subtitle, Exercise[] exercises) {
            this.title = title;
            this.subtitle = subtitle;
            this.exercises = exercises;
        }
    }

    public static class FlowLayout extends ViewGroup {
        private int lineHeight;

        public FlowLayout(android.content.Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
            int x = 0;
            int y = getPaddingTop();
            int maxLineHeight = 0;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, y);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int cw = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                int ch = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                if (x + cw > width && x > 0) {
                    x = 0;
                    y += maxLineHeight;
                    maxLineHeight = 0;
                }
                x += cw;
                maxLineHeight = Math.max(maxLineHeight, ch);
            }
            lineHeight = maxLineHeight;
            y += maxLineHeight + getPaddingBottom();
            int finalHeight = Math.max(y, getSuggestedMinimumHeight());
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(finalHeight, heightMeasureSpec));
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int width = r - l - getPaddingLeft() - getPaddingRight();
            int x = getPaddingLeft();
            int y = getPaddingTop();
            int currentLineHeight = 0;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int cw = child.getMeasuredWidth();
                int ch = child.getMeasuredHeight();
                int needed = lp.leftMargin + cw + lp.rightMargin;
                if (x + needed > width + getPaddingLeft() && x > getPaddingLeft()) {
                    x = getPaddingLeft();
                    y += currentLineHeight;
                    currentLineHeight = 0;
                }
                int left = x + lp.leftMargin;
                int top = y + lp.topMargin;
                child.layout(left, top, left + cw, top + ch);
                x += needed;
                currentLineHeight = Math.max(currentLineHeight, lp.topMargin + ch + lp.bottomMargin);
            }
        }

        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        @Override
        public LayoutParams generateLayoutParams(android.util.AttributeSet attrs) {
            return new MarginLayoutParams(getContext(), attrs);
        }

        @Override
        protected LayoutParams generateLayoutParams(LayoutParams p) {
            return new MarginLayoutParams(p);
        }

        @Override
        protected boolean checkLayoutParams(LayoutParams p) {
            return p instanceof MarginLayoutParams;
        }
    }
}
