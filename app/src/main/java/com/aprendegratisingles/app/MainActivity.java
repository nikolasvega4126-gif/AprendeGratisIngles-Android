package com.aprendegratisingles.app;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static final String KEY_EXAMS = "unit_exams_passed";
    private static final String KEY_TOTAL_ANSWERED = "total_answered";
    private static final String KEY_TOTAL_CORRECT = "total_correct";
    private static final String KEY_STUDY_MINUTES = "study_minutes";
    private static final String KEY_STUDY_SESSIONS = "study_sessions";
    private static final String KEY_MASTERED = "mastered_exercises";
    private static final String KEY_LEVEL_TEST_SCORE = "level_test_score";
    private static final String WEAK_PREFIX = "weak_";
    private static final String KEY_STUDY_DATES = "study_dates";
    private static final String KEY_PRON_ATTEMPTS = "pron_attempts";
    private static final String KEY_PRON_GOOD = "pron_good";
    private static final String VOCAB_STAGE_PREFIX = "vocab_stage_";
    private static final String VOCAB_DUE_PREFIX = "vocab_due_";
    private static final int REQ_EXPORT = 7201;
    private static final int REQ_IMPORT = 7202;

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

    private static final String[] UNIT_NAMES = {
            "Primeros pasos", "Personas y acciones", "Gramática esencial",
            "Tiempo y fechas", "Mundo cotidiano", "Presente simple y rutina"
    };

    private static final int[] UNIT_START = {0, 3, 7, 11, 14, 18};
    private static final int[] UNIT_END = {2, 6, 10, 13, 17, 19};

    private static final String[][] VOCABULARY = {
            {"Hello", "Hola"}, {"Good morning", "Buenos días"}, {"Goodbye", "Adiós"},
            {"Please", "Por favor"}, {"Thank you", "Gracias"}, {"Mother", "Madre"},
            {"Father", "Padre"}, {"Brother", "Hermano"}, {"Sister", "Hermana"},
            {"House", "Casa"}, {"Water", "Agua"}, {"Food", "Comida"},
            {"Work", "Trabajo"}, {"Friend", "Amigo"}, {"Family", "Familia"},
            {"Blue", "Azul"}, {"Head", "Cabeza"}, {"Monday", "Lunes"},
            {"Today", "Hoy"}, {"Tomorrow", "Mañana"}, {"Eat", "Comer"},
            {"Drink", "Beber"}, {"Study", "Estudiar"}, {"Speak", "Hablar"}
    };

    private static final String[][] PRONUNCIATION_PHRASES = {
            {"Hello, how are you?", "Hola, ¿cómo estás?"},
            {"Good morning", "Buenos días"},
            {"My name is Carlos", "Mi nombre es Carlos"},
            {"I am learning English", "Estoy aprendiendo inglés"},
            {"Where is the bathroom?", "¿Dónde está el baño?"},
            {"I would like some water, please", "Quisiera un poco de agua, por favor"},
            {"Can you help me?", "¿Puedes ayudarme?"},
            {"I go to work every day", "Voy al trabajo todos los días"}
    };

    private static final String[] CONVERSATION_NAMES = {"Restaurante", "Aeropuerto", "Trabajo"};

    private static final String[][][] CONVERSATIONS = {
            {
                    {"Waiter: Hello! What would you like to drink?", "I'd like water, please.", "I am water.", "Yesterday water.", "I'd like water, please."},
                    {"Waiter: Are you ready to order?", "Yes, I'd like the chicken.", "Chicken is blue.", "I ordering yesterday.", "Yes, I'd like the chicken."},
                    {"Waiter: Anything else?", "No, thank you.", "Else no yesterday.", "I am anything.", "No, thank you."},
                    {"Waiter: How was your meal?", "It was delicious, thank you.", "It delicious yesterday is.", "Meal blue.", "It was delicious, thank you."}
            },
            {
                    {"Agent: May I see your passport?", "Yes, here you are.", "Passport yesterday.", "I am passport.", "Yes, here you are."},
                    {"Agent: Where are you flying to?", "I'm flying to London.", "I London yesterday.", "Flying is blue.", "I'm flying to London."},
                    {"Agent: Do you have any bags to check?", "Yes, I have one bag.", "One bag am.", "Check yesterday.", "Yes, I have one bag."},
                    {"Agent: Have a nice flight!", "Thank you!", "Flight is bag.", "I nice yesterday.", "Thank you!"}
            },
            {
                    {"Manager: Tell me about yourself.", "I am responsible and I learn quickly.", "Myself blue.", "I learning yesterday quickly.", "I am responsible and I learn quickly."},
                    {"Manager: Why do you want this job?", "I want to grow and contribute to the team.", "Job wants me yesterday.", "Because blue.", "I want to grow and contribute to the team."},
                    {"Manager: Can you work in a team?", "Yes, I work well with others.", "Team is yesterday.", "I am work team.", "Yes, I work well with others."},
                    {"Manager: When can you start?", "I can start next week.", "Start is blue.", "I started tomorrow yesterday.", "I can start next week."}
            }
    };

    private static final int TYPE_CHOICE = 0;
    private static final int TYPE_ORDER = 1;
    private static final int TYPE_FILL = 2;
    private static final int TYPE_LISTEN_CHOICE = 3;
    private static final int TYPE_WRITE_LISTEN = 4;

    private static class Exercise {
        final int type;
        final int unit;
        final String prompt;
        final String answer;
        final String speak;
        final String[] options;

        Exercise(int type, int unit, String prompt, String answer, String speak, String... options) {
            this.type = type;
            this.unit = unit;
            this.prompt = prompt;
            this.answer = answer;
            this.speak = speak;
            this.options = options;
        }
    }

    private static final Exercise[] EXERCISES = {
            new Exercise(TYPE_CHOICE, 0, "¿Qué significa ‘Hello’?", "Hola", "Hello", "Hola", "Gracias", "Adiós"),
            new Exercise(TYPE_LISTEN_CHOICE, 0, "Escucha y elige la traducción", "Buenos días", "Good morning", "Buenas noches", "Buenos días", "Hasta luego"),
            new Exercise(TYPE_FILL, 0, "Completa: Thank ___", "you", "Thank you"),
            new Exercise(TYPE_ORDER, 0, "Ordena la frase", "My name is Ana", "My name is Ana", "Ana", "is", "My", "name"),
            new Exercise(TYPE_WRITE_LISTEN, 0, "Escribe lo que escuchas", "Goodbye", "Goodbye"),

            new Exercise(TYPE_CHOICE, 1, "¿Qué significa ‘mother’?", "Madre", "mother", "Hermana", "Madre", "Hija"),
            new Exercise(TYPE_FILL, 1, "Completa: She is my ___", "sister", "She is my sister"),
            new Exercise(TYPE_ORDER, 1, "Ordena la frase", "I drink water", "I drink water", "water", "I", "drink"),
            new Exercise(TYPE_LISTEN_CHOICE, 1, "Escucha y elige", "Hermano", "Brother", "Padre", "Hermano", "Primo"),
            new Exercise(TYPE_WRITE_LISTEN, 1, "Escribe lo que escuchas", "They are friends", "They are friends"),

            new Exercise(TYPE_CHOICE, 2, "Elige la forma correcta: I ___ happy", "am", "I am happy", "is", "am", "are"),
            new Exercise(TYPE_FILL, 2, "Completa: ___ apple", "an", "an apple"),
            new Exercise(TYPE_ORDER, 2, "Ordena la pregunta", "Where are you", "Where are you", "you", "Where", "are"),
            new Exercise(TYPE_LISTEN_CHOICE, 2, "Escucha y elige la traducción", "Esta es mi casa", "This is my house", "Esta es mi casa", "Esa es tu casa", "Mi casa es grande"),
            new Exercise(TYPE_WRITE_LISTEN, 2, "Escribe lo que escuchas", "He is my friend", "He is my friend"),

            new Exercise(TYPE_CHOICE, 3, "¿Qué día viene después de Monday?", "Tuesday", "Tuesday", "Sunday", "Tuesday", "Friday"),
            new Exercise(TYPE_FILL, 3, "Completa: January, February, ___", "March", "March"),
            new Exercise(TYPE_ORDER, 3, "Ordena la frase", "It is three o'clock", "It is three o'clock", "three", "It", "o'clock", "is"),
            new Exercise(TYPE_LISTEN_CHOICE, 3, "Escucha y elige", "Viernes", "Friday", "Viernes", "Martes", "Jueves"),
            new Exercise(TYPE_WRITE_LISTEN, 3, "Escribe lo que escuchas", "Today is Monday", "Today is Monday"),

            new Exercise(TYPE_CHOICE, 4, "¿Qué significa ‘blue’?", "Azul", "blue", "Rojo", "Azul", "Verde"),
            new Exercise(TYPE_FILL, 4, "Completa: I eat ___", "bread", "I eat bread"),
            new Exercise(TYPE_ORDER, 4, "Ordena la frase", "The kitchen is small", "The kitchen is small", "small", "The", "is", "kitchen"),
            new Exercise(TYPE_LISTEN_CHOICE, 4, "Escucha y elige", "Cabeza", "Head", "Brazo", "Cabeza", "Pierna"),
            new Exercise(TYPE_WRITE_LISTEN, 4, "Escribe lo que escuchas", "I drink coffee", "I drink coffee"),

            new Exercise(TYPE_CHOICE, 5, "Completa: She ___ English every day", "studies", "She studies English every day", "study", "studies", "studying"),
            new Exercise(TYPE_FILL, 5, "Completa: I ___ up at seven", "wake", "I wake up at seven"),
            new Exercise(TYPE_ORDER, 5, "Ordena la frase", "I go to work", "I go to work", "work", "to", "I", "go"),
            new Exercise(TYPE_LISTEN_CHOICE, 5, "Escucha y elige la traducción", "Él desayuna por la mañana", "He eats breakfast in the morning", "Él desayuna por la mañana", "Él trabaja de noche", "Él duerme por la tarde"),
            new Exercise(TYPE_WRITE_LISTEN, 5, "Escribe lo que escuchas", "We study English", "We study English")
    };

    private static final String[][] LEVEL_TEST = {
            {"¿Qué significa ‘Good night’?", "Buenos días", "Buenas noches", "Gracias", "Buenas noches"},
            {"Elige el pronombre para ‘nosotros’", "They", "We", "He", "We"},
            {"Completa: She ___ a teacher", "am", "is", "are", "is"},
            {"¿Cuál es correcto?", "a apple", "an apple", "an house", "an apple"},
            {"¿Qué significa ‘Where do you live?’", "¿Dónde vives?", "¿Cómo te llamas?", "¿Qué haces?", "¿Dónde vives?"},
            {"Completa: I ___ coffee every morning", "drink", "drinks", "drinking", "drink"},
            {"Elige la oración correcta", "He work every day", "He works every day", "He working every day", "He works every day"},
            {"¿Qué significa ‘I have already eaten’?", "Ya he comido", "Estoy comiendo", "Comeré después", "Ya he comido"},
            {"Elige la opción más natural", "I am agree", "I agree", "I agreeing", "I agree"},
            {"¿Qué significa ‘If I had time, I would travel’?", "Si tengo tiempo, viajo", "Si tuviera tiempo, viajaría", "Cuando tenga tiempo, viajaré", "Si tuviera tiempo, viajaría"}
    };

    private static final String[][][] UNIT_EXAMS = {
            {
                    {"¿Qué significa ‘Hello’?", "Hola", "Gracias", "Adiós", "Hola"},
                    {"¿Qué significa ‘Good morning’?", "Buenas tardes", "Buenos días", "Buenas noches", "Buenos días"},
                    {"Completa: Thank ___", "me", "you", "we", "you"},
                    {"¿Cuál es un número en inglés?", "Blue", "Seven", "Mother", "Seven"},
                    {"¿Qué significa ‘Goodbye’?", "Hasta luego/Adiós", "Hola", "Por favor", "Hasta luego/Adiós"}
            },
            {
                    {"¿Qué significa ‘father’?", "Padre", "Hermano", "Tío", "Padre"},
                    {"Pronombre para ‘ella’", "He", "She", "We", "She"},
                    {"¿Qué significa ‘drink’?", "Beber", "Dormir", "Correr", "Beber"},
                    {"Completa: ___ are friends", "They", "He", "She", "They"},
                    {"¿Qué significa ‘family’?", "Familia", "Trabajo", "Casa", "Familia"}
            },
            {
                    {"Completa: I ___ happy", "is", "am", "are", "am"},
                    {"Artículo correcto: ___ orange", "a", "an", "the siempre", "an"},
                    {"Posesivo de ‘yo’", "my", "his", "their", "my"},
                    {"¿Qué palabra pregunta ‘dónde’?", "When", "Where", "Who", "Where"},
                    {"Completa: He ___ my brother", "am", "is", "are", "is"}
            },
            {
                    {"Después de Monday viene…", "Friday", "Tuesday", "Sunday", "Tuesday"},
                    {"Mes después de March", "April", "June", "January", "April"},
                    {"¿Qué significa ‘What time is it?’", "¿Qué día es?", "¿Qué hora es?", "¿Dónde estás?", "¿Qué hora es?"},
                    {"‘Friday’ significa…", "Viernes", "Martes", "Sábado", "Viernes"},
                    {"‘January’ significa…", "Enero", "Junio", "Julio", "Enero"}
            },
            {
                    {"‘Blue’ significa…", "Azul", "Rojo", "Negro", "Azul"},
                    {"‘Head’ significa…", "Mano", "Cabeza", "Pie", "Cabeza"},
                    {"‘Bread’ significa…", "Pan", "Agua", "Leche", "Pan"},
                    {"‘Kitchen’ significa…", "Baño", "Cocina", "Dormitorio", "Cocina"},
                    {"Completa: I drink ___", "water", "house", "blue", "water"}
            },
            {
                    {"Completa: She ___ English", "study", "studies", "studying", "studies"},
                    {"Completa: I ___ up at seven", "wake", "wakes", "waking", "wake"},
                    {"‘Every day’ significa…", "Cada día", "Ayer", "Nunca", "Cada día"},
                    {"Completa: He ___ breakfast", "eat", "eats", "eating", "eats"},
                    {"‘I go to work’ significa…", "Voy al trabajo", "Estoy en casa", "Voy a dormir", "Voy al trabajo"}
            }
    };

    private static final String[][] EXAM_TYPED = {
            {"Escribe en inglés: Buenos días", "Good morning"},
            {"Escribe en inglés: Ella es mi hermana", "She is my sister"},
            {"Completa escribiendo: I ___ happy", "am"},
            {"Escribe en inglés: martes", "Tuesday"},
            {"Escribe en inglés: agua", "water"},
            {"Completa escribiendo: She ___ English every day", "studies"}
    };

    private static final String[][] EXAM_SPEECH = {
            {"Pronuncia esta frase", "Good morning"},
            {"Pronuncia esta frase", "My family is important"},
            {"Pronuncia esta frase", "He is my brother"},
            {"Pronuncia esta frase", "What time is it?"},
            {"Pronuncia esta frase", "The kitchen is small"},
            {"Pronuncia esta frase", "I go to work every day"}
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
    private LinearLayout bottomNav;
    private LinearLayout navPathBtn;
    private LinearLayout navPracticeBtn;
    private LinearLayout navAchievementsBtn;
    private LinearLayout navProfileBtn;
    private boolean shellBuilt = false;
    private int pronunciationIndex = 0;
    private String pendingSpeechExpected = "";
    private TextView pronunciationResult;
    private SpeechRecognizer speechRecognizer;
    private TextView speechStatusView;
    private boolean speechAwardStandaloneXp = true;
    private SpeechEvaluationCallback pendingSpeechCallback;

    private interface SpeechEvaluationCallback {
        void onEvaluated(int score, String heard);
    }

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

        boolean needsTest = "Ya sé un poco".equals(selectedStartMode);
        prefs.edit()
                .putBoolean(KEY_ONBOARDED, !needsTest)
                .putString(KEY_START_MODE, selectedStartMode)
                .putString(KEY_LEVEL, selectedLevel)
                .putString(KEY_GOAL, selectedGoal)
                .putInt(KEY_DAILY_MINUTES, selectedMinutes)
                .putInt(KEY_START_INDEX, startIndex)
                .putBoolean(KEY_REMINDER, true)
                .apply();

        if (needsTest) {
            showLevelTest(0, 0);
        } else {
            launchApp();
            Toast.makeText(this, "Tu ruta personalizada está lista", Toast.LENGTH_LONG).show();
            if ("Quiero practicar".equals(selectedStartMode)) showPracticeHub();
        }
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

        bottomNav = horizontal();
        bottomNav.setPadding(dp(8), dp(8), dp(8), dp(10));
        bottomNav.setBackground(rounded(Color.argb(250, 255, 255, 255), Color.rgb(224, 233, 242), 24));
        bottomNav.setElevation(dp(10));

        navPathBtn = createBottomNavItem("🗺️", "Ruta", this::showPath);
        navPracticeBtn = createBottomNavItem("🧠", "Practicar", this::showPracticeHub);
        navAchievementsBtn = createBottomNavItem("🏆", "Logros", this::showAchievements);
        navProfileBtn = createBottomNavItem("👤", "Perfil", this::showProfile);

        bottomNav.addView(navPathBtn, weightedBottomNavParams(false));
        bottomNav.addView(navPracticeBtn, weightedBottomNavParams(true));
        bottomNav.addView(navAchievementsBtn, weightedBottomNavParams(true));
        bottomNav.addView(navProfileBtn, weightedBottomNavParams(true));

        LinearLayout.LayoutParams navLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navLp.setMargins(dp(10), dp(2), dp(10), dp(10));
        root.addView(bottomNav, navLp);

        setContentView(root);
        refreshBottomNav();
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

    private LinearLayout createBottomNavItem(String icon, String text, Runnable action) {
        LinearLayout item = verticalBox();
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(8), dp(8), dp(8), dp(8));
        item.setClickable(true);
        item.setFocusable(true);

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(20);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTypeface(Typeface.DEFAULT_BOLD);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(11);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        item.addView(iconView);
        item.addView(textView);
        item.setTag(new TextView[]{iconView, textView});
        item.setOnClickListener(v -> {
            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
                action.run();
            }).start();
        });
        return item;
    }

    private LinearLayout.LayoutParams weightedBottomNavParams(boolean withStartMargin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        if (withStartMargin) lp.setMargins(dp(8), 0, 0, 0);
        return lp;
    }

    private void refreshBottomNav() {
        if (bottomNav == null) return;
        styleBottomNavItem(navPathBtn, currentSection.equals("path") || currentSection.equals("reader") || currentSection.equals("lessons") || currentSection.equals("favorites"), BLUE);
        styleBottomNavItem(navPracticeBtn, currentSection.equals("practice"), GREEN);
        styleBottomNavItem(navAchievementsBtn, currentSection.equals("achievements"), ORANGE);
        styleBottomNavItem(navProfileBtn, currentSection.equals("profile"), PURPLE);
    }

    private void styleBottomNavItem(LinearLayout item, boolean active, int accent) {
        if (item == null) return;
        Object tag = item.getTag();
        if (!(tag instanceof TextView[])) return;
        TextView[] parts = (TextView[]) tag;
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        if (active) {
            bg.setColors(new int[]{accent, BLUE_DARK});
            bg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            bg.setStroke(dp(1), Color.argb(55, 255, 255, 255));
            item.setElevation(dp(8));
            parts[0].setTextColor(Color.WHITE);
            parts[1].setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.rgb(245, 249, 252));
            bg.setStroke(dp(1), Color.rgb(227, 235, 243));
            item.setElevation(dp(1));
            parts[0].setTextColor(accent);
            parts[1].setTextColor(BLUE_DARK);
        }
        item.setBackground(bg);
        item.animate().scaleX(active ? 1.04f : 1f).scaleY(active ? 1.04f : 1f).setDuration(180).start();
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
        welcome.addView(heading("Aprende paso a paso", 24, BLUE_DARK));
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
        box.addView(spacer(8));

        List<PostItem> ordered = getOrderedPosts();
        if (ordered.isEmpty()) {
            box.addView(cardMessage("Cargando tu ruta…", "Estamos sincronizando las lecciones publicadas en tu web."));
            setContent(scroll);
            return;
        }

        int startIndex = Math.min(prefs.getInt(KEY_START_INDEX, 0), Math.max(0, ordered.size() - 1));
        int current = findCurrentPathIndex(ordered, startIndex);
        Set<String> done = completedSet();

        for (int unit = 0; unit < UNIT_NAMES.length; unit++) {
            boolean unitUnlocked = isUnitUnlocked(unit, startIndex);
            boolean placementPassed = UNIT_END[unit] < startIndex;

            LinearLayout banner = card();
            banner.setBackground(rounded(unitUnlocked ? Color.rgb(240, 248, 255) : Color.rgb(244, 246, 248), BORDER, 18));
            banner.addView(label("UNIDAD " + (unit + 1), unitUnlocked ? BLUE : MUTED));
            banner.addView(heading((unitUnlocked ? "" : "🔒 ") + UNIT_NAMES[unit], 21, unitUnlocked ? BLUE_DARK : MUTED));
            banner.addView(body(unitDescription(unit)));
            box.addView(banner);

            int from = UNIT_START[unit];
            int to = Math.min(UNIT_END[unit], ordered.size() - 1);
            for (int i = from; i <= to; i++) {
                PostItem post = ordered.get(i);
                boolean skippedByPlacement = i < startIndex;
                boolean completed = done.contains(post.url);
                boolean isCurrent = unitUnlocked && i == current;
                boolean locked = !unitUnlocked || (!completed && !skippedByPlacement && i != current);
                box.addView(pathNode(post, i, completed, skippedByPlacement, isCurrent, locked));
                if (i < to) box.addView(pathConnector(i, current, completed || skippedByPlacement));
            }

            box.addView(unitExamCard(unit, ordered, startIndex, done, placementPassed));
            box.addView(spacer(12));
        }

        if (done.size() >= ordered.size() && allRequiredExamsPassed(startIndex)) {
            box.addView(cardMessage("🏆 Ruta completada", "Terminaste las lecciones y los exámenes disponibles. Las nuevas publicaciones se añadirán automáticamente."));
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
        node.setTextSize(current ? 25 : 20);
        node.setTextColor(Color.WHITE);
        node.setAllCaps(false);
        node.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        int fill = completed ? GREEN : placementPassed ? PURPLE : locked ? Color.rgb(184, 196, 207) : BLUE;
        circle.setColor(fill);
        circle.setStroke(dp(current ? 6 : 4), current ? Color.rgb(173, 220, 255) : Color.argb(0, 0, 0, 0));
        node.setBackground(circle);
        node.setElevation(current ? dp(10) : dp(3));
        LinearLayout.LayoutParams nodeLp = new LinearLayout.LayoutParams(current ? dp(86) : dp(72), current ? dp(86) : dp(72));
        nodeWrap.addView(node, nodeLp);

        if (current) animatePlayNode(node);

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
            TextView start = label("JUGAR · SIGUIENTE", BLUE);
            start.setGravity(Gravity.CENTER);
            nodeWrap.addView(start);
        } else if (completed || placementPassed) {
            TextView doneLabel = label("COMPLETADA", GREEN_DARK);
            doneLabel.setGravity(Gravity.CENTER);
            nodeWrap.addView(doneLabel);
        }

        node.setOnClickListener(v -> {
            if (locked) {
                Toast.makeText(this, "Completa la etapa anterior para desbloquearla", Toast.LENGTH_SHORT).show();
                shakeLockedNode(node);
            } else {
                node.animate().scaleX(0.92f).scaleY(0.92f).setDuration(90).withEndAction(() -> {
                    node.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
                    openPost(p);
                }).start();
            }
        });
        name.setOnClickListener(v -> node.performClick());

        row.addView(nodeWrap, new LinearLayout.LayoutParams(dp(190), ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!right) row.addView(weightSpacer());
        return row;
    }

    private void animatePlayNode(View node) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(node, View.SCALE_X, 1f, 1.09f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(node, View.SCALE_Y, 1f, 1.09f, 1f);
        sx.setDuration(1250);
        sy.setDuration(1250);
        sx.setRepeatCount(ValueAnimator.INFINITE);
        sy.setRepeatCount(ValueAnimator.INFINITE);
        sx.setRepeatMode(ValueAnimator.RESTART);
        sy.setRepeatMode(ValueAnimator.RESTART);
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(sx, sy);
        set.start();
    }

    private void shakeLockedNode(View node) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(node, View.TRANSLATION_X, 0f, -dp(6), dp(6), -dp(4), dp(4), 0f);
        shake.setDuration(360);
        shake.start();
    }

    private View pathConnector(int index, int current, boolean completedBefore) {
        LinearLayout holder = horizontal();
        holder.setGravity(Gravity.CENTER);
        boolean unlockedTrail = completedBefore || (current >= 0 && index < current);
        View line = new View(this);
        GradientDrawable trail = new GradientDrawable();
        trail.setColor(unlockedTrail ? GREEN : Color.rgb(214, 223, 231));
        trail.setCornerRadius(dp(6));
        line.setBackground(trail);
        holder.addView(line, new LinearLayout.LayoutParams(dp(8), dp(38)));

        if (unlockedTrail && index == current - 1) {
            line.setPivotY(0f);
            ObjectAnimator fill = ObjectAnimator.ofFloat(line, View.SCALE_Y, 0.15f, 1f);
            fill.setDuration(600);
            fill.start();
            ObjectAnimator glow = ObjectAnimator.ofFloat(line, View.ALPHA, 0.55f, 1f, 0.55f);
            glow.setDuration(1100);
            glow.setRepeatCount(ValueAnimator.INFINITE);
            glow.setRepeatMode(ValueAnimator.RESTART);
            glow.start();
        }
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
        box.addView(body("Sesiones cortas que mezclan traducción, ordenar frases, completar, escuchar y escribir. La app prioriza tus errores frecuentes."));

        LinearLayout smart = card();
        smart.addView(label("SESIÓN INTELIGENTE", BLUE));
        smart.addView(heading("Práctica personalizada", 21, BLUE_DARK));
        smart.addView(body("10 ejercicios adaptados a tu unidad actual y a los conceptos que más te cuestan."));
        smart.addView(primaryButton("🧠 Empezar sesión", () -> showQuiz(false)));
        box.addView(smart);

        LinearLayout mistakes = card();
        int errors = errorSet().size();
        mistakes.addView(label("REPASO INTELIGENTE", RED));
        mistakes.addView(heading("Repasar mis errores", 21, BLUE_DARK));
        mistakes.addView(body(errors == 0 ? "No tienes errores pendientes." : "Tienes " + errors + " concepto(s) pendientes. Primero aparecerán los que más has fallado."));
        mistakes.addView(secondaryButton(errors == 0 ? "✓ Todo al día" : "↻ Repasar ahora", () -> showQuiz(true)));
        box.addView(mistakes);

        LinearLayout formats = card();
        formats.addView(label("5 FORMATOS", GREEN));
        formats.addView(heading("Aprende haciendo", 21, BLUE_DARK));
        formats.addView(body("✓ Elegir traducción\n✓ Ordenar palabras\n✓ Completar frases\n✓ Escuchar y elegir\n✓ Escribir lo que escuchas"));
        box.addView(formats);

        LinearLayout pronunciation = card();
        pronunciation.addView(label("PRONUNCIACIÓN CON MICRÓFONO", GREEN));
        pronunciation.addView(heading("Escucha, habla y recibe una puntuación", 21, BLUE_DARK));
        pronunciation.addView(body("La app reproduce una frase en inglés, escucha tu voz y compara lo reconocido con la frase esperada."));
        pronunciation.addView(primaryButton("🎙️ Entrenar pronunciación", this::showPronunciationCoach));
        box.addView(pronunciation);

        LinearLayout vocab = card();
        vocab.addView(label("REPETICIÓN ESPACIADA", PURPLE));
        vocab.addView(heading("Mi vocabulario", 21, BLUE_DARK));
        vocab.addView(body("Las palabras vuelven a aparecer en 1, 3, 7, 14 o 30 días según cómo las recuerdes."));
        LinearLayout vocabBtns = horizontal();
        vocabBtns.addView(primaryButton("Repasar ahora", this::showSpacedReview), new LinearLayout.LayoutParams(0, dp(46), 1f));
        LinearLayout.LayoutParams vb = new LinearLayout.LayoutParams(0, dp(46), 1f);
        vb.setMargins(dp(8), 0, 0, 0);
        vocabBtns.addView(secondaryButton("Ver palabras", this::showVocabulary), vb);
        vocab.addView(vocabBtns);
        box.addView(vocab);

        LinearLayout conv = card();
        conv.addView(label("CONVERSACIONES", ORANGE));
        conv.addView(heading("Practica situaciones reales", 21, BLUE_DARK));
        conv.addView(body("Restaurante, aeropuerto y trabajo. Elige respuestas naturales y escucha cada frase."));
        conv.addView(primaryButton("💬 Abrir conversaciones", this::showConversations));
        box.addView(conv);

        setContent(scroll);
    }

    private void showQuiz(boolean reviewOnly) {
        List<Integer> session = buildPracticeSession(reviewOnly);
        if (session.isEmpty()) {
            ScrollView scroll = new ScrollView(this);
            LinearLayout box = verticalBox();
            box.setPadding(dp(14), dp(14), dp(14), dp(28));
            box.addView(cardMessage("✓ Sin errores pendientes", "Tu repaso está al día. Puedes iniciar una sesión inteligente para seguir avanzando."));
            box.addView(primaryButton("Empezar práctica", () -> showQuiz(false)));
            scroll.addView(box);
            setContent(scroll);
            return;
        }
        renderPracticeQuestion(session, 0, 0, reviewOnly, System.currentTimeMillis());
    }

    private void showLevelTest(int position, int score) {
        if (position >= LEVEL_TEST.length) {
            showLevelTestResult(score);
            return;
        }

        LinearLayout root = verticalBox();
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(22), dp(22), dp(22), dp(22));

        TextView progressText = label("PRUEBA DE NIVEL · " + (position + 1) + "/" + LEVEL_TEST.length, BLUE);
        progressText.setGravity(Gravity.CENTER);
        root.addView(progressText);
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(LEVEL_TEST.length);
        progress.setProgress(position + 1);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        root.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)));
        root.addView(spacer(24));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(82), dp(82));
        logoLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logo, logoLp);
        root.addView(spacer(14));

        String[] q = LEVEL_TEST[position];
        TextView h = heading(q[0], 24, BLUE_DARK);
        h.setGravity(Gravity.CENTER);
        root.addView(h);
        root.addView(body("Elige una respuesta. La prueba ajustará automáticamente el punto de inicio de tu ruta."));

        for (int i = 1; i <= 3; i++) {
            final String option = q[i];
            root.addView(optionButton(option, false, () -> showLevelTest(position + 1, score + (option.equals(q[4]) ? 1 : 0))));
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void showLevelTestResult(int score) {
        String level;
        int startIndex;
        if (score >= 8) {
            level = "Avanzado";
            startIndex = 14;
        } else if (score >= 4) {
            level = "Intermedio";
            startIndex = 7;
        } else {
            level = "Básico";
            startIndex = 0;
        }

        prefs.edit()
                .putBoolean(KEY_ONBOARDED, true)
                .putString(KEY_LEVEL, level)
                .putInt(KEY_START_INDEX, startIndex)
                .putInt(KEY_LEVEL_TEST_SCORE, score)
                .apply();

        LinearLayout root = verticalBox();
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(24), dp(28), dp(24), dp(28));
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(110), dp(110));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logo, lp);
        root.addView(spacer(18));
        TextView h = heading("Tu nivel estimado: " + level, 27, BLUE_DARK);
        h.setGravity(Gravity.CENTER);
        root.addView(h);
        TextView sc = heading(score + " / " + LEVEL_TEST.length, 36, GREEN_DARK);
        sc.setGravity(Gravity.CENTER);
        root.addView(sc);
        TextView desc = body(levelTestDescription(level));
        desc.setGravity(Gravity.CENTER);
        root.addView(desc);
        root.addView(spacer(14));
        root.addView(primaryButton("Empezar mi ruta", () -> {
            shellBuilt = false;
            launchApp();
            Toast.makeText(this, "Ruta ajustada según tu prueba", Toast.LENGTH_LONG).show();
        }));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private String levelTestDescription(String level) {
        if ("Avanzado".equals(level)) return "Tienes una base sólida dentro del contenido disponible. Empezarás cerca de las unidades finales y podrás repasar lo anterior cuando quieras.";
        if ("Intermedio".equals(level)) return "Ya manejas conceptos básicos. La app te colocará en gramática esencial y mantendrá disponible el repaso de fundamentos.";
        return "Conviene empezar por los fundamentos para construir una base estable y avanzar sin huecos.";
    }

    private void restartLevelTest() {
        shellBuilt = false;
        showLevelTest(0, 0);
    }

    private String unitDescription(int unit) {
        switch (unit) {
            case 0: return "Saludos, pronunciación TH y números.";
            case 1: return "Familia, frases cotidianas, verbos y pronombres.";
            case 2: return "To Be, artículos, posesivos y preguntas.";
            case 3: return "Días, meses, fechas y hora.";
            case 4: return "Colores, cuerpo, comida y casa.";
            default: return "Presente simple y rutina diaria.";
        }
    }

    private int unitForLesson(int lessonIndex) {
        for (int i = 0; i < UNIT_START.length; i++) {
            if (lessonIndex >= UNIT_START[i] && lessonIndex <= UNIT_END[i]) return i;
        }
        return UNIT_NAMES.length - 1;
    }

    private Set<String> examPassedSet() {
        return new HashSet<>(prefs.getStringSet(KEY_EXAMS, Collections.emptySet()));
    }

    private Set<String> masteredSet() {
        return new HashSet<>(prefs.getStringSet(KEY_MASTERED, Collections.emptySet()));
    }

    private boolean isUnitUnlocked(int unit, int startIndex) {
        if (unit <= 0) return true;
        if (UNIT_END[unit - 1] < startIndex) return true;
        return examPassedSet().contains(String.valueOf(unit - 1));
    }

    private boolean allRequiredExamsPassed(int startIndex) {
        Set<String> passed = examPassedSet();
        for (int unit = 0; unit < UNIT_NAMES.length; unit++) {
            if (UNIT_END[unit] < startIndex) continue;
            if (!passed.contains(String.valueOf(unit))) return false;
        }
        return true;
    }

    private int findCurrentPathIndex(List<PostItem> ordered, int startIndex) {
        Set<String> done = completedSet();
        for (int i = Math.max(0, startIndex); i < ordered.size(); i++) {
            int unit = unitForLesson(i);
            if (!isUnitUnlocked(unit, startIndex)) return -1;
            if (!done.contains(ordered.get(i).url)) return i;
            if (i == Math.min(UNIT_END[unit], ordered.size() - 1) && !examPassedSet().contains(String.valueOf(unit))) return -1;
        }
        return -1;
    }

    private View unitExamCard(int unit, List<PostItem> ordered, int startIndex, Set<String> done, boolean placementPassed) {
        LinearLayout c = card();
        boolean passed = placementPassed || examPassedSet().contains(String.valueOf(unit));
        boolean unlocked = isUnitUnlocked(unit, startIndex);
        boolean ready = unlocked;
        int from = UNIT_START[unit];
        int to = Math.min(UNIT_END[unit], ordered.size() - 1);
        if (from >= ordered.size()) ready = false;
        for (int i = from; i <= to && ready; i++) {
            if (i < startIndex) continue;
            if (!done.contains(ordered.get(i).url)) ready = false;
        }

        c.addView(label("EXAMEN DE UNIDAD", passed ? GREEN : ORANGE));
        if (passed) {
            c.addView(heading(placementPassed ? "✓ Aprobado por prueba de nivel" : "✓ Examen aprobado", 19, GREEN_DARK));
            c.addView(body("La siguiente unidad está desbloqueada."));
        } else if (ready) {
            c.addView(heading("Demuestra lo aprendido", 19, BLUE_DARK));
            c.addView(body("7 retos · 5 de opción, 1 escribiendo y 1 hablando. Necesitas 6 correctas para desbloquear la siguiente unidad."));
            c.addView(primaryButton("🎓 Hacer examen", () -> showUnitExam(unit)));
        } else {
            c.addView(heading("🔒 Examen bloqueado", 19, MUTED));
            c.addView(body("Completa las lecciones de esta unidad para habilitarlo."));
        }
        return c;
    }

    private void showUnitExam(int unit) {
        currentSection = "practice";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        box.addView(label("UNIDAD " + (unit + 1), BLUE));
        box.addView(heading("Examen · " + UNIT_NAMES[unit], 25, BLUE_DARK));
        box.addView(body("7 retos: 5 de opción múltiple, 1 escribiendo y 1 pronunciando. Necesitas 6 de 7 para aprobar."));

        String[][] questions = UNIT_EXAMS[unit];
        final long startedAt = System.currentTimeMillis();
        LinearLayout resultHost = verticalBox();

        class ExamTracker {
            int answered = 0;
            int correct = 0;
            boolean finished = false;

            void record(boolean ok) {
                if (finished) return;
                answered++;
                if (ok) correct++;
                recordGenericAnswer(ok);
                if (answered < 7) return;

                finished = true;
                addStudySession(startedAt);
                recordStudyActivity();
                boolean pass = correct >= 6;
                if (pass) {
                    Set<String> exams = examPassedSet();
                    boolean firstPass = exams.add(String.valueOf(unit));
                    prefs.edit().putStringSet(KEY_EXAMS, new HashSet<>(exams)).apply();
                    if (firstPass) awardXp(120);
                }

                LinearLayout result = card();
                result.setBackground(rounded(pass ? Color.rgb(235, 250, 240) : Color.rgb(255, 244, 244), pass ? Color.rgb(185, 230, 197) : Color.rgb(244, 198, 198), 20));
                result.addView(heading(pass ? "🎉 ¡Unidad superada!" : "Casi lo tienes", 22, pass ? GREEN_DARK : RED));
                result.addView(body("Resultado: " + correct + "/7" + (pass ? " · +120 XP · Nueva etapa desbloqueada" : " · necesitas 6/7")));
                result.addView(primaryButton(pass ? "Continuar ruta" : "Reintentar", pass ? MainActivity.this::showPath : () -> showUnitExam(unit)));
                resultHost.addView(result);
            }
        }
        ExamTracker tracker = new ExamTracker();

        for (int qIndex = 0; qIndex < questions.length; qIndex++) {
            String[] q = questions[qIndex];
            LinearLayout c = card();
            c.addView(label("PREGUNTA " + (qIndex + 1) + " DE 7", BLUE));
            c.addView(heading(q[0], 18, TEXT));
            LinearLayout optionsHost = verticalBox();
            for (int a = 1; a <= 3; a++) {
                final String option = q[a];
                Button b = secondaryButton(option, () -> {});
                b.setOnClickListener(v -> {
                    if (!v.isEnabled() || tracker.finished) return;
                    for (int k = 0; k < optionsHost.getChildCount(); k++) optionsHost.getChildAt(k).setEnabled(false);
                    boolean ok = option.equals(q[4]);
                    if (ok) {
                        ((Button) v).setText("✓ " + option);
                        ((Button) v).setTextColor(GREEN_DARK);
                        awardXp(15);
                    } else {
                        ((Button) v).setText("✗ " + option + " · Correcta: " + q[4]);
                        ((Button) v).setTextColor(RED);
                        loseHeart();
                    }
                    tracker.record(ok);
                });
                optionsHost.addView(b);
            }
            c.addView(optionsHost);
            box.addView(c);
        }

        String[] typed = EXAM_TYPED[unit];
        LinearLayout writeCard = card();
        writeCard.addView(label("PREGUNTA 6 DE 7 · ESCRIBE", PURPLE));
        writeCard.addView(heading(typed[0], 18, TEXT));
        EditText answer = new EditText(this);
        answer.setHint("Escribe tu respuesta aquí");
        answer.setTextSize(18);
        answer.setSingleLine(true);
        answer.setPadding(dp(14), dp(10), dp(14), dp(10));
        answer.setBackground(rounded(Color.WHITE, BORDER, 14));
        writeCard.addView(answer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        TextView writeResult = body("");
        Button checkWrite = primaryButton("Comprobar respuesta", () -> {});
        checkWrite.setOnClickListener(v -> {
            if (!v.isEnabled() || tracker.finished) return;
            String given = answer.getText().toString();
            if (given.trim().isEmpty()) {
                Toast.makeText(this, "Escribe una respuesta primero", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = normalizeAnswer(given).equals(normalizeAnswer(typed[1]));
            answer.setEnabled(false);
            v.setEnabled(false);
            if (ok) {
                writeResult.setText("✓ ¡Correcto!");
                writeResult.setTextColor(GREEN_DARK);
                awardXp(20);
            } else {
                writeResult.setText("✗ Respuesta correcta: " + typed[1]);
                writeResult.setTextColor(RED);
                loseHeart();
            }
            tracker.record(ok);
        });
        writeCard.addView(checkWrite);
        writeCard.addView(writeResult);
        box.addView(writeCard);

        String[] speech = EXAM_SPEECH[unit];
        LinearLayout speechCard = card();
        speechCard.addView(label("PREGUNTA 7 DE 7 · PRONUNCIACIÓN", GREEN));
        speechCard.addView(heading(speech[0], 18, TEXT));
        TextView targetPhrase = heading(speech[1], 23, BLUE_DARK);
        targetPhrase.setGravity(Gravity.CENTER);
        speechCard.addView(targetPhrase);
        LinearLayout speechButtons = horizontal();
        speechButtons.addView(secondaryButton("🔊 Escuchar", () -> speakNative(speech[1])), new LinearLayout.LayoutParams(0, dp(50), 1f));
        TextView examSpeechResult = body("Toca Hablar y pronuncia la frase.");
        examSpeechResult.setGravity(Gravity.CENTER);
        Button speakButton = primaryButton("🎙️ Hablar", () -> {});
        LinearLayout.LayoutParams speakLp = new LinearLayout.LayoutParams(0, dp(50), 1f);
        speakLp.setMargins(dp(8), 0, 0, 0);
        speechButtons.addView(speakButton, speakLp);
        speechCard.addView(speechButtons);
        speechCard.addView(examSpeechResult);
        speakButton.setOnClickListener(v -> {
            if (!v.isEnabled() || tracker.finished) return;
            startSpeechRecognition(speech[1], examSpeechResult, false, (score, heard) -> {
                if (!speakButton.isEnabled() || tracker.finished) return;
                boolean ok = score >= 70;
                speakButton.setEnabled(false);
                if (ok) {
                    awardXp(20);
                } else {
                    loseHeart();
                }
                tracker.record(ok);
            });
        });
        box.addView(speechCard);

        box.addView(resultHost);
        setContent(scroll);
    }

    private List<Integer> buildPracticeSession(boolean reviewOnly) {
        List<Integer> session = new ArrayList<>();
        List<Integer> weak = new ArrayList<>();
        for (String s : errorSet()) {
            try {
                int idx = Integer.parseInt(s);
                if (idx >= 0 && idx < EXERCISES.length) weak.add(idx);
            } catch (Exception ignored) {}
        }
        Collections.sort(weak, (a, b) -> Integer.compare(weakCount(b), weakCount(a)));

        if (reviewOnly) {
            for (int idx : weak) {
                if (session.size() >= 10) break;
                session.add(idx);
            }
            return session;
        }

        for (int idx : weak) {
            if (session.size() >= 3) break;
            session.add(idx);
        }

        int unit = currentLearningUnit();
        for (int i = 0; i < EXERCISES.length && session.size() < 10; i++) {
            if (EXERCISES[i].unit == unit && !session.contains(i)) session.add(i);
        }
        for (int i = 0; i < EXERCISES.length && session.size() < 10; i++) {
            if (!session.contains(i)) session.add(i);
        }
        return session;
    }

    private int currentLearningUnit() {
        List<PostItem> ordered = getOrderedPosts();
        int startIndex = prefs.getInt(KEY_START_INDEX, 0);
        int current = ordered.isEmpty() ? startIndex : findCurrentPathIndex(ordered, Math.min(startIndex, Math.max(0, ordered.size() - 1)));
        if (current < 0) {
            for (int u = 0; u < UNIT_NAMES.length; u++) {
                if (isUnitUnlocked(u, startIndex) && !examPassedSet().contains(String.valueOf(u))) return u;
            }
            return unitForLesson(startIndex);
        }
        return unitForLesson(current);
    }

    private void renderPracticeQuestion(List<Integer> session, int position, int score, boolean reviewOnly, long startedAt) {
        if (position >= session.size()) {
            showPracticeResult(score, session.size(), reviewOnly, startedAt);
            return;
        }

        currentSection = "practice";
        int exerciseIndex = session.get(position);
        Exercise ex = EXERCISES[exerciseIndex];
        LinearLayout root = verticalBox();
        root.setPadding(dp(16), dp(14), dp(16), dp(22));
        root.setBackgroundColor(BG);

        LinearLayout top = horizontal();
        top.addView(smallButton("✕", this::showPracticeHub), new LinearLayout.LayoutParams(dp(50), dp(44)));
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(session.size());
        progress.setProgress(position);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, dp(10), 1f);
        pLp.setMargins(dp(10), 0, dp(10), 0);
        top.addView(progress, pLp);
        TextView hearts = label("❤️ " + prefs.getInt(KEY_LIVES, 5), RED);
        hearts.setGravity(Gravity.CENTER);
        top.addView(hearts, new LinearLayout.LayoutParams(dp(60), dp(44)));
        root.addView(top);
        root.addView(spacer(18));

        root.addView(label(exerciseTypeLabel(ex.type) + " · UNIDAD " + (ex.unit + 1), BLUE));
        root.addView(heading(ex.prompt, 25, BLUE_DARK));
        root.addView(body("Ejercicio " + (position + 1) + " de " + session.size() + " · Puntuación: " + score));
        root.addView(spacer(8));

        if (ex.type == TYPE_CHOICE || ex.type == TYPE_LISTEN_CHOICE) {
            if (ex.type == TYPE_LISTEN_CHOICE) root.addView(primaryButton("🔊 Escuchar", () -> speakNative(ex.speak)));
            for (String option : ex.options) {
                root.addView(optionButton(option, false, () -> answerPractice(session, position, score, reviewOnly, startedAt, exerciseIndex, option.equals(ex.answer))));
            }
        } else if (ex.type == TYPE_FILL || ex.type == TYPE_WRITE_LISTEN) {
            if (ex.type == TYPE_WRITE_LISTEN) root.addView(primaryButton("🔊 Escuchar", () -> speakNative(ex.speak)));
            EditText input = new EditText(this);
            input.setSingleLine(false);
            input.setHint(ex.type == TYPE_FILL ? "Escribe la palabra que falta" : "Escribe la frase que escuchas");
            input.setTextSize(18);
            input.setPadding(dp(14), dp(12), dp(14), dp(12));
            input.setBackground(rounded(Color.WHITE, BORDER, 16));
            root.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));
            root.addView(spacer(10));
            root.addView(primaryButton("COMPROBAR", () -> {
                String given = input.getText().toString();
                if (given.trim().isEmpty()) {
                    Toast.makeText(this, "Escribe una respuesta primero", Toast.LENGTH_SHORT).show();
                    return;
                }
                answerPractice(session, position, score, reviewOnly, startedAt, exerciseIndex, normalizeAnswer(given).equals(normalizeAnswer(ex.answer)));
            }));
        } else if (ex.type == TYPE_ORDER) {
            TextView assembled = heading("", 21, TEXT);
            assembled.setMinHeight(dp(58));
            assembled.setGravity(Gravity.CENTER_VERTICAL);
            assembled.setBackground(rounded(Color.WHITE, BORDER, 16));
            assembled.setPadding(dp(14), dp(10), dp(14), dp(10));
            root.addView(assembled);
            root.addView(spacer(10));
            LinearLayout tokens = verticalBox();
            List<String> selected = new ArrayList<>();
            List<Button> tokenButtons = new ArrayList<>();
            for (String token : ex.options) {
                Button b = secondaryButton(token, () -> {});
                b.setOnClickListener(v -> {
                    selected.add(token);
                    assembled.setText(joinLabels(selected, " "));
                    v.setEnabled(false);
                });
                tokenButtons.add(b);
                tokens.addView(b);
            }
            root.addView(tokens);
            LinearLayout actions = horizontal();
            Button reset = secondaryButton("↺ Reiniciar", () -> {
                selected.clear();
                assembled.setText("");
                for (Button b : tokenButtons) b.setEnabled(true);
            });
            actions.addView(reset, new LinearLayout.LayoutParams(0, dp(50), 1f));
            LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(0, dp(50), 1f);
            checkLp.setMargins(dp(8), 0, 0, 0);
            actions.addView(primaryButton("COMPROBAR", () -> {
                if (selected.isEmpty()) {
                    Toast.makeText(this, "Ordena las palabras primero", Toast.LENGTH_SHORT).show();
                    return;
                }
                answerPractice(session, position, score, reviewOnly, startedAt, exerciseIndex, normalizeAnswer(joinLabels(selected, " ")).equals(normalizeAnswer(ex.answer)));
            }), checkLp);
            root.addView(spacer(10));
            root.addView(actions);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContent(scroll);
    }

    private void answerPractice(List<Integer> session, int position, int score, boolean reviewOnly, long startedAt, int exerciseIndex, boolean correct) {
        Exercise ex = EXERCISES[exerciseIndex];
        recordExerciseResult(exerciseIndex, correct);
        if (correct) Toast.makeText(this, "✓ Correcto · +10 XP", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "✗ Correcta: " + ex.answer, Toast.LENGTH_LONG).show();
        renderPracticeQuestion(session, position + 1, score + (correct ? 1 : 0), reviewOnly, startedAt);
    }

    private void showPracticeResult(int score, int total, boolean reviewOnly, long startedAt) {
        addStudySession(startedAt);
        recordStudyActivity();
        if (total > 0 && score == total) awardXp(20);
        int pct = total == 0 ? 0 : Math.round(score * 100f / total);

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(18), dp(20), dp(18), dp(28));
        scroll.addView(box);
        TextView icon = heading(pct >= 80 ? "🎉" : "💪", 48, BLUE_DARK);
        icon.setGravity(Gravity.CENTER);
        box.addView(icon);
        TextView title = heading(reviewOnly ? "Repaso terminado" : "Sesión completada", 27, BLUE_DARK);
        title.setGravity(Gravity.CENTER);
        box.addView(title);
        TextView result = heading(score + "/" + total + " · " + pct + "%", 31, pct >= 80 ? GREEN_DARK : ORANGE);
        result.setGravity(Gravity.CENTER);
        box.addView(result);
        TextView desc = body(pct >= 80 ? "Buen resultado. Los conceptos acertados reducen su prioridad de repaso." : "Los errores quedaron guardados y aparecerán con mayor frecuencia en próximas sesiones.");
        desc.setGravity(Gravity.CENTER);
        box.addView(desc);
        box.addView(primaryButton("Continuar ruta", this::showPath));
        box.addView(secondaryButton("Otra sesión", () -> showQuiz(false)));
        setContent(scroll);
    }

    private void recordExerciseResult(int exerciseIndex, boolean correct) {
        Set<String> errors = errorSet();
        Set<String> mastered = masteredSet();
        int weak = weakCount(exerciseIndex);
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_TOTAL_ANSWERED, prefs.getInt(KEY_TOTAL_ANSWERED, 0) + 1);
        if (correct) {
            e.putInt(KEY_TOTAL_CORRECT, prefs.getInt(KEY_TOTAL_CORRECT, 0) + 1);
            weak = Math.max(0, weak - 1);
            if (weak == 0) errors.remove(String.valueOf(exerciseIndex));
            mastered.add(String.valueOf(exerciseIndex));
        } else {
            weak++;
            errors.add(String.valueOf(exerciseIndex));
        }
        e.putInt(WEAK_PREFIX + exerciseIndex, weak);
        e.putStringSet(KEY_ERRORS, new HashSet<>(errors));
        e.putStringSet(KEY_MASTERED, new HashSet<>(mastered));
        e.apply();
        if (correct) awardXp(10); else loseHeart();
    }

    private void recordGenericAnswer(boolean correct) {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_TOTAL_ANSWERED, prefs.getInt(KEY_TOTAL_ANSWERED, 0) + 1);
        if (correct) e.putInt(KEY_TOTAL_CORRECT, prefs.getInt(KEY_TOTAL_CORRECT, 0) + 1);
        e.apply();
    }

    private int weakCount(int exerciseIndex) {
        return prefs.getInt(WEAK_PREFIX + exerciseIndex, 0);
    }

    private void addStudySession(long startedAt) {
        long elapsed = Math.max(0, System.currentTimeMillis() - startedAt);
        int minutes = Math.max(1, (int) Math.ceil(elapsed / 60000.0));
        prefs.edit()
                .putInt(KEY_STUDY_MINUTES, prefs.getInt(KEY_STUDY_MINUTES, 0) + minutes)
                .putInt(KEY_STUDY_SESSIONS, prefs.getInt(KEY_STUDY_SESSIONS, 0) + 1)
                .apply();
    }

    private String normalizeAnswer(String value) {
        if (value == null) return "";
        String n = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private String exerciseTypeLabel(int type) {
        if (type == TYPE_ORDER) return "ORDENA LA FRASE";
        if (type == TYPE_FILL) return "COMPLETA";
        if (type == TYPE_LISTEN_CHOICE) return "ESCUCHA Y ELIGE";
        if (type == TYPE_WRITE_LISTEN) return "ESCUCHA Y ESCRIBE";
        return "ELIGE LA RESPUESTA";
    }

    private void speakNative(String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (!ttsReady || textToSpeech == null) {
            Toast.makeText(this, "El audio todavía se está preparando", Toast.LENGTH_SHORT).show();
            return;
        }
        textToSpeech.setLanguage(Locale.US);
        textToSpeech.setSpeechRate(0.85f);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "practice_" + System.currentTimeMillis());
    }

    private String userRank() {
        int xp = prefs.getInt(KEY_XP, 0);
        if (xp >= 2500) return "Avanzado";
        if (xp >= 1200) return "Conversador";
        if (xp >= 500) return "Explorador";
        if (xp >= 150) return "Aprendiz";
        return "Novato";
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
        int exams = examPassedSet().size();
        int mastered = masteredSet().size();

        box.addView(heading("Tus logros", 27, BLUE_DARK));
        box.addView(body("Aquí se ve lo que has conseguido de verdad: estudiar, practicar, aprobar y mantener constancia."));
        box.addView(achievementCard("🌱", "Primer paso", "Completa tu primera lección", done >= 1));
        box.addView(achievementCard("⭐", "100 XP", "Consigue 100 puntos de experiencia", xp >= 100));
        box.addView(achievementCard("📚", "Estudiante constante", "Completa 5 lecciones", done >= 5));
        box.addView(achievementCard("🎓", "Primer examen", "Aprueba un examen de unidad", exams >= 1));
        box.addView(achievementCard("🧠", "20 conceptos", "Domina 20 ejercicios diferentes", mastered >= 20));
        box.addView(achievementCard("🔥", "Racha de 3", "Estudia 3 días seguidos", streak >= 3));
        box.addView(achievementCard("🔥", "Racha de 7", "Estudia 7 días seguidos", streak >= 7));
        box.addView(achievementCard("🏆", "Ruta completada", "Completa las lecciones y exámenes disponibles", done >= total && total > 1 && allRequiredExamsPassed(prefs.getInt(KEY_START_INDEX, 0))));
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
        int placement = prefs.getInt(KEY_LEVEL_TEST_SCORE, -1);
        if (placement >= 0) profile.addView(body("Prueba de nivel: " + placement + "/" + LEVEL_TEST.length));
        box.addView(profile);

        int answered = prefs.getInt(KEY_TOTAL_ANSWERED, 0);
        int correct = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        int accuracy = answered == 0 ? 0 : Math.round(correct * 100f / answered);
        LinearLayout stats = card();
        stats.addView(label("ESTADÍSTICAS", GREEN));
        stats.addView(heading("⭐ " + prefs.getInt(KEY_XP, 0) + " XP", 21, BLUE_DARK));
        stats.addView(body("🔥 Racha: " + prefs.getInt(KEY_STREAK, 0) + " días"));
        stats.addView(body("✓ Lecciones completadas: " + completedSet().size()));
        stats.addView(body("🎓 Exámenes aprobados: " + examPassedSet().size() + "/" + UNIT_NAMES.length));
        stats.addView(body("🎯 Precisión: " + accuracy + "%  (" + correct + "/" + answered + ")"));
        stats.addView(body("🧠 Conceptos dominados: " + masteredSet().size() + "/" + EXERCISES.length));
        stats.addView(body("⏱ Tiempo de estudio: " + prefs.getInt(KEY_STUDY_MINUTES, 0) + " min"));
        stats.addView(body("📊 Sesiones: " + prefs.getInt(KEY_STUDY_SESSIONS, 0)));
        stats.addView(body("🏅 Nivel de usuario: " + userRank()));
        box.addView(stats);

        LinearLayout reminder = card();
        reminder.addView(label("RECORDATORIO", ORANGE));
        boolean enabled = prefs.getBoolean(KEY_REMINDER, true);
        reminder.addView(heading("Práctica diaria", 20, BLUE_DARK));
        reminder.addView(body("Recordatorio aproximado a las 19:00 para mantener la racha."));
        reminder.addView(secondaryButton(enabled ? "🔔 Activado" : "🔕 Activar", this::toggleReminder));
        box.addView(reminder);

        LinearLayout calendar = card();
        calendar.addView(label("CALENDARIO", GREEN));
        calendar.addView(heading("Tu constancia", 20, BLUE_DARK));
        calendar.addView(body("Consulta qué días estudiaste durante el mes actual."));
        calendar.addView(secondaryButton("📅 Ver calendario de estudio", this::showStudyCalendar));
        box.addView(calendar);

        LinearLayout backup = card();
        backup.addView(label("COPIA Y RESTAURACIÓN", BLUE));
        backup.addView(heading("Lleva tu progreso a otro teléfono", 20, BLUE_DARK));
        backup.addView(body("Guarda un archivo JSON en Google Drive, OneDrive o el almacenamiento del teléfono y restáuralo después. No requiere cuenta dentro de la app."));
        LinearLayout backupBtns = horizontal();
        backupBtns.addView(primaryButton("☁️ Guardar copia", this::exportProgress), new LinearLayout.LayoutParams(0, dp(46), 1f));
        LinearLayout.LayoutParams rb = new LinearLayout.LayoutParams(0, dp(46), 1f);
        rb.setMargins(dp(8), 0, 0, 0);
        backupBtns.addView(secondaryButton("↻ Restaurar", this::importProgress), rb);
        backup.addView(backupBtns);
        box.addView(backup);

        LinearLayout account = card();
        account.addView(label("PREFERENCIAS", PURPLE));
        account.addView(secondaryButton("🧪 Repetir prueba de nivel", this::restartLevelTest));
        account.addView(secondaryButton("♥ Ver favoritos", this::showFavorites));
        account.addView(secondaryButton("🔎 Todas las lecciones", this::showAllLessons));
        account.addView(secondaryButton("↻ Configurar inicio otra vez", this::resetOnboarding));
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
            Toast.makeText(this, "Lección completada · +50 XP · ¡Nueva etapa desbloqueada!", Toast.LENGTH_LONG).show();
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
        Set<String> studyDates = new HashSet<>(prefs.getStringSet(KEY_STUDY_DATES, Collections.emptySet()));
        studyDates.add(calendarDateKey(Calendar.getInstance()));
        prefs.edit().putStringSet(KEY_STUDY_DATES, new HashSet<>(studyDates)).apply();
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

    private void showPronunciationCoach() {
        currentSection = "practice";
        pronunciationIndex = Math.max(0, Math.min(pronunciationIndex, PRONUNCIATION_PHRASES.length - 1));
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        String[] item = PRONUNCIATION_PHRASES[pronunciationIndex];
        box.addView(heading("Pronunciación", 27, BLUE_DARK));
        box.addView(body("Escucha la frase, repítela y recibe una evaluación dentro de la app."));

        LinearLayout card = card();
        card.addView(label("FRASE " + (pronunciationIndex + 1) + "/" + PRONUNCIATION_PHRASES.length, GREEN));
        TextView en = heading(item[0], 24, BLUE_DARK);
        en.setGravity(Gravity.CENTER);
        card.addView(en);
        TextView es = body(item[1]);
        es.setGravity(Gravity.CENTER);
        card.addView(es);
        LinearLayout buttons = horizontal();
        buttons.addView(secondaryButton("🔊 Escuchar", () -> speakNative(item[0])), new LinearLayout.LayoutParams(0, dp(50), 1f));
        LinearLayout.LayoutParams micLp = new LinearLayout.LayoutParams(0, dp(50), 1f);
        micLp.setMargins(dp(8), 0, 0, 0);
        buttons.addView(primaryButton("🎙️ Hablar", () -> startSpeechRecognition(item[0])), micLp);
        card.addView(buttons);
        pronunciationResult = body("Pulsa Hablar cuando estés listo. El micrófono funciona dentro de la app.");
        pronunciationResult.setGravity(Gravity.CENTER);
        card.addView(pronunciationResult);
        box.addView(card);

        LinearLayout nav = horizontal();
        nav.addView(secondaryButton("← Anterior", () -> {
            pronunciationIndex = (pronunciationIndex - 1 + PRONUNCIATION_PHRASES.length) % PRONUNCIATION_PHRASES.length;
            showPronunciationCoach();
        }), new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        nextLp.setMargins(dp(8), 0, 0, 0);
        nav.addView(primaryButton("Siguiente →", () -> {
            pronunciationIndex = (pronunciationIndex + 1) % PRONUNCIATION_PHRASES.length;
            showPronunciationCoach();
        }), nextLp);
        box.addView(nav);

        int attempts = prefs.getInt(KEY_PRON_ATTEMPTS, 0);
        int good = prefs.getInt(KEY_PRON_GOOD, 0);
        int pct = attempts == 0 ? 0 : Math.round(good * 100f / attempts);
        box.addView(cardMessage("Tu progreso", "Intentos: " + attempts + " · Buenos intentos: " + good + " · Precisión: " + pct + "%"));
        setContent(scroll);
    }

    private void startSpeechRecognition(String expected) {
        startSpeechRecognition(expected, pronunciationResult, true, null);
    }

    private void startSpeechRecognition(String expected, TextView statusView, boolean awardStandaloneXp, SpeechEvaluationCallback callback) {
        pendingSpeechExpected = expected;
        speechStatusView = statusView;
        speechAwardStandaloneXp = awardStandaloneXp;
        pendingSpeechCallback = callback;

        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7110);
            return;
        }
        beginInternalSpeechRecognition();
    }

    private void beginInternalSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            setSpeechStatus("El reconocimiento de voz no está disponible en este teléfono.", RED);
            return;
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { setSpeechStatus("🎙️ Escuchando… habla ahora", BLUE); }
                @Override public void onBeginningOfSpeech() { setSpeechStatus("Te escucho…", BLUE_DARK); }
                @Override public void onRmsChanged(float rmsdB) { }
                @Override public void onBufferReceived(byte[] buffer) { }
                @Override public void onEndOfSpeech() { setSpeechStatus("Evaluando pronunciación…", MUTED); }
                @Override public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_NO_MATCH: message = "No pude reconocer la frase. Inténtalo otra vez."; break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No escuché tu voz. Toca Hablar e inténtalo otra vez."; break;
                        case SpeechRecognizer.ERROR_AUDIO: message = "No pude acceder al audio del micrófono."; break;
                        case SpeechRecognizer.ERROR_NETWORK: case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "No se pudo procesar la voz. Revisa tu conexión e inténtalo otra vez."; break;
                        default: message = "No pude evaluar la pronunciación. Inténtalo otra vez.";
                    }
                    setSpeechStatus(message, RED);
                }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String heard = (matches == null || matches.isEmpty()) ? "" : matches.get(0);
                    handleInternalSpeechResult(heard);
                }
                @Override public void onPartialResults(Bundle partialResults) { }
                @Override public void onEvent(int eventType, Bundle params) { }
            });
        } else {
            speechRecognizer.cancel();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizer.startListening(intent);
    }

    private void handleInternalSpeechResult(String heard) {
        int score = pronunciationScore(pendingSpeechExpected, heard);
        int attempts = prefs.getInt(KEY_PRON_ATTEMPTS, 0) + 1;
        int good = prefs.getInt(KEY_PRON_GOOD, 0) + (score >= 70 ? 1 : 0);
        prefs.edit().putInt(KEY_PRON_ATTEMPTS, attempts).putInt(KEY_PRON_GOOD, good).apply();

        if (score >= 70 && speechAwardStandaloneXp) {
            awardXp(10);
            recordStudyActivity();
        }

        if (score >= 90) setSpeechStatus("¡Excelente pronunciación! ✅\nPrecisión: " + score + "%", GREEN_DARK);
        else if (score >= 70) setSpeechStatus("¡Muy bien! ✅\nPrecisión: " + score + "%", GREEN_DARK);
        else if (score >= 50) setSpeechStatus("Casi. Inténtalo otra vez.\nPrecisión: " + score + "%", ORANGE);
        else setSpeechStatus("Vamos otra vez.\nPrecisión: " + score + "%", RED);

        SpeechEvaluationCallback callback = pendingSpeechCallback;
        pendingSpeechCallback = null;
        if (callback != null) callback.onEvaluated(score, heard);
    }

    private void setSpeechStatus(String text, int color) {
        if (speechStatusView != null) {
            speechStatusView.setText(text);
            speechStatusView.setTextColor(color);
        }
    }

    private int pronunciationScore(String expected, String heard) {
        String e = normalizeAnswer(expected);
        String h = normalizeAnswer(heard);
        if (e.equals(h)) return 100;
        if (e.isEmpty() || h.isEmpty()) return 0;
        String[] words = e.split(" ");
        int matched = 0;
        for (String w : words) {
            if (h.matches(".*\\b" + java.util.regex.Pattern.quote(w) + "\\b.*")) matched++;
        }
        return Math.round(matched * 100f / Math.max(1, words.length));
    }

    private void showVocabulary() {
        currentSection = "practice";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);
        box.addView(heading("Mi vocabulario", 27, BLUE_DARK));
        box.addView(body("Cada palabra tiene una etapa de memoria. Las etapas más altas aparecen con menos frecuencia."));

        int learned = 0;
        for (int i = 0; i < VOCABULARY.length; i++) {
            int stage = prefs.getInt(VOCAB_STAGE_PREFIX + i, 0);
            if (stage > 0) learned++;
            long due = prefs.getLong(VOCAB_DUE_PREFIX + i, 0L);
            LinearLayout c = card();
            c.addView(label(stageLabel(stage), stage >= 3 ? GREEN : BLUE));
            LinearLayout row = horizontal();
            LinearLayout text = verticalBox();
            text.addView(heading(VOCABULARY[i][0], 20, BLUE_DARK));
            text.addView(body(VOCABULARY[i][1] + " · " + dueLabel(due)));
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            final int idx = i;
            row.addView(smallButton("🔊", () -> speakNative(VOCABULARY[idx][0])), new LinearLayout.LayoutParams(dp(52), dp(44)));
            c.addView(row);
            box.addView(c);
        }
        box.addView(cardMessage("Resumen", learned + " de " + VOCABULARY.length + " palabras ya comenzaron su ciclo de memoria."));
        box.addView(primaryButton("Repasar palabras pendientes", this::showSpacedReview));
        setContent(scroll);
    }

    private void showSpacedReview() {
        List<Integer> due = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i = 0; i < VOCABULARY.length; i++) {
            long d = prefs.getLong(VOCAB_DUE_PREFIX + i, 0L);
            if (d == 0L || d <= now) due.add(i);
        }
        if (due.isEmpty()) {
            ScrollView scroll = new ScrollView(this);
            LinearLayout box = verticalBox();
            box.setPadding(dp(14), dp(14), dp(14), dp(28));
            box.addView(cardMessage("✓ Repaso al día", "No tienes palabras vencidas. Vuelve más tarde para mantener la memoria activa."));
            box.addView(secondaryButton("Ver mi vocabulario", this::showVocabulary));
            scroll.addView(box);
            setContent(scroll);
            return;
        }
        renderWordReview(due, 0, false);
    }

    private void renderWordReview(List<Integer> due, int pos, boolean revealed) {
        if (pos >= due.size()) {
            ScrollView scroll = new ScrollView(this);
            LinearLayout box = verticalBox();
            box.setPadding(dp(14), dp(14), dp(14), dp(28));
            box.addView(cardMessage("🎉 Repaso terminado", "Completaste " + due.size() + " palabra(s). La próxima fecha depende de cómo calificaste cada una."));
            box.addView(primaryButton("Volver a practicar", this::showPracticeHub));
            scroll.addView(box);
            setContent(scroll);
            return;
        }
        int idx = due.get(pos);
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);
        box.addView(label("REPASO " + (pos + 1) + "/" + due.size(), PURPLE));
        LinearLayout c = card();
        TextView word = heading(VOCABULARY[idx][0], 30, BLUE_DARK);
        word.setGravity(Gravity.CENTER);
        c.addView(word);
        c.addView(secondaryButton("🔊 Escuchar", () -> speakNative(VOCABULARY[idx][0])));
        if (!revealed) {
            c.addView(primaryButton("Mostrar significado", () -> renderWordReview(due, pos, true)));
        } else {
            TextView meaning = heading(VOCABULARY[idx][1], 24, GREEN_DARK);
            meaning.setGravity(Gravity.CENTER);
            c.addView(meaning);
            c.addView(body("¿Qué tan bien la recordaste?"));
            LinearLayout row = horizontal();
            row.addView(secondaryButton("Necesito repasar", () -> {
                rateVocabulary(idx, false);
                renderWordReview(due, pos + 1, false);
            }), new LinearLayout.LayoutParams(0, dp(50), 1f));
            LinearLayout.LayoutParams goodLp = new LinearLayout.LayoutParams(0, dp(50), 1f);
            goodLp.setMargins(dp(8), 0, 0, 0);
            row.addView(primaryButton("La recordé", () -> {
                rateVocabulary(idx, true);
                renderWordReview(due, pos + 1, false);
            }), goodLp);
            c.addView(row);
        }
        box.addView(c);
        setContent(scroll);
    }

    private void rateVocabulary(int idx, boolean remembered) {
        int stage = prefs.getInt(VOCAB_STAGE_PREFIX + idx, 0);
        if (remembered) stage = Math.min(4, stage + 1); else stage = 0;
        int[] days = {1, 3, 7, 14, 30};
        long next = System.currentTimeMillis() + days[stage] * 24L * 60L * 60L * 1000L;
        prefs.edit().putInt(VOCAB_STAGE_PREFIX + idx, stage).putLong(VOCAB_DUE_PREFIX + idx, next).apply();
        if (remembered) awardXp(5);
        recordStudyActivity();
    }

    private String stageLabel(int stage) {
        if (stage >= 4) return "MEMORIA FUERTE";
        if (stage == 3) return "CONSOLIDANDO";
        if (stage == 2) return "APRENDIDA";
        if (stage == 1) return "EN PROGRESO";
        return "NUEVA";
    }

    private String dueLabel(long due) {
        if (due == 0L || due <= System.currentTimeMillis()) return "repaso pendiente";
        long days = Math.max(1, (due - System.currentTimeMillis()) / (24L * 60L * 60L * 1000L));
        return "próximo repaso en " + days + " día(s)";
    }

    private void showConversations() {
        currentSection = "practice";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);
        box.addView(heading("Conversaciones", 27, BLUE_DARK));
        box.addView(body("Practica respuestas útiles en situaciones reales. Cada escenario tiene cuatro intercambios."));
        for (int i = 0; i < CONVERSATION_NAMES.length; i++) {
            final int scenario = i;
            LinearLayout c = card();
            c.addView(label("ESCENARIO " + (i + 1), ORANGE));
            c.addView(heading(CONVERSATION_NAMES[i], 21, BLUE_DARK));
            c.addView(body(conversationDescription(i)));
            c.addView(primaryButton("Empezar", () -> renderConversation(scenario, 0, 0)));
            box.addView(c);
        }
        setContent(scroll);
    }

    private String conversationDescription(int i) {
        if (i == 0) return "Pedir comida y responder al camarero.";
        if (i == 1) return "Facturación, equipaje y destino.";
        return "Respuestas sencillas para una entrevista de trabajo.";
    }

    private void renderConversation(int scenario, int step, int score) {
        if (step >= CONVERSATIONS[scenario].length) {
            ScrollView scroll = new ScrollView(this);
            LinearLayout box = verticalBox();
            box.setPadding(dp(14), dp(14), dp(14), dp(28));
            box.addView(cardMessage("💬 Conversación completada", "Resultado: " + score + "/" + CONVERSATIONS[scenario].length + ". Puedes repetirla para mejorar respuestas y pronunciación."));
            box.addView(primaryButton("Repetir conversación", () -> renderConversation(scenario, 0, 0)));
            box.addView(secondaryButton("Ver otros escenarios", this::showConversations));
            scroll.addView(box);
            setContent(scroll);
            return;
        }
        String[] q = CONVERSATIONS[scenario][step];
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);
        box.addView(label(CONVERSATION_NAMES[scenario] + " · " + (step + 1) + "/" + CONVERSATIONS[scenario].length, ORANGE));
        LinearLayout npc = card();
        npc.addView(heading(q[0], 22, BLUE_DARK));
        npc.addView(secondaryButton("🔊 Escuchar", () -> speakNative(q[0].replaceFirst("^[^:]+:\\s*", ""))));
        box.addView(npc);
        box.addView(body("Elige la respuesta más natural:"));
        for (int i = 1; i <= 3; i++) {
            final String choice = q[i];
            box.addView(optionButton(choice, false, () -> {
                boolean ok = choice.equals(q[4]);
                if (ok) {
                    awardXp(5);
                    Toast.makeText(this, "✓ Respuesta natural · +5 XP", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Mejor opción: " + q[4], Toast.LENGTH_LONG).show();
                }
                recordStudyActivity();
                renderConversation(scenario, step + 1, score + (ok ? 1 : 0));
            }));
        }
        setContent(scroll);
    }

    private void showStudyCalendar() {
        currentSection = "profile";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        scroll.addView(box);

        Calendar now = Calendar.getInstance();
        String month = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")).format(now.getTime());
        box.addView(heading("Calendario de estudio", 27, BLUE_DARK));
        box.addView(heading(month.substring(0, 1).toUpperCase(new Locale("es", "ES")) + month.substring(1), 20, GREEN_DARK));

        Set<String> studied = new HashSet<>(prefs.getStringSet(KEY_STUDY_DATES, Collections.emptySet()));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(7);
        String[] headers = {"L", "M", "X", "J", "V", "S", "D"};
        for (String h : headers) {
            TextView tv = calendarCell(h, false, true);
            grid.addView(tv, new GridLayout.LayoutParams());
        }

        Calendar first = (Calendar) now.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        int dow = first.get(Calendar.DAY_OF_WEEK);
        int mondayIndex = (dow + 5) % 7;
        for (int i = 0; i < mondayIndex; i++) grid.addView(calendarCell("", false, false));
        int max = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        int active = 0;
        for (int day = 1; day <= max; day++) {
            Calendar c = (Calendar) first.clone();
            c.set(Calendar.DAY_OF_MONTH, day);
            boolean done = studied.contains(calendarDateKey(c));
            if (done) active++;
            grid.addView(calendarCell(String.valueOf(day), done, false));
        }
        box.addView(grid);
        box.addView(cardMessage("Actividad del mes", active + " día(s) con estudio registrado. Tu racha actual es de " + prefs.getInt(KEY_STREAK, 0) + " día(s)."));
        box.addView(secondaryButton("← Volver al perfil", this::showProfile));
        setContent(scroll);
    }

    private TextView calendarCell(String text, boolean studied, boolean header) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setGravity(Gravity.CENTER);
        v.setTextSize(header ? 12 : 14);
        v.setTypeface(header || studied ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        v.setTextColor(studied ? Color.WHITE : (header ? MUTED : TEXT));
        v.setBackground(rounded(studied ? GREEN : Color.WHITE, BORDER, 10));
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(44);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        v.setLayoutParams(lp);
        return v;
    }

    private String calendarDateKey(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    private void exportProgress() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "AprendeGratisIngles_progreso.json");
        startActivityForResult(intent, REQ_EXPORT);
    }

    private void importProgress() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQ_IMPORT);
    }

    private JSONObject progressAsJson() throws Exception {
        JSONObject root = new JSONObject();
        root.put("app", "Aprende gratis inglés");
        root.put("version", "4.0");
        JSONObject data = new JSONObject();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Set) {
                JSONArray arr = new JSONArray();
                for (Object item : (Set<?>) value) arr.put(String.valueOf(item));
                data.put(entry.getKey(), arr);
            } else {
                data.put(entry.getKey(), value);
            }
        }
        root.put("data", data);
        return root;
    }

    private void saveProgressToUri(Uri uri) {
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new Exception("No output stream");
            out.write(progressAsJson().toString(2).getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Copia de progreso guardada", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo guardar la copia", Toast.LENGTH_LONG).show();
        }
    }

    private void restoreProgressFromUri(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONObject root = new JSONObject(sb.toString());
            JSONObject data = root.getJSONObject("data");
            SharedPreferences.Editor editor = prefs.edit();
            java.util.Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = data.get(key);
                if (value instanceof JSONArray) {
                    JSONArray arr = (JSONArray) value;
                    Set<String> set = new HashSet<>();
                    for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                    editor.putStringSet(key, set);
                } else if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                else if (value instanceof Long) editor.putLong(key, (Long) value);
                else if (value instanceof Double) {
                    double d = (Double) value;
                    if (d == Math.rint(d) && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) editor.putInt(key, (int) d);
                    else editor.putFloat(key, (float) d);
                } else editor.putString(key, String.valueOf(value));
            }
            editor.apply();
            ensureDayState();
            updateHeaderStats();
            Toast.makeText(this, "Progreso restaurado", Toast.LENGTH_LONG).show();
            showProfile();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo restaurar la copia", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == REQ_EXPORT) {
            saveProgressToUri(data.getData());
        } else if (requestCode == REQ_IMPORT) {
            restoreProgressFromUri(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7110 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            beginInternalSpeechRecognition();
        } else if (requestCode == 7110) {
            setSpeechStatus("Necesitas permitir el micrófono para practicar pronunciación.", RED);
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
        refreshBottomNav();
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
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        super.onDestroy();
    }
}
