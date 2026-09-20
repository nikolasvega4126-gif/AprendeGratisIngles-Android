package com.aprendegratisingles.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String HOME = "https://www.aprendegratisingles.com/";
    private static final String FEED = HOME + "feeds/posts/default?alt=json&max-results=50&orderby=published";
    private static final String PREFS = "agi_prefs";

    private static final String KEY_FEED_CACHE = "feed_cache";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_LAST_OPENED = "last_opened";
    private static final String KEY_REMINDER = "reminder_enabled";
    private static final String KEY_ONBOARDED = "onboarded_v3";
    private static final String KEY_START_MODE = "start_mode";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_GOAL = "goal";
    private static final String KEY_DAILY_MINUTES = "daily_minutes";
    private static final String KEY_START_INDEX = "start_index";
    private static final String KEY_XP = "xp";
    private static final String KEY_TODAY_XP = "today_xp";
    private static final String KEY_DAY = "day_key";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_STUDY_DAY = "last_study_day";
    private static final String KEY_LIVES = "lives";
    private static final String KEY_ERRORS = "quiz_errors";

    private static final int BLUE = Color.rgb(13, 115, 217);
    private static final int BLUE_DARK = Color.rgb(7, 63, 141);
    private static final int GREEN = Color.rgb(24, 184, 75);
    private static final int GREEN_DARK = Color.rgb(11, 141, 54);
    private static final int ORANGE = Color.rgb(255, 159, 28);
    private static final int RED = Color.rgb(232, 78, 78);
    private static final int PURPLE = Color.rgb(118, 83, 219);
    private static final int BG = Color.rgb(246, 250, 254);
    private static final int TEXT = Color.rgb(23, 50, 77);
    private static final int MUTED = Color.rgb(97, 113, 132);
    private static final int BORDER = Color.rgb(220, 231, 242);

    private static final String[] COURSE_ORDER = {
            "saludos", "pronunciar th", "numeros", "familia", "frases cotidianas", "verbos basicos",
            "pronombres personales", "verbo to be", "a an y the", "adjetivos posesivos", "preguntas basicas",
            "dias de la semana", "meses del ano", "decir la hora", "colores", "partes del cuerpo",
            "comida y bebidas", "la casa", "presente simple", "rutina diaria"
    };

    private static final String[][] QUIZ = {
            {"¿Qué significa 'Hello'?", "Hola", "Adiós", "Gracias", "Hola"},
            {"¿Cuál es el pronombre para 'ella'?", "He", "She", "They", "She"},
            {"¿Cuál forma de To Be corresponde a 'I'?", "is", "are", "am", "am"},
            {"¿Qué artículo va antes de 'apple'?", "a", "an", "the siempre", "an"},
            {"¿Qué significa 'family'?", "Familia", "Amigo", "Casa", "Familia"},
            {"¿Qué significa 'drink'?", "Dormir", "Beber", "Leer", "Beber"},
            {"¿Qué significa 'blue'?", "Azul", "Verde", "Rojo", "Azul"},
            {"¿Qué pronombre significa 'ellos/ellas'?", "We", "They", "You", "They"},
            {"¿Qué significa 'mother'?", "Hermana", "Madre", "Hija", "Madre"},
            {"¿Qué significa 'Thank you'?", "Por favor", "Gracias", "Lo siento", "Gracias"}
    };

    private FrameLayout contentHost;
    private ProgressBar topProgress;
    private TextView streakChip;
    private TextView xpChip;
    private TextView livesChip;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private WebView pendingTtsWebView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<PostItem> posts = new ArrayList<>();
    private SharedPreferences prefs;
    private String currentSection = "path";
    private boolean shellBuilt = false;

    private int onboardingStep = 0;
    private String selectedStartMode = "Desde cero";
    private String selectedLevel = "Básico";
    private String selectedGoal = "Hablar inglés";
    private int selectedMinutes = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        getWindow().setStatusBarColor(BLUE_DARK);
        getWindow().setNavigationBarColor(Color.WHITE);
        initTextToSpeech();
        ensureDayState();

        if (!prefs.getBoolean(KEY_ONBOARDED, false)) {
            showOnboarding();
        } else {
            launchApp();
        }
    }

    private void launchApp() {
        if (!shellBuilt) buildShell();
        showPath();
        loadFeed(false);
        requestNotificationPermissionIfNeeded();
        if (prefs.getBoolean(KEY_REMINDER, true)) scheduleDailyReminder();
    }

    private void showOnboarding() {
        onboardingStep = 0;
        renderOnboardingStep();
    }

    private void renderOnboardingStep() {
        LinearLayout root = verticalBox();
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(22), dp(22), dp(22), dp(22));

        LinearLayout top = horizontal();
        if (onboardingStep > 0) {
            top.addView(smallButton("←", () -> {
                onboardingStep--;
                renderOnboardingStep();
            }), new LinearLayout.LayoutParams(dp(52), dp(44)));
        } else {
            top.addView(spacerW(52));
        }

        TextView stepText = new TextView(this);
        stepText.setText((onboardingStep + 1) + " / 4");
        stepText.setTextColor(MUTED);
        stepText.setTextSize(13);
        stepText.setGravity(Gravity.CENTER);
        top.addView(stepText, new LinearLayout.LayoutParams(0, dp(44), 1f));
        top.addView(spacerW(52));
        root.addView(top);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(4);
        progress.setProgress(onboardingStep + 1);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        root.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)));

        root.addView(spacer(22));
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(92), dp(92));
        logoLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logo, logoLp);
        root.addView(spacer(16));

        String question;
        String subtitle;
        if (onboardingStep == 0) {
            question = "¿Cómo quieres empezar?";
            subtitle = "Usaremos tu respuesta para organizar tu ruta de aprendizaje.";
        } else if (onboardingStep == 1) {
            question = "¿Cuál es tu nivel actual?";
            subtitle = "No es un examen. Solo sirve para colocarte en un punto razonable.";
        } else if (onboardingStep == 2) {
            question = "¿Cuál es tu objetivo principal?";
            subtitle = "La app priorizará prácticas relacionadas con ese objetivo.";
        } else {
            question = "¿Cuánto quieres estudiar al día?";
            subtitle = "Una meta pequeña y constante suele funcionar mejor que una heroica que dura dos días.";
        }

        TextView q = heading(question, 27, BLUE_DARK);
        q.setGravity(Gravity.CENTER);
        root.addView(q);
        TextView sub = body(subtitle);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);
        root.addView(spacer(16));

        if (onboardingStep == 0) {
            root.addView(optionButton("🌱  Desde cero", selectedStartMode.equals("Desde cero"), () -> chooseOnboarding("Desde cero")));
            root.addView(optionButton("📘  Ya sé un poco", selectedStartMode.equals("Ya sé un poco"), () -> chooseOnboarding("Ya sé un poco")));
            root.addView(optionButton("🧠  Quiero practicar", selectedStartMode.equals("Quiero practicar"), () -> chooseOnboarding("Quiero practicar")));
        } else if (onboardingStep == 1) {
            root.addView(optionButton("Básico", selectedLevel.equals("Básico"), () -> chooseOnboarding("Básico")));
            root.addView(optionButton("Intermedio", selectedLevel.equals("Intermedio"), () -> chooseOnboarding("Intermedio")));
            root.addView(optionButton("Avanzado", selectedLevel.equals("Avanzado"), () -> chooseOnboarding("Avanzado")));
        } else if (onboardingStep == 2) {
            root.addView(optionButton("🗣️  Hablar inglés", selectedGoal.equals("Hablar inglés"), () -> chooseOnboarding("Hablar inglés")));
            root.addView(optionButton("👂  Entender conversaciones", selectedGoal.equals("Entender conversaciones"), () -> chooseOnboarding("Entender conversaciones")));
            root.addView(optionButton("✈️  Viajar", selectedGoal.equals("Viajar"), () -> chooseOnboarding("Viajar")));
            root.addView(optionButton("💼  Trabajar", selectedGoal.equals("Trabajar"), () -> chooseOnboarding("Trabajar")));
            root.addView(optionButton("📚  Vocabulario", selectedGoal.equals("Vocabulario"), () -> chooseOnboarding("Vocabulario")));
        } else {
            root.addView(optionButton("5 minutos", selectedMinutes == 5, () -> chooseOnboardingMinutes(5)));
            root.addView(optionButton("10 minutos", selectedMinutes == 10, () -> chooseOnboardingMinutes(10)));
            root.addView(optionButton("15 minutos", selectedMinutes == 15, () -> chooseOnboardingMinutes(15)));
            root.addView(optionButton("20 minutos", selectedMinutes == 20, () -> chooseOnboardingMinutes(20)));
        }

        TextView privacy = body("Puedes cambiar estas preferencias después desde Perfil.");
        privacy.setGravity(Gravity.CENTER);
        root.addView(spacer(12));
        root.addView(privacy);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private Button optionButton(String text, boolean selected, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(17);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(dp(18), 0, dp(18), 0);
        b.setTextColor(selected ? Color.WHITE : BLUE_DARK);
        b.setBackground(rounded(selected ? BLUE : Color.WHITE, selected ? BLUE : BORDER, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62));
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void chooseOnboarding(String value) {
        if (onboardingStep == 0) selectedStartMode = value;
        else if (onboardingStep == 1) selectedLevel = value;
        else if (onboardingStep == 2) selectedGoal = value;
        advanceOnboarding();
    }

    private void chooseOnboardingMinutes(int value) {
        selectedMinutes = value;
        finishOnboarding();
    }

    private void advanceOnboarding() {
        onboardingStep++;
        renderOnboardingStep();
    }

    private void finishOnboarding() {
        int startIndex;
        if ("Avanzado".equals(selectedLevel)) startIndex = 14;
        else if ("Intermedio".equals(selectedLevel)) startIndex = 8;
        else startIndex = 0;
        if ("Ya sé un poco".equals(selectedStartMode)) startIndex = Math.min(16, startIndex + 2);

        prefs.edit()
                .putBoolean(KEY_ONBOARDED, true)
                .putString(KEY_START_MODE, selectedStartMode)
                .putString(KEY_LEVEL, selectedLevel)
                .putString(KEY_GOAL, selectedGoal)
                .putInt(KEY_DAILY_MINUTES, selectedMinutes)
                .putInt(KEY_START_INDEX, startIndex)
                .putBoolean(KEY_REMINDER, true)
                .apply();

        launchApp();
        Toast.makeText(this, "Tu ruta personalizada está lista", Toast.LENGTH_LONG).show();
        if ("Quiero practicar".equals(selectedStartMode)) showPracticeHub();
    }

    private void buildShell() {
        shellBuilt = true;
        LinearLayout root = verticalBox();
        root.setBackgroundColor(BG);

        LinearLayout header = horizontal();
        header.setPadding(dp(12), dp(8), dp(12), dp(8));
        header.setBackgroundColor(BLUE_DARK);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(44), dp(44));
        logoLp.setMargins(0, 0, dp(8), 0);
        header.addView(logo, logoLp);

        LinearLayout titles = verticalBox();
        TextView title = new TextView(this);
        title.setText("Aprende gratis inglés");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        TextView subtitle = new TextView(this);
        subtitle.setText("Tu ruta personal");
        subtitle.setTextColor(Color.rgb(211, 233, 255));
        subtitle.setTextSize(11);
        titles.addView(title);
        titles.addView(subtitle);
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout stats = horizontal();
        stats.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        streakChip = statChip("🔥 0", ORANGE);
        xpChip = statChip("⭐ 0", Color.rgb(255, 201, 53));
        livesChip = statChip("❤️ 5", RED);
        stats.addView(streakChip);
        stats.addView(xpChip);
        stats.addView(livesChip);
        header.addView(stats);
        root.addView(header);

        topProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        topProgress.setMax(100);
        topProgress.setIndeterminate(false);
        topProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        topProgress.setVisibility(View.GONE);
        root.addView(topProgress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        contentHost = new FrameLayout(this);
        root.addView(contentHost, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout nav = horizontal();
        nav.setPadding(dp(4), dp(5), dp(4), dp(6));
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));
        addBottomButton(nav, "🗺️\nRuta", this::showPath);
        addBottomButton(nav, "🧠\nPracticar", this::showPracticeHub);
        addBottomButton(nav, "🏆\nLogros", this::showAchievements);
        addBottomButton(nav, "👤\nPerfil", this::showProfile);
        root.addView(nav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
        updateHeaderStats();
    }

    private TextView statChip(String text, int accent) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(11);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setGravity(Gravity.CENTER);
        GradientDrawable bg = rounded(Color.argb(55, 255, 255, 255), Color.argb(0, 0, 0, 0), 13);
        v.setBackground(bg);
        v.setPadding(dp(8), dp(6), dp(8), dp(6));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(3), 0, dp(3), 0);
        v.setLayoutParams(lp);
        return v;
    }

    private void addBottomButton(LinearLayout nav, String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(10);
        b.setAllCaps(false);
        b.setTextColor(BLUE_DARK);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setOnClickListener(v -> action.run());
        nav.addView(b, new LinearLayout.LayoutParams(0, dp(58), 1f));
    }

    private void showPath() {
        currentSection = "path";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(30));
        scroll.addView(box);

        LinearLayout welcome = card();
        welcome.setBackground(rounded(Color.rgb(234, 246, 255), Color.rgb(203, 227, 246), 22));
        welcome.addView(label("TU RUTA PERSONALIZADA", BLUE));
        welcome.addView(heading("Sigue avanzando paso a paso", 24, BLUE_DARK));
        String goal = prefs.getString(KEY_GOAL, "Hablar inglés");
        int mins = prefs.getInt(KEY_DAILY_MINUTES, 10);
        welcome.addView(body("Objetivo: " + goal + " · Meta diaria: " + mins + " min"));
        LinearLayout shortcuts = horizontal();
        shortcuts.addView(secondaryButton("Todas las lecciones", this::showAllLessons), new LinearLayout.LayoutParams(0, dp(45), 1f));
        LinearLayout.LayoutParams favLp = new LinearLayout.LayoutParams(0, dp(45), 1f);
        favLp.setMargins(dp(8), 0, 0, 0);
        shortcuts.addView(secondaryButton("♥ Favoritos", this::showFavorites), favLp);
        welcome.addView(shortcuts);
        box.addView(welcome);

        box.addView(dailyGoalCard());
        box.addView(spacer(12));

        TextView unit = heading("Unidad 1 · Inglés desde cero", 22, BLUE_DARK);
        unit.setGravity(Gravity.CENTER);
        box.addView(unit);
        TextView unitSub = body("Completa una lección para desbloquear la siguiente.");
        unitSub.setGravity(Gravity.CENTER);
        box.addView(unitSub);

        List<PostItem> ordered = getOrderedPosts();
        if (ordered.isEmpty()) {
            box.addView(cardMessage("Cargando tu ruta…", "Estamos sincronizando las lecciones publicadas en tu web."));
        } else {
            int startIndex = Math.min(prefs.getInt(KEY_START_INDEX, 0), Math.max(0, ordered.size() - 1));
            int current = findCurrentIndex(ordered, startIndex);
            Set<String> done = completedSet();

            for (int i = 0; i < ordered.size(); i++) {
                PostItem p = ordered.get(i);
                boolean placementPassed = i < startIndex;
                boolean completed = done.contains(p.url);
                boolean isCurrent = i == current;
                boolean locked = i > current;
                box.addView(pathNode(p, i, completed, placementPassed, isCurrent, locked));
                if (i < ordered.size() - 1) box.addView(pathConnector(i, current));
            }

            if (done.size() >= ordered.size()) {
                box.addView(cardMessage("🏆 Ruta completada", "Has terminado todas las lecciones disponibles. Las nuevas publicaciones se añadirán automáticamente."));
            }
        }

        setContent(scroll);
    }

    private View dailyGoalCard() {
        LinearLayout c = card();
        c.addView(label("MISIÓN DE HOY", ORANGE));
        int goalXp = dailyXpGoal();
        int todayXp = prefs.getInt(KEY_TODAY_XP, 0);
        int pct = Math.min(100, Math.round(todayXp * 100f / Math.max(1, goalXp)));
        c.addView(heading(todayXp + " / " + goalXp + " XP", 21, BLUE_DARK));
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(pct);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        c.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        c.addView(body(pct >= 100 ? "✓ Meta diaria completada" : "Completa lecciones o práctica para llenar tu meta diaria."));
        return c;
    }

    private View pathNode(PostItem p, int index, boolean completed, boolean placementPassed, boolean current, boolean locked) {
        LinearLayout row = horizontal();
        row.setGravity(Gravity.CENTER_VERTICAL);

        boolean right = index % 2 == 1;
        if (right) row.addView(weightSpacer());

        LinearLayout nodeWrap = verticalBox();
        nodeWrap.setGravity(Gravity.CENTER_HORIZONTAL);

        Button node = new Button(this);
        if (completed || placementPassed) node.setText("✓");
        else if (locked) node.setText("🔒");
        else node.setText("▶");
        node.setTextSize(current ? 24 : 20);
        node.setTextColor(Color.WHITE);
        node.setAllCaps(false);
        node.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        int fill = completed ? GREEN : placementPassed ? PURPLE : locked ? Color.rgb(184, 196, 207) : BLUE;
        circle.setColor(fill);
        circle.setStroke(dp(5), current ? Color.rgb(168, 217, 255) : Color.argb(0, 0, 0, 0));
        node.setBackground(circle);
        node.setElevation(current ? dp(8) : dp(3));
        LinearLayout.LayoutParams nodeLp = new LinearLayout.LayoutParams(current ? dp(82) : dp(72), current ? dp(82) : dp(72));
        nodeWrap.addView(node, nodeLp);

        TextView name = new TextView(this);
        name.setText(shortLessonTitle(p.title));
        name.setTextSize(13);
        name.setTextColor(locked ? Color.rgb(130, 145, 159) : BLUE_DARK);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(2);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(dp(170), ViewGroup.LayoutParams.WRAP_CONTENT);
        nameLp.setMargins(0, dp(5), 0, 0);
        nodeWrap.addView(name, nameLp);

        if (current) {
            TextView start = label("SIGUIENTE", BLUE);
            start.setGravity(Gravity.CENTER);
            nodeWrap.addView(start);
        }

        node.setOnClickListener(v -> {
            if (locked) {
                Toast.makeText(this, "Completa la lección anterior para desbloquearla", Toast.LENGTH_SHORT).show();
            } else {
                openPost(p);
            }
        });
        name.setOnClickListener(v -> node.performClick());

        row.addView(nodeWrap, new LinearLayout.LayoutParams(dp(190), ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!right) row.addView(weightSpacer());
        return row;
    }

    private View pathConnector(int index, int current) {
        LinearLayout holder = horizontal();
        holder.setGravity(Gravity.CENTER);
        View line = new View(this);
        line.setBackgroundColor(index < current ? GREEN : Color.rgb(214, 223, 231));
        holder.addView(line, new LinearLayout.LayoutParams(dp(6), dp(30)));
        return holder;
    }

    private View weightSpacer() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        return v;
    }

    private void showPracticeHub() {
        currentSection = "practice";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        box.addView(heading("Practicar", 27, BLUE_DARK));
        box.addView(body("Refuerza lo aprendido con sesiones cortas. Tus errores quedan guardados para repasarlos después."));

        LinearLayout quick = card();
        quick.addView(label("PRÁCTICA RÁPIDA", BLUE));
        quick.addView(heading("10 preguntas", 21, BLUE_DARK));
        quick.addView(body("Gana 10 XP por cada respuesta correcta. Una respuesta incorrecta consume un corazón."));
        quick.addView(primaryButton("🧠 Empezar", () -> showQuiz(false)));
        box.addView(quick);

        LinearLayout mistakes = card();
        int errors = errorSet().size();
        mistakes.addView(label("REPASO INTELIGENTE", RED));
        mistakes.addView(heading("Repasar mis errores", 21, BLUE_DARK));
        mistakes.addView(body(errors == 0 ? "No tienes errores pendientes. Sospechosamente competente." : "Tienes " + errors + " concepto(s) pendientes de repaso."));
        mistakes.addView(secondaryButton(errors == 0 ? "✓ Todo al día" : "↻ Repasar ahora", () -> showQuiz(true)));
        box.addView(mistakes);

        LinearLayout pronunciation = card();
        pronunciation.addView(label("PRONUNCIACIÓN", GREEN));
        pronunciation.addView(heading("Escucha y repite", 21, BLUE_DARK));
        pronunciation.addView(body("Usa el audio nativo de Android para practicar palabras y frases del curso."));
        pronunciation.addView(primaryButton("🗣️ Abrir pronunciación", () -> openUrl(HOME + "p/pronunciacion-facil.html")));
        box.addView(pronunciation);

        LinearLayout browse = card();
        browse.addView(label("BIBLIOTECA", PURPLE));
        browse.addView(heading("Buscar una lección", 21, BLUE_DARK));
        browse.addView(body("Encuentra verbos, familia, colores, To Be y cualquier nueva lección publicada."));
        browse.addView(secondaryButton("🔎 Buscar", this::showAllLessons));
        box.addView(browse);

        setContent(scroll);
    }

    private void showQuiz(boolean reviewOnly) {
        Set<String> savedErrors = errorSet();
        List<Integer> indexes = new ArrayList<>();
        if (reviewOnly) {
            for (String s : savedErrors) {
                try {
                    int idx = Integer.parseInt(s);
                    if (idx >= 0 && idx < QUIZ.length) indexes.add(idx);
                } catch (Exception ignored) {}
            }
            Collections.sort(indexes);
        } else {
            for (int i = 0; i < QUIZ.length; i++) indexes.add(i);
        }

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        box.addView(heading(reviewOnly ? "Repasar errores" : "Práctica rápida", 26, BLUE_DARK));
        TextView score = body("⭐ +10 XP por acierto · ❤️ " + prefs.getInt(KEY_LIVES, 5) + " corazones");
        box.addView(score);

        if (indexes.isEmpty()) {
            box.addView(cardMessage("✓ Sin errores pendientes", "Completa otra práctica y aquí aparecerán los conceptos que necesiten refuerzo."));
            box.addView(primaryButton("Volver a practicar", () -> showQuiz(false)));
            setContent(scroll);
            return;
        }

        final int total = indexes.size();
        final int[] correct = {0};
        final int[] answered = {0};

        for (int display = 0; display < indexes.size(); display++) {
            int questionIndex = indexes.get(display);
            String[] q = QUIZ[questionIndex];
            LinearLayout c = card();
            c.setTag(questionIndex);
            c.addView(label("PREGUNTA " + (display + 1) + " DE " + total, BLUE));
            c.addView(heading(q[0], 18, TEXT));

            for (int a = 1; a <= 3; a++) {
                String option = q[a];
                Button b = secondaryButton(option, () -> {});
                b.setOnClickListener(v -> {
                    if (!v.isEnabled()) return;
                    ViewGroup parent = (ViewGroup) v.getParent();
                    for (int k = 0; k < parent.getChildCount(); k++) {
                        View child = parent.getChildAt(k);
                        if (child instanceof Button) child.setEnabled(false);
                    }

                    answered[0]++;
                    Set<String> errs = errorSet();
                    if (option.equals(q[4])) {
                        correct[0]++;
                        ((Button) v).setText("✓ " + option);
                        ((Button) v).setTextColor(GREEN_DARK);
                        errs.remove(String.valueOf(questionIndex));
                        saveErrors(errs);
                        awardXp(10);
                    } else {
                        ((Button) v).setText("✗ " + option);
                        ((Button) v).setTextColor(RED);
                        errs.add(String.valueOf(questionIndex));
                        saveErrors(errs);
                        loseHeart();
                    }

                    score.setText("Puntuación: " + correct[0] + " / " + answered[0] + "   ·   ❤️ " + prefs.getInt(KEY_LIVES, 5));
                    if (answered[0] == total) {
                        recordStudyActivity();
                        if (correct[0] == total) awardXp(20);
                        Toast.makeText(this, "Sesión terminada: " + correct[0] + "/" + total, Toast.LENGTH_LONG).show();
                    }
                });
                c.addView(b);
            }
            box.addView(c);
        }

        setContent(scroll);
    }

    private void showAchievements() {
        currentSection = "achievements";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        int xp = prefs.getInt(KEY_XP, 0);
        int streak = prefs.getInt(KEY_STREAK, 0);
        int done = completedSet().size();
        int total = Math.max(1, getOrderedPosts().size());

        box.addView(heading("Tus logros", 27, BLUE_DARK));
        box.addView(body("No cambian tu nivel de inglés por magia, pero hacen visible el progreso. Los humanos parecen apreciar esas cosas."));
        box.addView(achievementCard("🌱", "Primer paso", "Completa tu primera lección", done >= 1));
        box.addView(achievementCard("⭐", "100 XP", "Consigue 100 puntos de experiencia", xp >= 100));
        box.addView(achievementCard("📚", "Estudiante constante", "Completa 5 lecciones", done >= 5));
        box.addView(achievementCard("🔥", "Racha de 3", "Estudia 3 días seguidos", streak >= 3));
        box.addView(achievementCard("🔥", "Racha de 7", "Estudia 7 días seguidos", streak >= 7));
        box.addView(achievementCard("🏆", "Ruta completada", "Completa todas las lecciones disponibles", done >= total && total > 1));
        setContent(scroll);
    }

    private View achievementCard(String icon, String title, String description, boolean unlocked) {
        LinearLayout c = card();
        LinearLayout row = horizontal();
        TextView badge = new TextView(this);
        badge.setText(unlocked ? icon : "🔒");
        badge.setTextSize(32);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(rounded(unlocked ? Color.rgb(235, 250, 240) : Color.rgb(240, 243, 246), BORDER, 18));
        row.addView(badge, new LinearLayout.LayoutParams(dp(64), dp(64)));
        LinearLayout txt = verticalBox();
        txt.setPadding(dp(12), 0, 0, 0);
        txt.addView(heading(title, 18, unlocked ? BLUE_DARK : Color.rgb(135, 146, 157)));
        txt.addView(body(description));
        row.addView(txt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        c.addView(row);
        return c;
    }

    private void showProfile() {
        currentSection = "profile";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        box.addView(heading("Tu perfil", 27, BLUE_DARK));
        LinearLayout profile = card();
        profile.addView(label("PLAN DE ESTUDIO", BLUE));
        profile.addView(heading(prefs.getString(KEY_LEVEL, "Básico"), 23, BLUE_DARK));
        profile.addView(body("Inicio: " + prefs.getString(KEY_START_MODE, "Desde cero")));
        profile.addView(body("Objetivo: " + prefs.getString(KEY_GOAL, "Hablar inglés")));
        profile.addView(body("Meta diaria: " + prefs.getInt(KEY_DAILY_MINUTES, 10) + " minutos"));
        box.addView(profile);

        LinearLayout stats = card();
        stats.addView(label("ESTADÍSTICAS", GREEN));
        stats.addView(heading("⭐ " + prefs.getInt(KEY_XP, 0) + " XP", 21, BLUE_DARK));
        stats.addView(body("🔥 Racha: " + prefs.getInt(KEY_STREAK, 0) + " días"));
        stats.addView(body("✓ Lecciones completadas: " + completedSet().size()));
        stats.addView(body("♥ Favoritos: " + favoriteSet().size()));
        box.addView(stats);

        LinearLayout reminder = card();
        reminder.addView(label("RECORDATORIO", ORANGE));
        boolean enabled = prefs.getBoolean(KEY_REMINDER, true);
        reminder.addView(heading("Práctica diaria", 20, BLUE_DARK));
        reminder.addView(body("Recordatorio aproximado a las 19:00 para mantener la racha."));
        reminder.addView(secondaryButton(enabled ? "🔔 Activado" : "🔕 Activar", this::toggleReminder));
        box.addView(reminder);

        LinearLayout account = card();
        account.addView(label("PREFERENCIAS", PURPLE));
        account.addView(secondaryButton("♥ Ver favoritos", this::showFavorites));
        account.addView(secondaryButton("🔎 Todas las lecciones", this::showAllLessons));
        account.addView(secondaryButton("↻ Configurar mi nivel otra vez", this::resetOnboarding));
        box.addView(account);
        setContent(scroll);
    }

    private void resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDED, false).apply();
        shellBuilt = false;
        showOnboarding();
    }

    private void showAllLessons() {
        currentSection = "lessons";
        LinearLayout root = verticalBox();
        root.setPadding(dp(14), dp(14), dp(14), dp(18));

        EditText search = new EditText(this);
        search.setHint("Buscar: verbos, familia, colores…");
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        search.setPadding(dp(14), dp(10), dp(14), dp(10));
        search.setBackground(rounded(Color.WHITE, BORDER, 16));
        root.addView(search, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = verticalBox();
        list.setPadding(0, dp(10), 0, dp(28));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        renderLessonList(list, getOrderedPosts());
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = normalize(s.toString());
                List<PostItem> filtered = new ArrayList<>();
                for (PostItem p : getOrderedPosts()) {
                    String hay = normalize(p.title + " " + p.summary + " " + joinLabels(p.labels, " "));
                    if (q.isEmpty() || hay.contains(q)) filtered.add(p);
                }
                renderLessonList(list, filtered);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        setContent(root);
    }

    private void renderLessonList(LinearLayout list, List<PostItem> source) {
        list.removeAllViews();
        if (source.isEmpty()) {
            list.addView(body("No encontré lecciones con ese término."));
            return;
        }
        for (PostItem p : source) list.addView(postCard(p));
    }

    private void showFavorites() {
        currentSection = "favorites";
        Set<String> fav = favoriteSet();
        List<PostItem> filtered = new ArrayList<>();
        for (PostItem p : getOrderedPosts()) if (fav.contains(p.url)) filtered.add(p);

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        box.addView(heading("Tus favoritos", 26, BLUE_DARK));
        box.addView(body("Guarda las lecciones que quieras encontrar rápido después."));
        if (filtered.isEmpty()) box.addView(cardMessage("Sin favoritos", "Toca ♡ en una lección para guardarla aquí."));
        for (PostItem p : filtered) box.addView(postCard(p));
        scroll.addView(box);
        setContent(scroll);
    }

    private LinearLayout postCard(PostItem p) {
        LinearLayout c = card();
        LinearLayout top = horizontal();
        TextView title = heading(p.title, 18, BLUE_DARK);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button fav = smallButton(favoriteSet().contains(p.url) ? "♥" : "♡", () -> toggleFavorite(p));
        top.addView(fav, new LinearLayout.LayoutParams(dp(48), dp(44)));
        c.addView(top);
        String s = stripHtml(p.summary, 150);
        if (!s.isEmpty()) c.addView(body(s));
        LinearLayout actions = horizontal();
        actions.addView(primaryButton("Abrir", () -> openPost(p)), new LinearLayout.LayoutParams(0, dp(46), 1f));
        TextView status = new TextView(this);
        boolean done = completedSet().contains(p.url);
        status.setText(done ? "✓ Hecha" : "+50 XP al completar");
        status.setTextColor(done ? GREEN_DARK : MUTED);
        status.setTextSize(12);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        stLp.setMargins(dp(8), 0, 0, 0);
        actions.addView(status, stLp);
        c.addView(actions);
        return c;
    }

    private void openPost(PostItem p) {
        prefs.edit().putString(KEY_LAST_OPENED, p.url).apply();
        showReader(p);
    }

    private void openUrl(String url) {
        PostItem p = new PostItem();
        p.title = "Aprende gratis inglés";
        p.url = url;
        p.summary = "";
        showReader(p);
    }

    private void showReader(PostItem p) {
        currentSection = "reader";
        LinearLayout root = verticalBox();

        LinearLayout toolbar = horizontal();
        toolbar.setPadding(dp(8), dp(6), dp(8), dp(6));
        toolbar.setBackgroundColor(Color.WHITE);
        toolbar.addView(smallButton("← Ruta", this::showPath));
        TextView t = heading(p.title, 15, BLUE_DARK);
        t.setSingleLine(true);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        tLp.setMargins(dp(8), 0, dp(8), 0);
        toolbar.addView(t, tLp);
        toolbar.addView(smallButton(favoriteSet().contains(p.url) ? "♥" : "♡", () -> toggleFavorite(p)));
        root.addView(toolbar);

        WebView w = new WebView(this);
        configureWebView(w);
        root.addView(w, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout actions = horizontal();
        actions.setPadding(dp(8), dp(6), dp(8), dp(8));
        actions.setBackgroundColor(Color.WHITE);
        actions.addView(secondaryButton("🗣 Pronunciación", () -> w.loadUrl(HOME + "p/pronunciacion-facil.html")), new LinearLayout.LayoutParams(0, dp(48), 1f));

        boolean trackable = p.url != null && !p.url.contains("/p/");
        boolean completed = trackable && completedSet().contains(p.url);
        Button done;
        if (trackable) {
            done = primaryButton(completed ? "✓ Completada" : "Completar +50 XP", () -> completeLesson(p));
            if (completed) done.setEnabled(false);
        } else {
            done = primaryButton("Volver a practicar", this::showPracticeHub);
        }
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        doneLp.setMargins(dp(8), 0, 0, 0);
        actions.addView(done, doneLp);
        root.addView(actions);

        setContent(root);

        if (isNetworkLikelyAvailable()) {
            w.loadUrl(p.url);
        } else if (p.contentHtml != null && !p.contentHtml.isEmpty()) {
            String html = "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'><style>body{font-family:Arial;padding:18px;line-height:1.65;color:#17324d}img{max-width:100%;height:auto}a{color:#0D73D9}h1,h2,h3{color:#073F8D}</style></head><body>" + p.contentHtml + "</body></html>";
            w.loadDataWithBaseURL(HOME, html, "text/html", "UTF-8", null);
            Toast.makeText(this, "Modo sin conexión: mostrando contenido guardado", Toast.LENGTH_SHORT).show();
        } else {
            w.loadUrl(p.url);
        }
    }

    private void completeLesson(PostItem p) {
        if (p.url == null || p.url.isEmpty()) return;
        Set<String> set = completedSet();
        if (!set.contains(p.url)) {
            set.add(p.url);
            prefs.edit().putStringSet(KEY_COMPLETED, set).apply();
            awardXp(50);
            recordStudyActivity();
            Toast.makeText(this, "Lección completada · +50 XP", Toast.LENGTH_LONG).show();
        }
        showPath();
    }

    private void configureWebView(WebView w) {
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        s.setUserAgentString(s.getUserAgentString() + " AprendeGratisInglesApp/3.0");
        w.addJavascriptInterface(new NativeTtsBridge(w), "AndroidTTS");
        w.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                if (topProgress == null) return;
                topProgress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                topProgress.setProgress(newProgress);
            }
        });
        w.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectNativeSpeechSynthesis(view);
            }
        });
        w.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
            catch (Exception e) { Toast.makeText(this, "No se pudo abrir la descarga", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void injectNativeSpeechSynthesis(WebView w) {
        String js = "(function(){if(!window.AndroidTTS){return;}var pending={};var seq=0;"
                + "window.SpeechSynthesisUtterance=function(text){this.text=text||'';this.lang='en-US';this.rate=1;this.voice=null;this.onend=null;this.onerror=null;};"
                + "window.__agiTtsDone=function(id){var u=pending[id];if(!u)return;delete pending[id];if(typeof u.onend==='function'){try{u.onend({utterance:u});}catch(e){}}};"
                + "window.speechSynthesis={getVoices:function(){return [{name:'Android English',lang:'en-US',default:true}];},cancel:function(){pending={};AndroidTTS.stop();},"
                + "speak:function(u){if(!u)return;var id='agi_'+(++seq);pending[id]=u;AndroidTTS.speak(String(u.text||''),Number(u.rate||0.88),String(u.lang||'en-US'),id);},pause:function(){},resume:function(){}};})();";
        w.evaluateJavascript(js, null);
    }

    private class NativeTtsBridge {
        private final WebView target;
        NativeTtsBridge(WebView target) { this.target = target; }

        @JavascriptInterface public void speak(String text, double rate, String lang, String utteranceId) {
            runOnUiThread(() -> {
                if (!ttsReady || textToSpeech == null) {
                    Toast.makeText(MainActivity.this, "El audio todavía se está preparando", Toast.LENGTH_SHORT).show();
                    return;
                }
                Locale locale = (lang != null && lang.toLowerCase(Locale.ROOT).startsWith("en-gb")) ? Locale.UK : Locale.US;
                textToSpeech.setLanguage(locale);
                textToSpeech.setSpeechRate((float) Math.max(0.3, Math.min(1.5, rate)));
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                pendingTtsWebView = target;
            });
        }

        @JavascriptInterface public void stop() {
            runOnUiThread(() -> { if (textToSpeech != null) textToSpeech.stop(); });
        }
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
                textToSpeech.setSpeechRate(0.88f);
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String utteranceId) {}
                    @Override public void onDone(String utteranceId) { notifyTtsDone(utteranceId); }
                    @Override public void onError(String utteranceId) { notifyTtsDone(utteranceId); }
                });
            }
        });
    }

    private void notifyTtsDone(String id) {
        WebView target = pendingTtsWebView;
        if (target == null || id == null) return;
        runOnUiThread(() -> target.evaluateJavascript(
                "window.__agiTtsDone && window.__agiTtsDone('" + id.replace("'", "") + "');", null));
    }

    private void loadFeed(boolean forceToast) {
        if (topProgress != null) {
            topProgress.setVisibility(View.VISIBLE);
            topProgress.setIndeterminate(true);
        }
        executor.execute(() -> {
            String json = fetchText(FEED);
            boolean fresh = json != null && !json.trim().isEmpty();
            if (!fresh) json = prefs.getString(KEY_FEED_CACHE, "");
            final String result = json;
            final boolean fromNetwork = fresh;
            runOnUiThread(() -> {
                if (topProgress != null) {
                    topProgress.setIndeterminate(false);
                    topProgress.setVisibility(View.GONE);
                }
                if (!result.isEmpty() && parseFeed(result)) {
                    if (fromNetwork) prefs.edit().putString(KEY_FEED_CACHE, result).apply();
                    if (forceToast) Toast.makeText(this, fromNetwork ? "Lecciones actualizadas" : "Usando contenido guardado", Toast.LENGTH_SHORT).show();
                    refreshCurrentSection();
                } else if (forceToast) {
                    Toast.makeText(this, "No se pudieron cargar las lecciones", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean parseFeed(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject feed = root.getJSONObject("feed");
            JSONArray entries = feed.optJSONArray("entry");
            posts.clear();
            if (entries != null) {
                for (int i = 0; i < entries.length(); i++) {
                    PostItem p = PostItem.fromJson(entries.getJSONObject(i));
                    if (p.url != null && !p.url.isEmpty()) posts.add(p);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String fetchText(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "AprendeGratisInglesApp/3.0");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private List<PostItem> getOrderedPosts() {
        List<PostItem> ordered = new ArrayList<>(posts);
        ordered.sort(Comparator.comparingInt(this::courseRank));
        return ordered;
    }

    private int courseRank(PostItem p) {
        String t = normalize(p.title);
        for (int i = 0; i < COURSE_ORDER.length; i++) {
            if (t.contains(COURSE_ORDER[i])) return i;
        }
        return 1000 + posts.indexOf(p);
    }

    private String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private int findCurrentIndex(List<PostItem> ordered, int startIndex) {
        Set<String> done = completedSet();
        for (int i = Math.max(0, startIndex); i < ordered.size(); i++) {
            if (!done.contains(ordered.get(i).url)) return i;
        }
        return ordered.size();
    }

    private String shortLessonTitle(String title) {
        if (title == null) return "Lección";
        String t = title.replace("| Inglés desde cero", "").replace("| Inglés para principiantes", "").trim();
        if (t.length() > 34) t = t.substring(0, 34).trim() + "…";
        return t;
    }

    private boolean isNetworkLikelyAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network n = cm.getActiveNetwork();
            if (n == null) return false;
            android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(n);
            return caps != null && caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    private void refreshCurrentSection() {
        if (!shellBuilt) return;
        if ("path".equals(currentSection)) showPath();
        else if ("practice".equals(currentSection)) showPracticeHub();
        else if ("achievements".equals(currentSection)) showAchievements();
        else if ("profile".equals(currentSection)) showProfile();
        else if ("lessons".equals(currentSection)) showAllLessons();
        else if ("favorites".equals(currentSection)) showFavorites();
    }

    private Set<String> favoriteSet() {
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITES, Collections.emptySet()));
    }

    private Set<String> completedSet() {
        return new HashSet<>(prefs.getStringSet(KEY_COMPLETED, Collections.emptySet()));
    }

    private Set<String> errorSet() {
        return new HashSet<>(prefs.getStringSet(KEY_ERRORS, Collections.emptySet()));
    }

    private void saveErrors(Set<String> set) {
        prefs.edit().putStringSet(KEY_ERRORS, new HashSet<>(set)).apply();
    }

    private void toggleFavorite(PostItem p) {
        if (p.url == null || p.url.isEmpty()) return;
        Set<String> set = favoriteSet();
        if (set.contains(p.url)) set.remove(p.url); else set.add(p.url);
        prefs.edit().putStringSet(KEY_FAVORITES, new HashSet<>(set)).apply();
        Toast.makeText(this, set.contains(p.url) ? "Guardada en favoritos" : "Eliminada de favoritos", Toast.LENGTH_SHORT).show();
        refreshCurrentSection();
    }

    private void ensureDayState() {
        String today = dayKey(0);
        String stored = prefs.getString(KEY_DAY, "");
        if (!today.equals(stored)) {
            prefs.edit()
                    .putString(KEY_DAY, today)
                    .putInt(KEY_TODAY_XP, 0)
                    .putInt(KEY_LIVES, 5)
                    .apply();
        }
    }

    private void awardXp(int amount) {
        ensureDayState();
        int total = prefs.getInt(KEY_XP, 0) + amount;
        int today = prefs.getInt(KEY_TODAY_XP, 0) + amount;
        prefs.edit().putInt(KEY_XP, total).putInt(KEY_TODAY_XP, today).apply();
        updateHeaderStats();
    }

    private void loseHeart() {
        ensureDayState();
        int hearts = Math.max(0, prefs.getInt(KEY_LIVES, 5) - 1);
        prefs.edit().putInt(KEY_LIVES, hearts).apply();
        updateHeaderStats();
        if (hearts == 0) Toast.makeText(this, "Sin corazones por hoy. Puedes seguir repasando sin perder más.", Toast.LENGTH_LONG).show();
    }

    private void recordStudyActivity() {
        String today = dayKey(0);
        String yesterday = dayKey(-1);
        String last = prefs.getString(KEY_LAST_STUDY_DAY, "");
        int streak = prefs.getInt(KEY_STREAK, 0);
        if (today.equals(last)) return;
        if (yesterday.equals(last)) streak++;
        else streak = 1;
        prefs.edit().putString(KEY_LAST_STUDY_DAY, today).putInt(KEY_STREAK, streak).apply();
        updateHeaderStats();
    }

    private String dayKey(int offsetDays) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, offsetDays);
        return new SimpleDateFormat("yyyy-DDD", Locale.US).format(c.getTime());
    }

    private int dailyXpGoal() {
        int mins = prefs.getInt(KEY_DAILY_MINUTES, 10);
        return Math.max(50, mins * 10);
    }

    private void updateHeaderStats() {
        if (streakChip == null) return;
        ensureDayState();
        streakChip.setText("🔥 " + prefs.getInt(KEY_STREAK, 0));
        xpChip.setText("⭐ " + prefs.getInt(KEY_XP, 0));
        livesChip.setText("❤️ " + prefs.getInt(KEY_LIVES, 5));
    }

    private void toggleReminder() {
        boolean enabled = !prefs.getBoolean(KEY_REMINDER, true);
        prefs.edit().putBoolean(KEY_REMINDER, enabled).apply();
        if (enabled) scheduleDailyReminder(); else cancelReminder();
        showProfile();
    }

    private void scheduleDailyReminder() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 7001, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 19);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1);
        if (am != null) am.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    private void cancelReminder() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 7001, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.cancel(pi);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 500);
        }
    }

    private View cardMessage(String title, String text) {
        LinearLayout c = card();
        c.addView(heading(title, 20, BLUE_DARK));
        c.addView(body(text));
        return c;
    }

    private void setContent(View view) {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        contentHost.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LinearLayout verticalBox() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private LinearLayout card() {
        LinearLayout c = verticalBox();
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(rounded(Color.WHITE, BORDER, 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(8));
        c.setLayoutParams(lp);
        c.setElevation(dp(2));
        return c;
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radius));
        if (Color.alpha(stroke) > 0) g.setStroke(dp(1), stroke);
        return g;
    }

    private TextView heading(String text, int size, int color) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setPadding(0, dp(3), 0, dp(6));
        return v;
    }

    private TextView body(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(15);
        v.setTextColor(MUTED);
        v.setLineSpacing(0, 1.15f);
        v.setPadding(0, dp(2), 0, dp(8));
        return v;
    }

    private TextView label(String text, int color) {
        TextView v = new TextView(this);
        v.setText(text.toUpperCase(Locale.ROOT));
        v.setTextSize(11);
        v.setTextColor(color);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setPadding(0, dp(2), 0, dp(6));
        return v;
    }

    private Button primaryButton(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(rounded(BLUE, BLUE, 14));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private Button secondaryButton(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(BLUE_DARK);
        b.setTextSize(14);
        b.setBackground(rounded(Color.WHITE, BORDER, 14));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private Button smallButton(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(BLUE_DARK);
        b.setTextSize(12);
        b.setBackground(rounded(Color.WHITE, BORDER, 11));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private View spacer(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    private View spacerW(int w) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(w), 1));
        return v;
    }

    private String stripHtml(String html, int max) {
        if (html == null || html.isEmpty()) return "";
        String s = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("\\s+", " ").trim();
        if (s.length() > max) s = s.substring(0, max).trim() + "…";
        return s;
    }

    private String joinLabels(List<String> items, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (!shellBuilt) {
            super.onBackPressed();
            return;
        }
        if (!"path".equals(currentSection)) showPath();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
