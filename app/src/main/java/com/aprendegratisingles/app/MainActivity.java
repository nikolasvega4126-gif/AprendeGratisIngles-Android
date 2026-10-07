package com.aprendegratisingles.app;

import android.Manifest;
import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
            new Lesson("Saludos en inglés", "Saludos cotidianos y frases básicas", new Exercise[]{
                    Exercise.wordBank("Traduce esta oración", "Good morning, how are you?", "Buenos días ¿cómo estás?", "Buenos", "días", "¿cómo", "estás?", "noche", "gracias"),
                    Exercise.choice("¿Qué significa “Hello”?", "Hello", "Hola", "Hola", "Gracias", "Adiós", "Por favor"),
                    Exercise.listen("Escucha y elige la traducción", "Good night", "Buenas noches", "Buenas noches", "Buenos días", "Hasta luego", "Gracias"),
                    Exercise.wordBank("Traduce esta frase", "Nice to meet you", "Mucho gusto", "Mucho", "gusto", "Buenas", "tardes", "por", "favor"),
                    Exercise.speak("Repite la frase", "Goodbye, see you later", "Adiós, nos vemos luego")
            }),
            new Lesson("Di de dónde eres", "Países y presentaciones", new Exercise[]{
                    Exercise.wordBank("Traduce esta oración", "I am from Poland", "Soy de Polonia", "Soy", "de", "Polonia", "Francia", "España"),
                    Exercise.choice("¿Qué significa esta pregunta?", "Where are you from?", "¿De dónde eres?", "¿De dónde eres?", "¿Cómo te llamas?", "¿Dónde trabajas?", "¿Qué hora es?"),
                    Exercise.choice("Elige la traducción correcta", "France", "Francia", "Francia", "Alemania", "Italia", "Inglaterra"),
                    Exercise.wordBank("Traduce esta oración", "I am from Spain", "Soy de España", "Soy", "de", "España", "Italia", "Alemania"),
                    Exercise.speak("Repite la frase", "I am from Germany", "Soy de Alemania")
            }),
            new Lesson("Números del 1 al 10", "Reconoce, escucha y usa números básicos", new Exercise[]{
                    Exercise.choice("¿Qué número es “Seven”?", "Seven", "7", "7", "5", "8", "10"),
                    Exercise.choice("¿Qué número es “Ten”?", "Ten", "10", "10", "2", "6", "9"),
                    Exercise.listen("Escucha y elige el número", "Four", "4", "4", "3", "5", "8"),
                    Exercise.wordBank("Traduce esta oración", "I have three books", "Tengo tres libros", "Tengo", "tres", "libros", "dos", "casa"),
                    Exercise.speak("Repite los números", "One, two, three, four, five", "Uno, dos, tres, cuatro, cinco")
            }),
            new Lesson("Examen de unidad", "Mezcla de saludos, países y números", new Exercise[]{
                    Exercise.choice("¿Qué significa “Good morning”?", "Good morning", "Buenos días", "Buenos días", "Buenas noches", "Gracias", "Adiós"),
                    Exercise.wordBank("Traduce esta oración", "I am from Italy", "Soy de Italia", "Soy", "de", "Italia", "Polonia", "Francia"),
                    Exercise.listen("Escucha y elige", "Nine", "9", "9", "6", "7", "10"),
                    Exercise.choice("¿Qué significa “Where are you from?”", "Where are you from?", "¿De dónde eres?", "¿De dónde eres?", "¿Cómo estás?", "¿Qué edad tienes?", "¿Dónde está Londres?"),
                    Exercise.wordBank("Traduce esta frase", "Thank you", "Gracias", "Gracias", "Hola", "días", "favor"),
                    Exercise.choice("¿Qué número es “Three”?", "Three", "3", "3", "2", "4", "8"),
                    Exercise.speak("Repite la frase", "Nice to meet you", "Mucho gusto")
            })
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        ensureDefaults();
        initTts();
        showRoute();
    }

    private void ensureDefaults() {
        if (!prefs.contains("hearts")) prefs.edit().putInt("hearts", 5).apply();
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
        body.setPadding(dp(6), dp(8), dp(6), dp(24));
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout unitCard = cardColumn();
        unitCard.setPadding(dp(22), dp(18), dp(22), dp(18));
        unitCard.addView(label("UNIDAD 1", 14, BLUE, true));
        TextView title = label("Primeros pasos", 32, BLUE_DARK, true);
        title.setPadding(0, dp(6), 0, 0);
        unitCard.addView(title);
        TextView sub = label("Saludos, presentaciones y números básicos.", 18, MUTED, false);
        sub.setPadding(0, dp(6), 0, dp(8));
        unitCard.addView(sub);
        unitCard.addView(label("🇬🇧  LONDRES · EMPIEZA TU AVENTURA", 15, BLUE_DARK, true));
        body.addView(unitCard, matchWrapMargin(0, 0, 0, 18));

        body.addView(lessonRow(0, "👋", "Saludos en inglés", "JUGAR · SIGUIENTE", true));
        body.addView(connector());
        boolean l1 = prefs.getBoolean("lesson_0", false);
        body.addView(lessonRow(1, "🌍", "Di de dónde eres", l1 ? "JUGAR" : "BLOQUEADA", l1));
        body.addView(connector());
        boolean l2 = prefs.getBoolean("lesson_1", false);
        body.addView(lessonRow(2, "🔢", "Números del 1 al 10", l2 ? "JUGAR" : "BLOQUEADA", l2));
        body.addView(connector());
        boolean exam = prefs.getBoolean("lesson_0", false) && prefs.getBoolean("lesson_1", false) && prefs.getBoolean("lesson_2", false);
        body.addView(lessonRow(3, "🏆", "Examen de unidad", exam ? "EMPEZAR EXAMEN" : "BLOQUEADO", exam));

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
        TextView level = label("🥉  Nivel " + levelForXp(xp) + " · Bronce\n" + xp + " XP", 15, Color.WHITE, true);
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

    private View lessonRow(int index, String icon, String title, String subtitle, boolean unlocked) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView node = label(unlocked ? (index == 3 ? "🎁" : "▶") : "🔒", index == 3 ? 34 : 30, unlocked ? Color.WHITE : Color.rgb(114, 132, 148), true);
        node.setGravity(Gravity.CENTER);
        int nodeColor = unlocked ? (index == 3 ? Color.rgb(255, 187, 0) : GREEN) : Color.rgb(218, 227, 236);
        node.setBackground(roundRect(nodeColor, 100, Color.WHITE, 5));
        node.setElevation(dp(8));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(dp(index == 3 ? 102 : 86), dp(index == 3 ? 102 : 86));
        np.rightMargin = dp(12);
        row.addView(node, np);

        LinearLayout card = cardColumn();
        card.setPadding(dp(18), dp(15), dp(18), dp(15));
        card.addView(label(icon + "  " + title, 21, BLUE_DARK, true));
        TextView st = label(subtitle, 13, unlocked ? (index == 0 ? Color.rgb(20, 175, 59) : BLUE) : MUTED, true);
        st.setPadding(0, dp(5), 0, 0);
        card.addView(st);
        card.setAlpha(unlocked ? 1f : 0.94f);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(card, cp);

        View.OnClickListener click = v -> {
            if (!unlocked) {
                String msg = index == 3 ? "Completa las 3 lecciones para desbloquear el examen" : "Completa la lección anterior para desbloquear esta";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

        String[] icons = {"🗺️", "🧠", "🏆", "👤"};
        String[] labels = {"Ruta", "Practicar", "Logros", "Perfil"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView item = label(icons[i] + "\n" + labels[i], 13, i == selected ? BLUE : BLUE_DARK, true);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(6), dp(6), dp(6), dp(6));
            if (i == selected) item.setBackground(roundRect(Color.rgb(232, 246, 255), 16, Color.TRANSPARENT, 0));
            item.setOnClickListener(v -> {
                if (idx == 0) showRoute();
                else if (idx == 1) showPractice();
                else if (idx == 2) showAchievements();
                else showProfile();
            });
            nav.addView(item, new LinearLayout.LayoutParams(0, dp(58), 1f));
        }
        return nav;
    }

    private void showPractice() {
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
        page.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(1));
        root.addView(page);
    }

    private int firstUnlockedPracticeLesson() {
        if (prefs.getBoolean("lesson_2", false)) return 2;
        if (prefs.getBoolean("lesson_1", false)) return 1;
        return 0;
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
        body.addView(statCard("🏆 Primer paso", prefs.getBoolean("lesson_0", false) ? "COMPLETADO" : "Completa Saludos en inglés"));
        body.addView(statCard("🔥 Constancia", prefs.getInt("streak", 0) + " días de racha"));
        body.addView(statCard("⭐ Experiencia", prefs.getInt("xp", 0) + " XP acumulados"));
        body.addView(statCard("🇬🇧 Unidad 1", completedCount() + "/3 lecciones completadas"));
        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(2));
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
        for (int i = 0; i < 3; i++) if (prefs.getBoolean("lesson_" + i, false)) c++;
        return c;
    }

    private void showProfile() {
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        page.addView(buildHud());
        LinearLayout profile = cardColumn();
        profile.setGravity(Gravity.CENTER_HORIZONTAL);
        profile.setPadding(dp(24), dp(28), dp(24), dp(28));
        profile.addView(label("👤", 52, BLUE_DARK, true));
        TextView name = label("Estudiante de inglés", 27, BLUE_DARK, true);
        name.setGravity(Gravity.CENTER);
        profile.addView(name);
        TextView stats = label("Nivel " + levelForXp(prefs.getInt("xp", 0)) + "  ·  " + prefs.getInt("xp", 0) + " XP\n" + completedCount() + " lecciones completadas", 17, MUTED, false);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, dp(10), 0, dp(18));
        profile.addView(stats);
        TextView refill = actionButton("RECARGAR CORAZONES", Color.WHITE, BLUE);
        refill.setBackground(roundRect(Color.WHITE, 18, BLUE, 2));
        refill.setOnClickListener(v -> {
            prefs.edit().putInt("hearts", 5).apply();
            Toast.makeText(this, "Corazones restaurados", Toast.LENGTH_SHORT).show();
            showProfile();
        });
        profile.addView(refill);
        page.addView(profile, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(3));
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
        titleCard.addView(label(lesson.title, 17, BLUE, true));
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
        if (!wasDone) e.putInt("xp", prefs.getInt("xp", 0) + (currentLesson == 3 ? 100 : 50));
        e.apply();
        updateStreak();

        setRootWithArt(R.drawable.london_route_art);
        LinearLayout done = cardColumn();
        done.setGravity(Gravity.CENTER);
        done.setPadding(dp(26), dp(30), dp(26), dp(30));
        done.addView(label(currentLesson == 3 ? "🏆" : "🎉", 64, BLUE_DARK, true));
        TextView h = label(currentLesson == 3 ? "¡Unidad 1 completada!" : "¡Lección completada!", 30, BLUE_DARK, true);
        h.setGravity(Gravity.CENTER);
        done.addView(h);
        TextView xp = label(currentLesson == 3 ? "+100 XP" : "+50 XP", 22, GREEN, true);
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
        if (requestCode == 701 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startListening();
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
