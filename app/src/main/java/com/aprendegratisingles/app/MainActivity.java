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
import android.net.Uri;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import android.widget.VideoView;
import android.text.InputType;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    private static final int REQ_AVATAR = 801;
    private static final long REENTRY_SPLASH_MS = 3L * 60L * 1000L;

    private SharedPreferences prefs;
    private FrameLayout root;
    private TextToSpeech tts;
    private boolean ttsReady;
    private SpeechRecognizer recognizer;
    private boolean initialSplashFinished = false;
    private boolean startupVideoVisible = false;
    private VideoView startupVideo;
    private int startupVideoPosition = 0;
    private long backgroundStartedAt = 0L;
    private final Random random = new Random();

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
    private TextView subtitleText;
    private View subtitleCard;
    private String pendingAutoSpeak;

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

    private static final int RETURN_ROUTE = 0;
    private static final int RETURN_PRACTICE = 1;
    private int lessonReturnTarget = RETURN_ROUTE;
    private int practiceSection = 0;
    private boolean notificationPermissionFromOnboarding = false;

    private int soundQuizQuestion = 0;
    private int soundQuizCorrect = 0;
    private final ArrayList<SoundPair> soundQuizQueue = new ArrayList<>();
    private SoundPair currentSoundPair;
    private String selectedSoundAnswer = "";
    private TextView soundCheckButton;
    private TextView soundOptionA;
    private TextView soundOptionB;
    private TextView soundFeedback;
    private boolean inSoundQuiz = false;

    // V8: biblioteca nativa inspirada en el contenido educativo del sitio.
    // No se abre ningún WebView: todo vive dentro de la app y usa el TTS de Android.
    private final StudyLesson[] studyLessons = new StudyLesson[]{
            new StudyLesson("Tiempo y frecuencia", "Palabras para hablar de cuándo ocurre algo.", 7, new StudyItem[]{
                    new StudyItem("Today", "tudéi", "Hoy"),
                    new StudyItem("Right now", "ráit náu", "Ahora mismo"),
                    new StudyItem("Tomorrow", "tamárou", "Mañana"),
                    new StudyItem("Every day", "évri déi", "Todos los días"),
                    new StudyItem("Yesterday", "yésterdei", "Ayer"),
                    new StudyItem("This morning", "dhis mórnin", "Esta mañana"),
                    new StudyItem("Always", "ólweis", "Siempre"),
                    new StudyItem("This week", "dhis uík", "Esta semana"),
                    new StudyItem("Never", "néver", "Nunca"),
                    new StudyItem("Sometimes", "sámtaims", "A veces"),
                    new StudyItem("Usually", "iúshuali", "Normalmente")
            }),
            new StudyLesson("Saludos y cortesía", "Frases esenciales para iniciar y terminar conversaciones.", 0, new StudyItem[]{
                    new StudyItem("Hello", "jelóu", "Hola"),
                    new StudyItem("Hi", "jái", "Hola / Buenas"),
                    new StudyItem("Good morning", "gud mórnin", "Buenos días"),
                    new StudyItem("Good afternoon", "gud áfternún", "Buenas tardes"),
                    new StudyItem("Good evening", "gud ívnin", "Buenas noches"),
                    new StudyItem("Goodbye", "gudbái", "Adiós"),
                    new StudyItem("Please", "plís", "Por favor"),
                    new StudyItem("Thank you", "zánk iu", "Gracias"),
                    new StudyItem("You are welcome", "iur uélcom", "De nada"),
                    new StudyItem("Excuse me", "ekskiús mi", "Disculpe")
            }),
            new StudyLesson("Presentaciones", "Preséntate y pregunta el nombre de otra persona.", 1, new StudyItem[]{
                    new StudyItem("My name is…", "mai néim is", "Mi nombre es…"),
                    new StudyItem("I am…", "ai am", "Yo soy…"),
                    new StudyItem("What is your name?", "uót is ior néim", "¿Cómo te llamas?"),
                    new StudyItem("Nice to meet you", "náis tu mít iu", "Mucho gusto"),
                    new StudyItem("How are you?", "jáu ar iu", "¿Cómo estás?"),
                    new StudyItem("I am fine", "ai am fáin", "Estoy bien"),
                    new StudyItem("And you?", "and iu", "¿Y tú?"),
                    new StudyItem("This is my friend", "dhis is mai frénd", "Este es mi amigo")
            }),
            new StudyLesson("Países y origen", "Pregunta y responde de dónde eres.", 2, new StudyItem[]{
                    new StudyItem("Where are you from?", "uér ar iu from", "¿De dónde eres?"),
                    new StudyItem("I am from Poland", "ai am from póuland", "Soy de Polonia"),
                    new StudyItem("France", "fráns", "Francia"),
                    new StudyItem("Spain", "spéin", "España"),
                    new StudyItem("Germany", "yérmani", "Alemania"),
                    new StudyItem("Italy", "ítali", "Italia"),
                    new StudyItem("England", "íngland", "Inglaterra"),
                    new StudyItem("I live in…", "ai liv in", "Vivo en…")
            }),
            new StudyLesson("Familia", "Vocabulario para hablar de las personas cercanas.", 5, new StudyItem[]{
                    new StudyItem("Family", "fámili", "Familia"),
                    new StudyItem("Mother", "móder", "Madre"),
                    new StudyItem("Father", "fáder", "Padre"),
                    new StudyItem("Brother", "bróder", "Hermano"),
                    new StudyItem("Sister", "síster", "Hermana"),
                    new StudyItem("Son", "san", "Hijo"),
                    new StudyItem("Daughter", "dóter", "Hija"),
                    new StudyItem("This is my family", "dhis is mai fámili", "Esta es mi familia")
            }),
            new StudyLesson("Casa y objetos", "Habitaciones y objetos que usas todos los días.", 6, new StudyItem[]{
                    new StudyItem("House", "jáus", "Casa"),
                    new StudyItem("Kitchen", "kíchen", "Cocina"),
                    new StudyItem("Bathroom", "bázrum", "Baño"),
                    new StudyItem("Bedroom", "bédrum", "Dormitorio"),
                    new StudyItem("Door", "dór", "Puerta"),
                    new StudyItem("Window", "uíndou", "Ventana"),
                    new StudyItem("Table", "téibol", "Mesa"),
                    new StudyItem("Chair", "chér", "Silla"),
                    new StudyItem("Where is the bathroom?", "uér is de bázrum", "¿Dónde está el baño?")
            }),
            new StudyLesson("Trabajo y profesiones", "Frases útiles para hablar del trabajo.", 8, new StudyItem[]{
                    new StudyItem("Work", "uérk", "Trabajo / trabajar"),
                    new StudyItem("Job", "yob", "Empleo"),
                    new StudyItem("Office", "ófis", "Oficina"),
                    new StudyItem("Manager", "mánayer", "Gerente"),
                    new StudyItem("Driver", "dráiver", "Conductor"),
                    new StudyItem("Teacher", "tícher", "Profesor"),
                    new StudyItem("I work here", "ai uérk jír", "Trabajo aquí"),
                    new StudyItem("What do you do?", "uót du iu dú", "¿A qué te dedicas?")
            }),
            new StudyLesson("Viajes y transporte", "Inglés práctico para moverte por una ciudad.", 2, new StudyItem[]{
                    new StudyItem("Airport", "érport", "Aeropuerto"),
                    new StudyItem("Train", "tréin", "Tren"),
                    new StudyItem("Bus", "bas", "Autobús"),
                    new StudyItem("Ticket", "tíket", "Billete"),
                    new StudyItem("Hotel", "joutél", "Hotel"),
                    new StudyItem("Where is the station?", "uér is de stéishon", "¿Dónde está la estación?"),
                    new StudyItem("How much is the ticket?", "jáu mach is de tíket", "¿Cuánto cuesta el billete?"),
                    new StudyItem("I need a taxi", "ai níd a táksi", "Necesito un taxi")
            }),
            new StudyLesson("Comida y restaurante", "Pide comida y entiende frases básicas en un restaurante.", 9, new StudyItem[]{
                    new StudyItem("Water", "uóter", "Agua"),
                    new StudyItem("Food", "fúd", "Comida"),
                    new StudyItem("Breakfast", "brékfast", "Desayuno"),
                    new StudyItem("Lunch", "lanch", "Almuerzo"),
                    new StudyItem("Dinner", "díner", "Cena"),
                    new StudyItem("Menu", "méniu", "Menú"),
                    new StudyItem("I would like…", "ai ud láik", "Me gustaría…"),
                    new StudyItem("The bill, please", "de bil plís", "La cuenta, por favor")
            }),
            new StudyLesson("Compras y situaciones cotidianas", "Frases rápidas para comprar y resolver necesidades básicas.", 9, new StudyItem[]{
                    new StudyItem("How much is it?", "jáu mach is it", "¿Cuánto cuesta?"),
                    new StudyItem("I like it", "ai láik it", "Me gusta"),
                    new StudyItem("I need help", "ai níd jelp", "Necesito ayuda"),
                    new StudyItem("Where can I pay?", "uér can ai péi", "¿Dónde puedo pagar?"),
                    new StudyItem("Cash", "kásh", "Efectivo"),
                    new StudyItem("Card", "kárd", "Tarjeta"),
                    new StudyItem("Open", "óupen", "Abierto"),
                    new StudyItem("Closed", "clóuzd", "Cerrado")
            })
    };

    private final PronunciationLesson[] pronunciationLessons = new PronunciationLesson[]{
            new PronunciationLesson("Sonido A corto", "La A corta inglesa es más abierta que la A española, entre A y E. Escucha y copia el audio.", new PronWord[]{
                    new PronWord("Cat", "kát", "Gato"),
                    new PronWord("Man", "mán", "Hombre"),
                    new PronWord("Dad", "dád", "Papá"),
                    new PronWord("Family", "FÁ-ma-li", "Familia")
            }),
            new PronunciationLesson("Sonido E corto", "Haz un sonido breve y relajado, parecido a la E española, sin alargarlo.", new PronWord[]{
                    new PronWord("Bed", "bed", "Cama"),
                    new PronWord("Red", "red", "Rojo"),
                    new PronWord("Pen", "pen", "Bolígrafo"),
                    new PronWord("Ten", "ten", "Diez")
            }),
            new PronunciationLesson("Sonido I corto", "La I corta inglesa es más relajada que la I española. Evita convertirla en una I larga.", new PronWord[]{
                    new PronWord("Sit", "sit", "Sentarse"),
                    new PronWord("Big", "big", "Grande"),
                    new PronWord("Fish", "fish", "Pez"),
                    new PronWord("Milk", "milk", "Leche")
            }),
            new PronunciationLesson("Sonido O corto", "Abre la boca y mantén el sonido breve. Escucha primero y luego imita.", new PronWord[]{
                    new PronWord("Hot", "jót", "Caliente"),
                    new PronWord("Dog", "dóg", "Perro"),
                    new PronWord("Box", "bóks", "Caja"),
                    new PronWord("Stop", "stóp", "Parar")
            }),
            new PronunciationLesson("Sonido U corto", "No es una U española. Es un sonido central y corto, como en cup o bus.", new PronWord[]{
                    new PronWord("Cup", "kap", "Taza"),
                    new PronWord("Bus", "bas", "Autobús"),
                    new PronWord("Sun", "san", "Sol"),
                    new PronWord("Run", "ran", "Correr")
            }),
            new PronunciationLesson("R inglesa", "La R inglesa no vibra como la R española. Lleva la lengua hacia atrás sin tocar el paladar.", new PronWord[]{
                    new PronWord("Red", "red", "Rojo"),
                    new PronWord("Right", "ráit", "Derecha / correcto"),
                    new PronWord("Road", "róud", "Carretera"),
                    new PronWord("Really", "ríali", "Realmente")
            }),
            new PronunciationLesson("Sonido W", "Redondea los labios al comenzar. La W inglesa empieza con un movimiento parecido a una U rápida.", new PronWord[]{
                    new PronWord("Water", "uóter", "Agua"),
                    new PronWord("Work", "uérk", "Trabajo"),
                    new PronWord("Window", "uíndou", "Ventana"),
                    new PronWord("Week", "uík", "Semana")
            }),
            new PronunciationLesson("TH básico", "Coloca suavemente la punta de la lengua entre los dientes y deja pasar el aire. No lo conviertas en T o D.", new PronWord[]{
                    new PronWord("Think", "thínk", "Pensar"),
                    new PronWord("Three", "thrí", "Tres"),
                    new PronWord("Thank you", "thánk iu", "Gracias"),
                    new PronWord("This", "dhis", "Esto / este")
            })
    };

    private final SoundTile[] vowelSounds = new SoundTile[]{
            new SoundTile("æ", "cat", "A abierta", "ae"),
            new SoundTile("ʌ", "cup", "A central", "uh"),
            new SoundTile("ɪ", "sit", "I corta", "i_short"),
            new SoundTile("iː", "see", "I larga", "i_long"),
            new SoundTile("e", "bed", "E corta", "e_short"),
            new SoundTile("ɑː", "car", "A larga", "a_long"),
            new SoundTile("ɒ", "hot", "O corta", "o_short"),
            new SoundTile("ɔː", "saw", "O larga", "o_long"),
            new SoundTile("ʊ", "book", "U corta", "u_short"),
            new SoundTile("uː", "food", "U larga", "u_long"),
            new SoundTile("ə", "about", "Schwa", "schwa"),
            new SoundTile("ɜː", "bird", "ER larga", "er_long"),
            new SoundTile("eɪ", "day", "Diptongo", "ay"),
            new SoundTile("aɪ", "time", "Diptongo", "ai"),
            new SoundTile("ɔɪ", "boy", "Diptongo", "oi"),
            new SoundTile("aʊ", "cow", "Diptongo", "au"),
            new SoundTile("əʊ", "go", "Diptongo", "ou")
    };

    private final SoundTile[] consonantSounds = new SoundTile[]{
            new SoundTile("b", "book", "B", "b"),
            new SoundTile("tʃ", "chair", "CH", "ch"),
            new SoundTile("d", "day", "D", "d"),
            new SoundTile("f", "fish", "F", "f"),
            new SoundTile("g", "go", "G", "g"),
            new SoundTile("h", "home", "H aspirada", "h"),
            new SoundTile("dʒ", "job", "J inglesa", "dj"),
            new SoundTile("k", "key", "K", "k"),
            new SoundTile("l", "lion", "L", "l"),
            new SoundTile("m", "moon", "M", "m"),
            new SoundTile("n", "nose", "N", "n"),
            new SoundTile("ŋ", "sing", "NG", "ng"),
            new SoundTile("p", "pig", "P", "p"),
            new SoundTile("r", "red", "R inglesa", "r"),
            new SoundTile("s", "see", "S", "s"),
            new SoundTile("ʃ", "shoe", "SH", "sh"),
            new SoundTile("t", "time", "T", "t"),
            new SoundTile("θ", "think", "TH sorda", "th_voiceless"),
            new SoundTile("ð", "this", "TH sonora", "th_voiced"),
            new SoundTile("v", "van", "V", "v"),
            new SoundTile("w", "water", "W", "w"),
            new SoundTile("j", "yes", "Y", "y"),
            new SoundTile("z", "zoo", "Z", "z"),
            new SoundTile("ʒ", "vision", "ZH", "zh")
    };

    private final SoundPair[] soundPairs = new SoundPair[]{
            new SoundPair("cat", "cut", "æ", "ae"),
            new SoundPair("bed", "bad", "e", "e_short"),
            new SoundPair("full", "fool", "ʊ", "u_short"),
            new SoundPair("sit", "seat", "ɪ", "i_short"),
            new SoundPair("hat", "hot", "æ", "ae"),
            new SoundPair("cap", "cup", "æ", "ae"),
            new SoundPair("pin", "pen", "ɪ", "i_short"),
            new SoundPair("look", "luck", "ʊ", "u_short"),
            new SoundPair("fan", "van", "f", "f"),
            new SoundPair("thin", "sin", "θ", "th_voiceless"),
            new SoundPair("three", "tree", "θ", "th_voiceless"),
            new SoundPair("rice", "rise", "s", "s"),
            new SoundPair("light", "right", "l", "l"),
            new SoundPair("west", "vest", "w", "w"),
            new SoundPair("berry", "very", "b", "b"),
            new SoundPair("coat", "goat", "k", "k"),
            new SoundPair("cheap", "jeep", "tʃ", "ch"),
            new SoundPair("think", "sink", "θ", "th_voiceless"),
            new SoundPair("day", "they", "d", "d"),
            new SoundPair("fine", "vine", "f", "f")
    };

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
        if (!prefs.contains("joined_at")) e.putLong("joined_at", System.currentTimeMillis());
        e.apply();
    }

    private void showSplash() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        playStartupVideo(() -> {
            initialSplashFinished = true;
            if (prefs.getBoolean("onboarded_v7", false)) showRoute();
            else showOnboardingUsername();
        });
    }

    private void playStartupVideo(Runnable after) {
        if (startupVideoVisible) return;
        startupVideoVisible = true;

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setTag("startup_video_overlay");
        root.addView(overlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        startupVideo = new VideoView(this);
        startupVideo.setBackgroundColor(Color.BLACK);
        overlay.addView(startupVideo, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.startup);
        startupVideo.setVideoURI(uri);
        startupVideo.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            mp.setVolume(1f, 1f);
            try {
                if (startupVideoPosition > 0) startupVideo.seekTo(startupVideoPosition);
                startupVideo.start();
            } catch (Throwable ignored) {}
        });
        startupVideo.setOnCompletionListener(mp -> finishStartupVideo(overlay, after));
        startupVideo.setOnErrorListener((mp, what, extra) -> {
            finishStartupVideo(overlay, after);
            return true;
        });
        startupVideo.start();
    }

    private void finishStartupVideo(FrameLayout overlay, Runnable after) {
        try { if (startupVideo != null) startupVideo.stopPlayback(); } catch (Throwable ignored) {}
        try { if (overlay.getParent() != null) ((ViewGroup) overlay.getParent()).removeView(overlay); } catch (Throwable ignored) {}
        startupVideo = null;
        startupVideoVisible = false;
        startupVideoPosition = 0;
        if (after != null) after.run();
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
        LinearLayout card = onboardingPage("PASO 1 DE 5", "Crea tu nombre de usuario", "Este será tu nombre dentro de la app y en tu perfil de aprendizaje.");
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
        card.addView(label("🔔  Recordatorio diario de estudio\n🔥  Aviso para proteger tu racha\n🏆  Futuras alertas de retos y logros", 16, BLUE_DARK, true), matchWrapMargin(0, 0, 0, 18));
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
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override public void onStart(String utteranceId) {
                            runOnUiThread(() -> highlightSubtitle(true));
                        }

                        @Override public void onDone(String utteranceId) {
                            runOnUiThread(() -> highlightSubtitle(false));
                        }

                        @Override public void onError(String utteranceId) {
                            runOnUiThread(() -> highlightSubtitle(false));
                        }
                    });
                    ttsReady = true;
                    if (pendingAutoSpeak != null && !pendingAutoSpeak.trim().isEmpty()) {
                        final String queued = pendingAutoSpeak;
                        pendingAutoSpeak = null;
                        runOnUiThread(() -> speak(queued, true));
                    }
                }
            });
        } catch (Throwable ignored) {
            ttsReady = false;
        }
    }

    private void speak(String text) {
        speak(text, false);
    }

    private void speak(String text, boolean automatic) {
        if (text == null || text.trim().isEmpty()) return;
        if (!ttsReady || tts == null) {
            if (automatic) {
                pendingAutoSpeak = text;
            } else {
                Toast.makeText(this, "El audio todavía se está preparando", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        try {
            pendingAutoSpeak = null;
            String utteranceId = (automatic ? "auto_" : "manual_") + System.currentTimeMillis();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } catch (Throwable ignored) {
            highlightSubtitle(false);
            if (!automatic) Toast.makeText(this, "No pude reproducir el audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void queueAutomaticAudio(String text) {
        pendingAutoSpeak = text;
        if (root == null) return;
        root.postDelayed(() -> {
            if (currentExercise != null && text != null && text.equals(currentExercise.english)) {
                speak(text, true);
            }
        }, 420);
    }

    private void highlightSubtitle(boolean playing) {
        if (subtitleText == null || subtitleCard == null) return;
        subtitleText.setTextColor(playing ? BLUE : BLUE_DARK);
        subtitleCard.animate()
                .scaleX(playing ? 1.015f : 1f)
                .scaleY(playing ? 1.015f : 1f)
                .setDuration(160)
                .start();
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
        TextView level = label("⭐  Nivel " + levelForXp(xp) + "\n" + username + " · " + xp + " XP", 14, Color.WHITE, true);
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
            startLessonFromRoute(index);
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
        nav.setPadding(dp(5), dp(7), dp(5), dp(7));
        nav.setBackground(roundRect(Color.argb(250, 255, 255, 255), 22, BORDER, 1));
        nav.setElevation(dp(12));

        String[] icons = {"🗺️", "🧠", "👄", "👤"};
        String[] labels = {"Ruta", "Practicar", "Sonidos", "Perfil"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView item = label(icons[i] + "\n" + labels[i], 13, i == selected ? BLUE : BLUE_DARK, true);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(6), dp(5), dp(6), dp(5));
            if (i == selected) item.setBackground(roundRect(Color.rgb(232, 246, 255), 16, Color.TRANSPARENT, 0));
            item.setOnClickListener(v -> {
                if (idx == 0) showRoute();
                else if (idx == 1) showPractice();
                else if (idx == 2) showSounds();
                else showProfile();
            });
            nav.addView(item, new LinearLayout.LayoutParams(0, dp(60), 1f));
        }
        return nav;
    }

    private void showPractice() {
        currentLesson = -1;
        subtitleText = null;
        subtitleCard = null;
        setRootWithArt(R.drawable.london_route_art);

        LinearLayout page = pageColumn();
        page.addView(buildHud());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(2), dp(10), dp(2), dp(28));
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout hero = cardColumn();
        hero.setPadding(dp(22), dp(18), dp(22), dp(18));
        hero.addView(label("📚  CENTRO DE PRÁCTICA", 13, BLUE, true));
        TextView title = label("Aprende, escucha y repite", 28, BLUE_DARK, true);
        title.setPadding(0, dp(4), 0, 0);
        hero.addView(title);
        TextView copy = label("Todo el contenido está integrado en Bluelingo. No necesitas abrir la web para estudiar pronunciación, vocabulario o lecciones.", 16, MUTED, false);
        copy.setPadding(0, dp(7), 0, dp(14));
        hero.addView(copy);

        TextView continueBtn = actionButton("▶  CONTINUAR PRÁCTICA", LIME, BLUE_DARK);
        continueBtn.setOnClickListener(v -> startLessonFromPractice(firstUnlockedPracticeLesson()));
        hero.addView(continueBtn);
        body.addView(hero, matchWrapMargin(0, 0, 0, 12));

        LinearLayout selector = new LinearLayout(this);
        selector.setOrientation(LinearLayout.HORIZONTAL);
        selector.setPadding(dp(5), dp(5), dp(5), dp(5));
        selector.setBackground(roundRect(Color.argb(245, 255, 255, 255), 20, BORDER, 1));

        TextView lessonsTab = label("📘  LECCIONES", 14, practiceSection == 0 ? Color.WHITE : BLUE_DARK, true);
        lessonsTab.setGravity(Gravity.CENTER);
        lessonsTab.setPadding(dp(8), dp(12), dp(8), dp(12));
        lessonsTab.setBackground(roundRect(practiceSection == 0 ? BLUE : Color.TRANSPARENT, 16, Color.TRANSPARENT, 0));
        lessonsTab.setOnClickListener(v -> {
            practiceSection = 0;
            showPractice();
        });
        selector.addView(lessonsTab, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView pronunciationTab = label("🗣️  PRONUNCIACIÓN", 14, practiceSection == 1 ? Color.WHITE : BLUE_DARK, true);
        pronunciationTab.setGravity(Gravity.CENTER);
        pronunciationTab.setPadding(dp(8), dp(12), dp(8), dp(12));
        pronunciationTab.setBackground(roundRect(practiceSection == 1 ? GREEN : Color.TRANSPARENT, 16, Color.TRANSPARENT, 0));
        pronunciationTab.setOnClickListener(v -> {
            practiceSection = 1;
            showPractice();
        });
        selector.addView(pronunciationTab, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        body.addView(selector, matchWrapMargin(0, 0, 0, 14));

        if (practiceSection == 0) {
            body.addView(label("Todas las lecciones", 24, BLUE_DARK, true), matchWrapMargin(4, 0, 0, 4));
            TextView hint = label("Abre una lección para estudiar el vocabulario, escuchar cada palabra y luego practicarla con ejercicios.", 15, MUTED, false);
            body.addView(hint, matchWrapMargin(4, 0, 4, 12));
            for (int i = 0; i < studyLessons.length; i++) {
                body.addView(studyLessonCard(i), matchWrapMargin(0, 0, 0, 12));
            }
        } else {
            body.addView(label("Pronunciación paso a paso", 24, BLUE_DARK, true), matchWrapMargin(4, 0, 0, 4));
            TextView hint = label("Escucha a velocidad normal, lenta o repite tres veces. Todo el audio usa el motor de voz nativo de Android.", 15, MUTED, false);
            body.addView(hint, matchWrapMargin(4, 0, 4, 12));
            for (int i = 0; i < pronunciationLessons.length; i++) {
                body.addView(pronunciationLessonCard(i), matchWrapMargin(0, 0, 0, 12));
            }
        }

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
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

    private View studyLessonCard(int index) {
        StudyLesson lesson = studyLessons[index];
        LinearLayout card = cardColumn();
        card.setPadding(dp(20), dp(17), dp(20), dp(17));

        TextView badge = pill("LECCIÓN " + (index + 1), Color.rgb(232, 246, 255), BLUE_DARK);
        badge.setGravity(Gravity.CENTER);
        card.addView(badge, wrapMargin(0, 0, 0, 10));

        TextView icon = label(index < 2 ? "🗣️" : "📘", 36, BLUE, true);
        icon.setPadding(0, 0, 0, dp(5));
        card.addView(icon);

        TextView title = label(lesson.title, 23, BLUE_DARK, true);
        title.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        card.addView(title);

        TextView subtitle = label(lesson.subtitle, 15, MUTED, false);
        subtitle.setPadding(0, dp(5), 0, dp(12));
        card.addView(subtitle);

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setPadding(dp(14), dp(10), dp(14), dp(10));
        preview.setBackground(roundRect(Color.rgb(244, 249, 253), 16, BORDER, 1));
        int previewCount = Math.min(3, lesson.items.length);
        for (int i = 0; i < previewCount; i++) {
            StudyItem item = lesson.items[i];
            TextView row = label("•  " + item.english + "  =  " + item.spanish, 15, i == 0 ? BLUE_DARK : MUTED, i == 0);
            row.setPadding(0, dp(3), 0, dp(3));
            preview.addView(row);
        }
        card.addView(preview);

        TextView open = label("Abrir lección  →", 17, GREEN, true);
        open.setPadding(0, dp(13), 0, 0);
        card.addView(open);

        card.setOnClickListener(v -> {
            pulse(card);
            showStudyLesson(index);
        });
        return card;
    }

    private void showStudyLesson(int index) {
        if (index < 0 || index >= studyLessons.length) return;
        StudyLesson lesson = studyLessons[index];
        currentLesson = -1;
        subtitleText = null;
        subtitleCard = null;
        setRootWithArt(R.drawable.london_route_art);

        LinearLayout page = pageColumn();
        page.addView(nativeBackBar("Lección " + (index + 1), () -> showPractice()));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(4), dp(10), dp(4), dp(28));
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout intro = cardColumn();
        intro.setPadding(dp(22), dp(20), dp(22), dp(20));
        intro.addView(label("INGLÉS BÁSICO · LECCIÓN " + (index + 1), 13, GREEN, true));
        TextView title = label(lesson.title, 29, BLUE_DARK, true);
        title.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        title.setPadding(0, dp(6), 0, 0);
        intro.addView(title);
        TextView desc = label(lesson.subtitle + " Toca 🔊 para escuchar y repite en voz alta.", 16, MUTED, false);
        desc.setPadding(0, dp(8), 0, 0);
        intro.addView(desc);
        body.addView(intro, matchWrapMargin(0, 0, 0, 12));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(14), dp(12), dp(14), dp(12));
        header.setBackground(roundRect(Color.rgb(7, 35, 61), 14, Color.TRANSPARENT, 0));
        TextView h1 = label("INGLÉS", 13, Color.WHITE, true);
        TextView h2 = label("PRONUNCIACIÓN", 13, Color.WHITE, true);
        TextView h3 = label("ESPAÑOL", 13, Color.WHITE, true);
        header.addView(h1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.05f));
        header.addView(h2, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.15f));
        header.addView(h3, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        body.addView(header);

        for (int i = 0; i < lesson.items.length; i++) {
            final StudyItem item = lesson.items[i];
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(13), dp(8), dp(13));
            int rowBg = (i % 2 == 0) ? Color.argb(246, 244, 249, 253) : Color.argb(248, 255, 255, 255);
            row.setBackground(roundRect(rowBg, 0, Color.rgb(225, 234, 241), 1));

            TextView english = label(item.english, 16, Color.rgb(40, 58, 75), true);
            english.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
            TextView pronunciation = label("(" + item.pronunciation + ")", 15, RED, false);
            TextView spanish = label(item.spanish, 15, MUTED, false);
            TextView speaker = label("🔊", 19, BLUE, true);
            speaker.setGravity(Gravity.CENTER);
            speaker.setContentDescription("Escuchar " + item.english);
            speaker.setOnClickListener(v -> speakAtRate(item.english, 0.90f, 1));

            LinearLayout first = new LinearLayout(this);
            first.setOrientation(LinearLayout.HORIZONTAL);
            first.setGravity(Gravity.CENTER_VERTICAL);
            first.addView(english, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            first.addView(speaker, new LinearLayout.LayoutParams(dp(36), dp(36)));
            row.addView(first, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.15f));
            row.addView(pronunciation, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.05f));
            row.addView(spanish, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            row.setOnClickListener(v -> speakAtRate(item.english, 0.90f, 1));
            body.addView(row);
        }

        LinearLayout practice = cardColumn();
        practice.setPadding(dp(18), dp(18), dp(18), dp(18));
        TextView practiceTitle = label("¿Listo para probarte?", 20, BLUE_DARK, true);
        practice.addView(practiceTitle);
        TextView practiceCopy = label("Pasa de estudiar a responder ejercicios interactivos de esta temática.", 14, MUTED, false);
        practiceCopy.setPadding(0, dp(5), 0, dp(12));
        practice.addView(practiceCopy);
        TextView btn = actionButton("▶  PRACTICAR ESTA LECCIÓN", LIME, BLUE_DARK);
        btn.setOnClickListener(v -> startLessonFromPractice(Math.max(0, Math.min(lessons.length - 1, lesson.routeLessonIndex))));
        practice.addView(btn);
        body.addView(practice, matchWrapMargin(0, 14, 0, 0));

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(page);
    }

    private View pronunciationLessonCard(int index) {
        PronunciationLesson lesson = pronunciationLessons[index];
        LinearLayout card = cardColumn();
        card.setPadding(dp(20), dp(17), dp(20), dp(17));

        TextView badge = pill("PRONUNCIACIÓN · LECCIÓN " + (index + 1), Color.rgb(235, 250, 239), Color.rgb(19, 139, 74));
        card.addView(badge, wrapMargin(0, 0, 0, 10));
        card.addView(label("🗣️", 36, BLUE, true));

        TextView title = label(lesson.title, 23, BLUE_DARK, true);
        title.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        title.setPadding(0, dp(4), 0, 0);
        card.addView(title);

        TextView tip = label(lesson.tip, 15, MUTED, false);
        tip.setPadding(0, dp(6), 0, dp(12));
        card.addView(tip);

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < Math.min(3, lesson.words.length); i++) {
            PronWord w = lesson.words[i];
            TextView mini = label(w.english + "\n" + w.pronunciation, 13, i == 0 ? BLUE_DARK : MUTED, i == 0);
            mini.setGravity(Gravity.CENTER);
            mini.setPadding(dp(5), dp(9), dp(5), dp(9));
            mini.setBackground(roundRect(Color.rgb(244, 249, 253), 12, BORDER, 1));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) lp.leftMargin = dp(6);
            preview.addView(mini, lp);
        }
        card.addView(preview);
        TextView open = label("Practicar sonidos  →", 17, GREEN, true);
        open.setPadding(0, dp(13), 0, 0);
        card.addView(open);

        card.setOnClickListener(v -> {
            pulse(card);
            showPronunciationLesson(index);
        });
        return card;
    }

    private void showPronunciationLesson(int index) {
        if (index < 0 || index >= pronunciationLessons.length) return;
        PronunciationLesson lesson = pronunciationLessons[index];
        currentLesson = -1;
        subtitleText = null;
        subtitleCard = null;
        setRootWithArt(R.drawable.london_route_art);

        LinearLayout page = pageColumn();
        page.addView(nativeBackBar("Pronunciación", () -> showPractice()));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(4), dp(10), dp(4), dp(30));
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout intro = cardColumn();
        intro.setPadding(dp(22), dp(19), dp(22), dp(19));
        intro.addView(label("PRONUNCIACIÓN FÁCIL · LECCIÓN " + (index + 1), 13, GREEN, true));
        TextView title = label(lesson.title, 29, BLUE_DARK, true);
        title.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        title.setPadding(0, dp(7), 0, 0);
        intro.addView(title);

        LinearLayout tipCard = new LinearLayout(this);
        tipCard.setOrientation(LinearLayout.HORIZONTAL);
        tipCard.setGravity(Gravity.TOP);
        tipCard.setPadding(dp(14), dp(13), dp(14), dp(13));
        tipCard.setBackground(roundRect(Color.rgb(244, 249, 253), 16, Color.rgb(200, 222, 238), 1));
        TextView bulb = label("💡", 22, GREEN, true);
        TextView tip = label(lesson.tip, 15, MUTED, false);
        tip.setPadding(dp(8), 0, 0, 0);
        tipCard.addView(bulb);
        tipCard.addView(tip, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        intro.addView(tipCard, matchWrapMargin(0, 13, 0, 0));
        body.addView(intro, matchWrapMargin(0, 0, 0, 12));

        for (PronWord word : lesson.words) {
            body.addView(pronunciationWordCard(word), matchWrapMargin(0, 0, 0, 12));
        }

        LinearLayout coach = cardColumn();
        coach.setPadding(dp(18), dp(17), dp(18), dp(17));
        coach.addView(label("🎯 Método Bluelingo", 18, BLUE_DARK, true));
        TextView coachText = label("1. Escucha normal.  2. Escucha lento.  3. Repite tres veces mirando la palabra.  4. Intenta decirla sin mirar.", 14, MUTED, false);
        coachText.setPadding(0, dp(6), 0, 0);
        coach.addView(coachText);
        body.addView(coach);

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(page);
    }

    private View pronunciationWordCard(PronWord word) {
        LinearLayout card = cardColumn();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView english = label(word.english, 25, Color.rgb(29, 46, 63), true);
        english.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        card.addView(english);

        TextView pronunciation = label(word.pronunciation, 17, RED, true);
        pronunciation.setPadding(0, dp(4), 0, 0);
        card.addView(pronunciation);

        TextView spanish = label(word.spanish, 15, MUTED, false);
        spanish.setPadding(0, dp(4), 0, dp(12));
        card.addView(spanish);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        TextView listen = smallAudioButton("🔊 Escuchar", BLUE, Color.WHITE);
        listen.setOnClickListener(v -> speakAtRate(word.english, 0.90f, 1));
        actions.addView(listen, new LinearLayout.LayoutParams(0, dp(52), 1f));

        TextView slow = smallAudioButton("🐢 Lento", Color.rgb(64, 157, 225), Color.WHITE);
        slow.setOnClickListener(v -> speakAtRate(word.english, 0.62f, 1));
        LinearLayout.LayoutParams slowLp = new LinearLayout.LayoutParams(0, dp(52), 1f);
        slowLp.leftMargin = dp(7);
        actions.addView(slow, slowLp);

        TextView repeat = smallAudioButton("🔁 ×3", GREEN, Color.WHITE);
        repeat.setOnClickListener(v -> speakAtRate(word.english, 0.82f, 3));
        LinearLayout.LayoutParams repeatLp = new LinearLayout.LayoutParams(0, dp(52), 1f);
        repeatLp.leftMargin = dp(7);
        actions.addView(repeat, repeatLp);

        card.addView(actions);
        return card;
    }

    private TextView smallAudioButton(String text, int bg, int fg) {
        TextView b = label(text, 14, fg, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(7), dp(8), dp(7), dp(8));
        b.setBackground(roundRect(bg, 15, Color.argb(70, 0, 0, 0), 1));
        b.setElevation(dp(3));
        return b;
    }

    private View nativeBackBar(String title, Runnable backAction) {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(2), dp(4), dp(2), dp(6));

        TextView back = pill("‹", BLUE, Color.WHITE);
        back.setTextSize(27);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> backAction.run());
        top.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView titleView = label(title, 20, Color.WHITE, true);
        titleView.setShadowLayer(5f, 0, 2, Color.argb(110, 0, 0, 0));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.leftMargin = dp(12);
        top.addView(titleView, tp);
        return top;
    }

    private void speakAtRate(String text, float rate, int repetitions) {
        if (text == null || text.trim().isEmpty()) return;
        if (!ttsReady || tts == null) {
            Toast.makeText(this, "El audio todavía se está preparando", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            tts.stop();
            tts.setSpeechRate(rate);
            for (int i = 0; i < Math.max(1, repetitions); i++) {
                String id = "practice_" + System.currentTimeMillis() + "_" + i;
                tts.speak(text, i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null, id);
            }
            tts.setSpeechRate(0.90f);
        } catch (Throwable t) {
            Toast.makeText(this, "No pude reproducir el audio", Toast.LENGTH_SHORT).show();
            try { tts.setSpeechRate(0.90f); } catch (Throwable ignored) {}
        }
    }

    private void showSounds() {
        inSoundQuiz = false;
        currentLesson = -1;
        subtitleText = null;
        subtitleCard = null;
        setRootWithArt(R.drawable.london_route_art);

        LinearLayout page = pageColumn();
        page.addView(buildHud());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(2), dp(10), dp(2), dp(28));
        scroll.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout hero = cardColumn();
        hero.setPadding(dp(22), dp(20), dp(22), dp(20));
        hero.addView(label("👄  ENTRENADOR DE SONIDOS", 13, BLUE, true));
        TextView title = label("¡Mejora tu pronunciación del inglés!", 28, BLUE_DARK, true);
        title.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        title.setPadding(0, dp(5), 0, 0);
        hero.addView(title);
        TextView copy = label("Entrena tu oído, distingue sonidos parecidos y aprende a pronunciar palabras con más claridad.", 16, MUTED, false);
        copy.setPadding(0, dp(8), 0, dp(16));
        hero.addView(copy);
        TextView start = actionButton("▶  EMPEZAR LECCIÓN", Color.rgb(72, 184, 238), Color.WHITE);
        start.setOnClickListener(v -> startSoundQuiz());
        hero.addView(start);
        body.addView(hero, matchWrapMargin(0, 0, 0, 14));

        LinearLayout progressCard = cardColumn();
        progressCard.setPadding(dp(18), dp(15), dp(18), dp(15));
        int attempts = prefs.getInt("sound_attempts", 0);
        int best = prefs.getInt("sound_best", 0);
        progressCard.addView(label("🎧 Tu progreso de pronunciación", 19, BLUE_DARK, true));
        TextView stats = label(attempts + " ejercicios completados · Mejor sesión: " + best + "/10", 14, MUTED, false);
        stats.setPadding(0, dp(5), 0, 0);
        progressCard.addView(stats);
        body.addView(progressCard, matchWrapMargin(0, 0, 0, 16));

        body.addView(label("Vocales", 25, BLUE_DARK, true), matchWrapMargin(4, 0, 0, 10));
        addSoundGrid(body, vowelSounds);

        body.addView(label("Consonantes", 25, BLUE_DARK, true), matchWrapMargin(4, 22, 0, 10));
        addSoundGrid(body, consonantSounds);

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(2));
        root.addView(page);
    }

    private void addSoundGrid(LinearLayout body, SoundTile[] sounds) {
        for (int i = 0; i < sounds.length; i += 3) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 3; j++) {
                int pos = i + j;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(126), 1f);
                if (j > 0) lp.leftMargin = dp(7);
                if (pos < sounds.length) row.addView(soundTileView(sounds[pos]), lp);
                else {
                    View spacer = new View(this);
                    row.addView(spacer, lp);
                }
            }
            body.addView(row, matchWrapMargin(0, 0, 0, 8));
        }
    }

    private View soundTileView(SoundTile sound) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(8), dp(12), dp(8), dp(9));
        card.setBackground(roundRect(Color.argb(248, 255, 255, 255), 20, Color.rgb(169, 211, 236), 2));
        card.setElevation(dp(3));

        TextView ipa = label(sound.symbol, 24, BLUE_DARK, true);
        ipa.setGravity(Gravity.CENTER);
        card.addView(ipa);
        TextView example = label(sound.example, 13, MUTED, false);
        example.setGravity(Gravity.CENTER);
        example.setPadding(0, dp(2), 0, dp(7));
        card.addView(example);

        ProgressBar p = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        p.setMax(5);
        p.setProgress(Math.min(5, prefs.getInt("sound_skill_" + sound.key, 0)));
        p.getProgressDrawable().setTint(GREEN);
        card.addView(p, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(9)));
        card.setOnClickListener(v -> {
            pulse(card);
            speakAtRate(sound.example, 0.82f, 1);
            int skill = Math.min(5, prefs.getInt("sound_skill_" + sound.key, 0) + 1);
            prefs.edit().putInt("sound_skill_" + sound.key, skill).apply();
            p.setProgress(skill);
        });
        return card;
    }

    private void startSoundQuiz() {
        soundQuizQuestion = 0;
        soundQuizCorrect = 0;
        soundQuizQueue.clear();
        soundQuizQueue.addAll(Arrays.asList(soundPairs));
        Collections.shuffle(soundQuizQueue, random);
        while (soundQuizQueue.size() > 10) soundQuizQueue.remove(soundQuizQueue.size() - 1);
        showSoundQuizQuestion();
    }

    private void showSoundQuizQuestion() {
        inSoundQuiz = true;
        if (soundQuizQuestion >= soundQuizQueue.size()) {
            showSoundQuizResult();
            return;
        }
        currentSoundPair = soundQuizQueue.get(soundQuizQuestion);
        selectedSoundAnswer = "";
        setRootWithArt(R.drawable.london_exercise_art);

        LinearLayout page = pageColumn();
        page.addView(nativeBackBar("Entrenamiento de sonidos", () -> showSounds()));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(8), dp(12), dp(8), dp(14));
        page.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(soundQuizQueue.size());
        progress.setProgress(soundQuizQuestion + 1);
        progress.getProgressDrawable().setTint(LIME);
        content.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(16)));

        TextView counter = label("SONIDO " + (soundQuizQuestion + 1) + " DE " + soundQuizQueue.size(), 12, BLUE, true);
        counter.setPadding(0, dp(10), 0, 0);
        content.addView(counter);
        TextView title = label("Elige lo que escuchas", 28, BLUE_DARK, true);
        title.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        title.setPadding(0, dp(7), 0, dp(14));
        content.addView(title);

        TextView speaker = label("🔊", 54, BLUE_DARK, true);
        speaker.setGravity(Gravity.CENTER);
        speaker.setContentDescription("Repetir audio");
        speaker.setBackground(roundRect(Color.rgb(72, 184, 238), 28, Color.rgb(34, 145, 211), 2));
        speaker.setElevation(dp(7));
        speaker.setOnClickListener(v -> speakAtRate(currentSoundPair.correct, 0.82f, 1));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(148), dp(148));
        sp.gravity = Gravity.CENTER_HORIZONTAL;
        sp.bottomMargin = dp(24);
        content.addView(speaker, sp);

        boolean correctFirst = random.nextBoolean();
        String a = correctFirst ? currentSoundPair.correct : currentSoundPair.distractor;
        String b = correctFirst ? currentSoundPair.distractor : currentSoundPair.correct;

        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.HORIZONTAL);
        soundOptionA = soundChoice(a);
        soundOptionB = soundChoice(b);
        soundOptionA.setOnClickListener(v -> chooseSoundAnswer(soundOptionA, soundOptionB));
        soundOptionB.setOnClickListener(v -> chooseSoundAnswer(soundOptionB, soundOptionA));
        LinearLayout.LayoutParams opA = new LinearLayout.LayoutParams(0, dp(170), 1f);
        opA.rightMargin = dp(7);
        LinearLayout.LayoutParams opB = new LinearLayout.LayoutParams(0, dp(170), 1f);
        opB.leftMargin = dp(7);
        options.addView(soundOptionA, opA);
        options.addView(soundOptionB, opB);
        content.addView(options);

        soundFeedback = label("", 15, MUTED, true);
        soundFeedback.setGravity(Gravity.CENTER);
        soundFeedback.setPadding(0, dp(12), 0, dp(8));
        content.addView(soundFeedback);

        soundCheckButton = actionButton("COMPROBAR", LIME, BLUE_DARK);
        soundCheckButton.setOnClickListener(v -> checkSoundAnswer());
        page.addView(soundCheckButton, matchWrapMargin(8, 0, 8, 12));
        root.addView(page);

        root.postDelayed(() -> {
            if (currentSoundPair != null) speakAtRate(currentSoundPair.correct, 0.82f, 1);
        }, 430);
    }

    private TextView soundChoice(String word) {
        TextView choice = label(word, 25, BLUE_DARK, true);
        choice.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        choice.setGravity(Gravity.CENTER);
        choice.setBackground(roundRect(Color.argb(249, 255, 255, 255), 22, BORDER, 2));
        choice.setElevation(dp(4));
        return choice;
    }

    private void chooseSoundAnswer(TextView selected, TextView other) {
        selectedSoundAnswer = selected.getText().toString();
        selected.setTextColor(BLUE_DARK);
        selected.setBackground(roundRect(Color.rgb(224, 245, 255), 22, Color.rgb(72, 184, 238), 3));
        other.setTextColor(BLUE_DARK);
        other.setBackground(roundRect(Color.argb(249, 255, 255, 255), 22, BORDER, 2));
    }

    private void checkSoundAnswer() {
        if (currentSoundPair == null) return;
        if ("SIGUIENTE".contentEquals(soundCheckButton.getText())) {
            soundQuizQuestion++;
            showSoundQuizQuestion();
            return;
        }
        if (selectedSoundAnswer.isEmpty()) {
            Toast.makeText(this, "Elige una respuesta primero", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean correct = selectedSoundAnswer.equalsIgnoreCase(currentSoundPair.correct);
        if (correct) {
            soundQuizCorrect++;
            soundFeedback.setText("✓ ¡Correcto!  /" + currentSoundPair.soundSymbol + "/");
            soundFeedback.setTextColor(GREEN);
            int skill = Math.min(5, prefs.getInt("sound_skill_" + currentSoundPair.skillKey, 0) + 1);
            prefs.edit()
                    .putInt("sound_skill_" + currentSoundPair.skillKey, skill)
                    .putInt("xp", prefs.getInt("xp", 0) + 5)
                    .apply();
        } else {
            soundFeedback.setText("✕ Era “" + currentSoundPair.correct + "”. Escúchalo otra vez.");
            soundFeedback.setTextColor(RED);
            speakAtRate(currentSoundPair.correct, 0.68f, 1);
        }
        prefs.edit().putInt("sound_attempts", prefs.getInt("sound_attempts", 0) + 1).apply();
        soundCheckButton.setText("SIGUIENTE");
        soundCheckButton.setBackground(roundRect(correct ? GREEN : Color.rgb(72, 184, 238), 18, Color.argb(80, 0, 0, 0), 1));
        soundCheckButton.setTextColor(Color.WHITE);
    }

    private void showSoundQuizResult() {
        inSoundQuiz = true;
        int previousBest = prefs.getInt("sound_best", 0);
        if (soundQuizCorrect > previousBest) prefs.edit().putInt("sound_best", soundQuizCorrect).apply();
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setGravity(Gravity.CENTER);
        body.setPadding(dp(18), dp(20), dp(18), dp(20));
        page.addView(body, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout card = cardColumn();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(24), dp(28), dp(24), dp(28));
        card.addView(label(soundQuizCorrect >= 8 ? "🏆" : "🎧", 62, BLUE_DARK, true));
        TextView title = label(soundQuizCorrect >= 8 ? "¡Gran oído!" : "Sesión completada", 30, BLUE_DARK, true);
        title.setGravity(Gravity.CENTER);
        card.addView(title);
        TextView score = label(soundQuizCorrect + " de " + soundQuizQueue.size() + " respuestas correctas", 18, BLUE, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, dp(8), 0, dp(4));
        card.addView(score);
        TextView xp = label("+" + (soundQuizCorrect * 5) + " XP por respuestas correctas", 15, GREEN, true);
        xp.setGravity(Gravity.CENTER);
        card.addView(xp);
        TextView again = actionButton("REPETIR ENTRENAMIENTO", LIME, BLUE_DARK);
        again.setOnClickListener(v -> startSoundQuiz());
        card.addView(again, matchWrapMargin(0, 20, 0, 9));
        TextView back = actionButton("VOLVER A SONIDOS", Color.WHITE, BLUE);
        back.setBackground(roundRect(Color.WHITE, 18, BLUE, 2));
        back.setOnClickListener(v -> showSounds());
        card.addView(back);
        body.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(page);
    }

    private int completedCount() {
        int c = 0;
        for (int i = 0; i < lessons.length; i++) if (prefs.getBoolean("lesson_" + i, false)) c++;
        return c;
    }

    private void showProfile() {
        currentLesson = -1;
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(2), dp(8), dp(2), dp(28));
        scroll.addView(body);

        LinearLayout header = cardColumn();
        header.setPadding(dp(20), dp(16), dp(20), dp(22));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView profileName = label(prefs.getString("username", "@Usuario").replace("@", ""), 26, BLUE_DARK, true);
        profileName.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        top.addView(profileName, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView share = label("↗", 28, BLUE_DARK, true);
        share.setGravity(Gravity.CENTER);
        share.setOnClickListener(v -> shareProfile());
        top.addView(share, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView settings = label("⚙", 28, BLUE_DARK, true);
        settings.setGravity(Gravity.CENTER);
        settings.setOnClickListener(v -> showEditProfile());
        top.addView(settings, new LinearLayout.LayoutParams(dp(48), dp(48)));
        header.addView(top);

        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setBackground(circleDrawable(Color.WHITE, BLUE, 4));
        avatar.setClipToOutline(true);
        String avatarUri = prefs.getString("avatar_uri", "");
        if (!avatarUri.isEmpty()) {
            try { avatar.setImageURI(Uri.parse(avatarUri)); }
            catch (Throwable ignored) { avatar.setImageResource(R.drawable.app_icon_512); }
        } else avatar.setImageResource(R.drawable.app_icon_512);
        avatar.setOnClickListener(v -> chooseAvatar());
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dp(142), dp(142));
        avatarLp.gravity = Gravity.CENTER_HORIZONTAL;
        avatarLp.topMargin = dp(12);
        avatarLp.bottomMargin = dp(10);
        header.addView(avatar, avatarLp);

        TextView handle = label(prefs.getString("username", "@Usuario"), 20, BLUE, true);
        handle.setGravity(Gravity.CENTER);
        header.addView(handle);
        TextView meta = label(prefs.getString("english_level", "Principiante") + " · " + prefs.getString("goal", "Hablar") + " · " + prefs.getInt("daily_minutes", 10) + " min/día", 14, MUTED, false);
        meta.setGravity(Gravity.CENTER);
        meta.setPadding(0, dp(5), 0, dp(3));
        header.addView(meta);
        String joinedYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date(prefs.getLong("joined_at", System.currentTimeMillis())));
        TextView joined = label("Miembro desde " + joinedYear, 12, MUTED, false);
        joined.setGravity(Gravity.CENTER);
        joined.setPadding(0, 0, 0, dp(12));
        header.addView(joined);
        TextView changeAvatar = actionButton("📷  CAMBIAR AVATAR", Color.WHITE, BLUE);
        changeAvatar.setBackground(roundRect(Color.WHITE, 16, BLUE, 2));
        changeAvatar.setOnClickListener(v -> chooseAvatar());
        header.addView(changeAvatar);
        body.addView(header, matchWrapMargin(0, 0, 0, 12));

        LinearLayout stats = cardColumn();
        stats.setPadding(dp(16), dp(17), dp(16), dp(17));
        stats.addView(label("RESUMEN", 13, MUTED, true));
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(profileStat("🔥", prefs.getInt("streak", 0) + " días", "Racha"), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row1.addView(profileStat("⚡", prefs.getInt("xp", 0) + " XP", "Experiencia"), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        stats.addView(row1, matchWrapMargin(0, 10, 0, 4));
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(profileStat("📘", completedCount() + "/" + lessons.length, "Lecciones"), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row2.addView(profileStat("👄", prefs.getInt("sound_attempts", 0) + "", "Sonidos practicados"), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        stats.addView(row2);
        body.addView(stats, matchWrapMargin(0, 0, 0, 12));

        LinearLayout levelCard = cardColumn();
        levelCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        int xp = prefs.getInt("xp", 0);
        int level = levelForXp(xp);
        int base = (level - 1) * 150;
        int progressXp = xp - base;
        levelCard.addView(label("⭐ Nivel " + level, 21, BLUE_DARK, true));
        TextView next = label(Math.max(0, 150 - progressXp) + " XP para el siguiente nivel", 14, MUTED, false);
        next.setPadding(0, dp(4), 0, dp(8));
        levelCard.addView(next);
        ProgressBar levelProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        levelProgress.setMax(150);
        levelProgress.setProgress(Math.min(150, progressXp));
        levelProgress.getProgressDrawable().setTint(GREEN);
        levelCard.addView(levelProgress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(13)));
        body.addView(levelCard, matchWrapMargin(0, 0, 0, 12));

        LinearLayout achievements = cardColumn();
        achievements.setPadding(dp(18), dp(16), dp(18), dp(18));
        achievements.addView(label("🏅 LOGROS", 19, BLUE_DARK, true));
        TextView achCopy = label("Desbloquea insignias aprendiendo y practicando sonidos.", 14, MUTED, false);
        achCopy.setPadding(0, dp(4), 0, dp(12));
        achievements.addView(achCopy);
        LinearLayout ar1 = new LinearLayout(this);
        ar1.setOrientation(LinearLayout.HORIZONTAL);
        ar1.addView(achievementTile("🚀", "Primer paso", prefs.getBoolean("lesson_0", false)), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); gap.leftMargin = dp(8);
        ar1.addView(achievementTile("⚡", "100 XP", xp >= 100), gap);
        achievements.addView(ar1);
        LinearLayout ar2 = new LinearLayout(this);
        ar2.setOrientation(LinearLayout.HORIZONTAL);
        ar2.addView(achievementTile("👄", "Buen oído", prefs.getInt("sound_attempts", 0) >= 10), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams gap2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); gap2.leftMargin = dp(8);
        ar2.addView(achievementTile("🔥", "7 días", prefs.getInt("streak", 0) >= 7), gap2);
        achievements.addView(ar2, matchWrapMargin(0, 8, 0, 0));
        body.addView(achievements, matchWrapMargin(0, 0, 0, 12));

        LinearLayout notificationCard = cardColumn();
        notificationCard.setPadding(dp(18), dp(16), dp(18), dp(16));
        notificationCard.addView(label("🔔 Recordatorios", 19, BLUE_DARK, true));
        boolean enabled = prefs.getBoolean("notifications_enabled", false);
        TextView ns = label(enabled ? "Recordatorio diario activo aproximadamente a las 19:00." : "Las notificaciones están desactivadas.", 14, MUTED, false);
        ns.setPadding(0, dp(5), 0, dp(11));
        notificationCard.addView(ns);
        TextView notify = actionButton(enabled ? "DESACTIVAR" : "ACTIVAR NOTIFICACIONES", enabled ? Color.WHITE : LIME, enabled ? RED : BLUE_DARK);
        if (enabled) notify.setBackground(roundRect(Color.WHITE, 16, RED, 2));
        notify.setOnClickListener(v -> {
            if (prefs.getBoolean("notifications_enabled", false)) {
                cancelDailyReminder();
                prefs.edit().putBoolean("notifications_enabled", false).apply();
                showProfile();
            } else requestNotificationPermission(false);
        });
        notificationCard.addView(notify);
        body.addView(notificationCard, matchWrapMargin(0, 0, 0, 12));

        TextView refill = actionButton("❤️  RECARGAR CORAZONES", Color.WHITE, BLUE);
        refill.setBackground(roundRect(Color.WHITE, 18, BLUE, 2));
        refill.setOnClickListener(v -> {
            prefs.edit().putInt("hearts", 5).apply();
            Toast.makeText(this, "Corazones restaurados", Toast.LENGTH_SHORT).show();
            showProfile();
        });
        body.addView(refill);

        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(bottomNav(3));
        root.addView(page);
    }

    private View profileStat(String emoji, String value, String labelText) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView valueView = label(emoji + "  " + value, 20, BLUE_DARK, true);
        box.addView(valueView);
        TextView labelView = label(labelText, 12, MUTED, false);
        labelView.setPadding(dp(28), dp(2), 0, 0);
        box.addView(labelView);
        return box;
    }

    private View achievementTile(String emoji, String title, boolean unlocked) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(9), dp(13), dp(9), dp(13));
        card.setBackground(roundRect(unlocked ? Color.rgb(238, 252, 238) : Color.rgb(243, 247, 250), 18, unlocked ? GREEN : BORDER, 1));
        TextView icon = label(emoji, 34, unlocked ? BLUE_DARK : Color.rgb(150, 164, 176), true);
        icon.setGravity(Gravity.CENTER);
        card.addView(icon);
        TextView t = label(title, 13, unlocked ? BLUE_DARK : MUTED, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(4), 0, 0);
        card.addView(t);
        return card;
    }

    private GradientDrawable circleDrawable(int fill, int stroke, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        g.setStroke(dp(strokeDp), stroke);
        return g;
    }

    private void chooseAvatar() {
        try {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(pick, REQ_AVATAR);
        } catch (Throwable t) {
            Toast.makeText(this, "No pude abrir la galería", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareProfile() {
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, "Estoy aprendiendo inglés con Bluelingo. " + prefs.getString("username", "@Usuario") + " · " + prefs.getInt("xp", 0) + " XP · " + completedCount() + " lecciones completadas.");
            startActivity(Intent.createChooser(send, "Compartir perfil"));
        } catch (Throwable ignored) {}
    }

    private void showEditProfile() {
        setRootWithArt(R.drawable.london_route_art);
        LinearLayout page = pageColumn();
        page.addView(nativeBackBar("Editar perfil", () -> showProfile()));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(5), dp(12), dp(5), dp(24));
        scroll.addView(body);

        LinearLayout card = cardColumn();
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.addView(label("Tu nombre de usuario", 18, BLUE_DARK, true));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(prefs.getString("username", "@Usuario").replace("@", ""));
        input.setTextColor(BLUE_DARK);
        input.setTextSize(18);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(roundRect(Color.WHITE, 14, BORDER, 2));
        card.addView(input, matchWrapMargin(0, 8, 0, 14));
        TextView save = actionButton("GUARDAR CAMBIOS", LIME, BLUE_DARK);
        save.setOnClickListener(v -> {
            String raw = input.getText().toString().trim().replaceAll("[^A-Za-z0-9_]", "");
            if (raw.length() < 3) {
                Toast.makeText(this, "Usa al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString("username", "@" + raw).apply();
            showProfile();
        });
        card.addView(save);
        body.addView(card);
        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(page);
    }

    private LinearLayout pageColumn() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(10), dp(14), dp(0));
        page.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return page;
    }

    private void startLessonFromRoute(int lessonIndex) {
        lessonReturnTarget = RETURN_ROUTE;
        startLesson(lessonIndex);
    }

    private void startLessonFromPractice(int lessonIndex) {
        lessonReturnTarget = RETURN_PRACTICE;
        startLesson(lessonIndex);
    }

    private void startLesson(int lessonIndex) {
        if (lessonIndex < 0 || lessonIndex >= lessons.length) return;
        currentLesson = lessonIndex;
        currentQuestion = 0;
        showExercise();
    }

    private void returnAfterLesson() {
        currentLesson = -1;
        currentQuestion = 0;
        if (lessonReturnTarget == RETURN_PRACTICE) {
            practiceSection = 0;
            showPractice();
        } else {
            showRoute();
        }
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
        close.setOnClickListener(v -> returnAfterLesson());
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

        LinearLayout speech = new LinearLayout(this);
        speech.setOrientation(LinearLayout.HORIZONTAL);
        speech.setGravity(Gravity.CENTER_VERTICAL);
        speech.setPadding(dp(14), dp(16), dp(16), dp(16));
        speech.setBackground(roundRect(Color.argb(250, 255, 255, 255), 24, Color.rgb(151, 207, 245), 2));
        speech.setElevation(dp(6));
        subtitleCard = speech;

        TextView speaker = label("🔊", 28, Color.WHITE, true);
        speaker.setGravity(Gravity.CENTER);
        speaker.setContentDescription("Reproducir audio");
        speaker.setBackground(roundRect(BLUE, 22, BLUE_DARK, 1));
        speaker.setOnClickListener(v -> {
            pulse(speaker);
            speak(currentExercise.english);
        });
        speech.addView(speaker, new LinearLayout.LayoutParams(dp(58), dp(58)));

        subtitleText = label(currentExercise.english, 26, BLUE_DARK, true);
        subtitleText.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        subtitleText.setGravity(Gravity.CENTER);
        subtitleText.setContentDescription("Subtítulo en inglés");
        subtitleText.setPadding(dp(12), 0, dp(4), 0);
        LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        speech.addView(subtitleText, subtitleLp);

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

        // V7.1: cada ejercicio pronuncia la frase automáticamente al entrar.
        queueAutomaticAudio(currentExercise.english);
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
        String destination = lessonReturnTarget == RETURN_PRACTICE ? "VOLVER A PRACTICAR" : "VOLVER A LA RUTA";
        TextView btn = actionButton(destination, LIME, BLUE_DARK);
        btn.setOnClickListener(v -> returnAfterLesson());
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
    protected void onPause() {
        if (startupVideoVisible && startupVideo != null) {
            try {
                startupVideoPosition = startupVideo.getCurrentPosition();
                startupVideo.pause();
            } catch (Throwable ignored) {}
        } else if (initialSplashFinished) {
            backgroundStartedAt = System.currentTimeMillis();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startupVideoVisible && startupVideo != null) {
            try {
                if (startupVideoPosition > 0) startupVideo.seekTo(startupVideoPosition);
                startupVideo.start();
            } catch (Throwable ignored) {}
            return;
        }
        if (initialSplashFinished && backgroundStartedAt > 0L) {
            long away = System.currentTimeMillis() - backgroundStartedAt;
            backgroundStartedAt = 0L;
            if (away >= REENTRY_SPLASH_MS && root != null) {
                playStartupVideo(null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Throwable ignored) {}
            prefs.edit().putString("avatar_uri", uri.toString()).apply();
            showProfile();
        }
    }

    @Override
    public void onBackPressed() {
        if (inSoundQuiz) {
            showSounds();
            return;
        }
        if (currentLesson >= 0) {
            returnAfterLesson();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        try {
            if (tts != null) { tts.stop(); tts.shutdown(); }
            if (recognizer != null) recognizer.destroy();
        } catch (Throwable ignored) {}
        super.onDestroy();
    }

    private static class StudyItem {
        final String english;
        final String pronunciation;
        final String spanish;

        StudyItem(String english, String pronunciation, String spanish) {
            this.english = english;
            this.pronunciation = pronunciation;
            this.spanish = spanish;
        }
    }

    private static class StudyLesson {
        final String title;
        final String subtitle;
        final int routeLessonIndex;
        final StudyItem[] items;

        StudyLesson(String title, String subtitle, int routeLessonIndex, StudyItem[] items) {
            this.title = title;
            this.subtitle = subtitle;
            this.routeLessonIndex = routeLessonIndex;
            this.items = items;
        }
    }

    private static class PronWord {
        final String english;
        final String pronunciation;
        final String spanish;

        PronWord(String english, String pronunciation, String spanish) {
            this.english = english;
            this.pronunciation = pronunciation;
            this.spanish = spanish;
        }
    }

    private static class PronunciationLesson {
        final String title;
        final String tip;
        final PronWord[] words;

        PronunciationLesson(String title, String tip, PronWord[] words) {
            this.title = title;
            this.tip = tip;
            this.words = words;
        }
    }

    private static class SoundTile {
        final String symbol;
        final String example;
        final String description;
        final String key;

        SoundTile(String symbol, String example, String description, String key) {
            this.symbol = symbol;
            this.example = example;
            this.description = description;
            this.key = key;
        }
    }

    private static class SoundPair {
        final String correct;
        final String distractor;
        final String soundSymbol;
        final String skillKey;

        SoundPair(String correct, String distractor, String soundSymbol, String skillKey) {
            this.correct = correct;
            this.distractor = distractor;
            this.soundSymbol = soundSymbol;
            this.skillKey = skillKey;
        }
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
