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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

    private static final int BLUE = Color.rgb(13,115,217);
    private static final int BLUE_DARK = Color.rgb(7,63,141);
    private static final int GREEN = Color.rgb(24,184,75);
    private static final int BG = Color.rgb(244,250,255);
    private static final int TEXT = Color.rgb(23,50,77);
    private static final int MUTED = Color.rgb(97,113,132);

    private FrameLayout contentHost;
    private ProgressBar topProgress;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<PostItem> posts = new ArrayList<>();
    private SharedPreferences prefs;
    private String currentSection = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        getWindow().setStatusBarColor(BLUE_DARK);
        initTextToSpeech();
        buildShell();
        showHome();
        loadFeed(false);
        requestNotificationPermissionIfNeeded();
        if (prefs.getBoolean(KEY_REMINDER, true)) scheduleDailyReminder();
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));
        header.setBackgroundColor(BLUE_DARK);

        android.widget.ImageView logo = new android.widget.ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(48), dp(48));
        logoLp.setMargins(0, 0, dp(10), 0);
        header.addView(logo, logoLp);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("Aprende gratis inglés");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        TextView subtitle = new TextView(this);
        subtitle.setText("Tu curso dinámico desde cero");
        subtitle.setTextColor(Color.rgb(215,235,255));
        subtitle.setTextSize(12);
        titles.addView(title);
        titles.addView(subtitle);
        header.addView(titles, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(header);

        topProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        topProgress.setMax(100);
        topProgress.setIndeterminate(false);
        topProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        topProgress.setVisibility(View.GONE);
        root.addView(topProgress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        contentHost = new FrameLayout(this);
        root.addView(contentHost, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(dp(5), dp(7), dp(5), dp(7));
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(7));
        addBottomButton(nav, "⌂\nInicio", () -> showHome());
        addBottomButton(nav, "☰\nLecciones", () -> showLessons(posts));
        addBottomButton(nav, "♥\nFavoritos", this::showFavorites);
        addBottomButton(nav, "✓\nProgreso", this::showProgress);
        root.addView(nav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void addBottomButton(LinearLayout nav, String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setTextColor(BLUE_DARK);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setOnClickListener(v -> action.run());
        nav.addView(b, new LinearLayout.LayoutParams(0, dp(58), 1f));
    }

    private void showHome() {
        currentSection = "home";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(16), dp(16), dp(16), dp(28));
        scroll.addView(box);

        TextView hello = heading("Aprende un poco cada día", 26, BLUE_DARK);
        box.addView(hello);
        TextView intro = body("Tus nuevas lecciones de Blogger aparecen aquí automáticamente. Guarda favoritas, marca lo aprendido y continúa donde lo dejaste.");
        box.addView(intro);

        box.addView(spacer(12));
        box.addView(progressCard());
        box.addView(spacer(12));

        PostItem next = getNextPost();
        if (next != null) {
            LinearLayout continueCard = card();
            continueCard.addView(label("CONTINUAR APRENDIENDO", GREEN));
            continueCard.addView(heading(next.title, 20, BLUE_DARK));
            continueCard.addView(body(stripHtml(next.summary, 150)));
            continueCard.addView(primaryButton("▶ Continuar lección", () -> openPost(next)));
            box.addView(continueCard);
        }

        box.addView(spacer(16));
        LinearLayout rowTitle = horizontal();
        rowTitle.addView(heading("Últimas lecciones", 22, BLUE_DARK), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button refresh = smallButton("Actualizar", () -> loadFeed(true));
        rowTitle.addView(refresh);
        box.addView(rowTitle);

        int limit = Math.min(6, posts.size());
        if (limit == 0) {
            box.addView(body("Cargando las lecciones del sitio…"));
        } else {
            for (int i = 0; i < limit; i++) box.addView(postCard(posts.get(i)));
        }

        box.addView(spacer(12));
        LinearLayout practice = card();
        practice.addView(label("PRÁCTICA RÁPIDA", BLUE));
        practice.addView(heading("10 preguntas para reforzar lo aprendido", 20, BLUE_DARK));
        practice.addView(body("Un mini ejercicio dentro de la app. Tu cerebro agradecerá el trabajo. Eventualmente."));
        practice.addView(primaryButton("🧠 Empezar práctica", this::showQuiz));
        box.addView(practice);

        box.addView(spacer(12));
        LinearLayout tools = card();
        tools.addView(label("RECURSOS", GREEN));
        tools.addView(primaryButton("🗣️ Curso de pronunciación", () -> openUrl(HOME + "p/pronunciacion-facil.html")));
        tools.addView(secondaryButton("📥 Guías PDF", () -> openUrl(HOME + "p/guias-pdf.html")));
        box.addView(tools);

        setContent(scroll);
    }

    private View progressCard() {
        LinearLayout c = card();
        c.addView(label("MI PROGRESO", BLUE));
        int total = Math.max(posts.size(), 1);
        int completed = completedSet().size();
        int percent = posts.isEmpty() ? 0 : Math.min(100, Math.round(completed * 100f / total));
        TextView pct = heading(percent + "% completado", 24, BLUE_DARK);
        c.addView(pct);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(percent);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        c.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        TextView small = body(completed + " de " + (posts.isEmpty() ? "…" : total) + " lecciones marcadas como completadas");
        c.addView(small);
        return c;
    }

    private void showLessons(List<PostItem> source) {
        currentSection = "lessons";
        LinearLayout root = verticalBox();
        root.setPadding(dp(14), dp(14), dp(14), dp(18));

        EditText search = new EditText(this);
        search.setHint("Buscar: verbos, familia, colores…");
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        search.setPadding(dp(14), dp(10), dp(14), dp(10));
        search.setBackground(rounded(Color.WHITE, Color.rgb(220,231,242), 16));
        root.addView(search, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = verticalBox();
        list.setPadding(0, dp(10), 0, dp(28));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        renderLessonList(list, source);
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim().toLowerCase(Locale.ROOT);
                List<PostItem> filtered = new ArrayList<>();
                for (PostItem p : posts) {
                    String hay = (p.title + " " + p.summary + " " + joinLabels(p.labels, " ")).toLowerCase(Locale.ROOT);
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
        List<PostItem> f = new ArrayList<>();
        for (PostItem p : posts) if (fav.contains(p.url)) f.add(p);

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        box.addView(heading("Tus favoritos", 25, BLUE_DARK));
        box.addView(body("Las lecciones que guardes aparecen aquí y siguen disponibles en la lista cacheada aunque pierdas conexión."));
        if (f.isEmpty()) box.addView(body("Todavía no has guardado ninguna lección."));
        for (PostItem p : f) box.addView(postCard(p));
        scroll.addView(box);
        setContent(scroll);
    }

    private void showProgress() {
        currentSection = "progress";
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        box.addView(heading("Mi progreso", 25, BLUE_DARK));
        box.addView(progressCard());
        box.addView(spacer(12));

        LinearLayout reminder = card();
        reminder.addView(label("RECORDATORIO", GREEN));
        reminder.addView(heading("5 minutos de inglés al día", 20, BLUE_DARK));
        reminder.addView(body("La app puede recordarte cada día que practiques. Nada agresivo, solo una pequeña presión civilizada."));
        boolean enabled = prefs.getBoolean(KEY_REMINDER, true);
        Button toggle = secondaryButton(enabled ? "🔔 Recordatorio activado" : "🔕 Activar recordatorio", () -> toggleReminder());
        reminder.addView(toggle);
        box.addView(reminder);

        box.addView(spacer(12));
        box.addView(heading("Lecciones completadas", 20, BLUE_DARK));
        Set<String> done = completedSet();
        if (done.isEmpty()) box.addView(body("Marca una lección como completada para empezar a registrar tu avance."));
        for (PostItem p : posts) if (done.contains(p.url)) box.addView(postCard(p));
        scroll.addView(box);
        setContent(scroll);
    }

    private void showQuiz() {
        final String[][] q = {
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

        ScrollView scroll = new ScrollView(this);
        LinearLayout box = verticalBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(28));
        box.addView(heading("Práctica rápida", 25, BLUE_DARK));
        TextView score = body("Responde las 10 preguntas.");
        box.addView(score);

        final int[] correct = {0};
        final int[] answered = {0};
        for (int i = 0; i < q.length; i++) {
            LinearLayout c = card();
            c.addView(heading((i + 1) + ". " + q[i][0], 17, TEXT));
            for (int a = 1; a <= 3; a++) {
                String option = q[i][a];
                Button b = secondaryButton(option, () -> {});
                b.setOnClickListener(v -> {
                    if (!v.isEnabled()) return;
                    ViewGroup parent = (ViewGroup) v.getParent();
                    for (int k = 0; k < parent.getChildCount(); k++) {
                        View child = parent.getChildAt(k);
                        if (child instanceof Button) child.setEnabled(false);
                    }
                    answered[0]++;
                    if (option.equals(q[(Integer) c.getTag()][4])) {
                        correct[0]++;
                        ((Button) v).setText("✓ " + option);
                    } else {
                        ((Button) v).setText("✗ " + option);
                    }
                    score.setText("Puntuación: " + correct[0] + " / " + answered[0]);
                    if (answered[0] == q.length) {
                        Toast.makeText(this, "Resultado: " + correct[0] + "/10", Toast.LENGTH_LONG).show();
                    }
                });
                c.addView(b);
            }
            c.setTag(i);
            box.addView(c);
        }
        scroll.addView(box);
        setContent(scroll);
    }

    private LinearLayout postCard(PostItem p) {
        LinearLayout c = card();
        LinearLayout top = horizontal();
        TextView title = heading(p.title, 19, BLUE_DARK);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button fav = smallButton(favoriteSet().contains(p.url) ? "♥" : "♡", () -> toggleFavorite(p));
        top.addView(fav, new LinearLayout.LayoutParams(dp(48), dp(44)));
        c.addView(top);
        String s = stripHtml(p.summary, 170);
        if (!s.isEmpty()) c.addView(body(s));
        if (!p.labels.isEmpty()) c.addView(label(joinLabels(p.labels.subList(0, Math.min(2, p.labels.size())), " · "), GREEN));
        LinearLayout actions = horizontal();
        actions.addView(primaryButton("Abrir", () -> openPost(p)), new LinearLayout.LayoutParams(0, dp(46), 1f));
        Button done = secondaryButton(completedSet().contains(p.url) ? "✓ Hecha" : "Marcar ✓", () -> toggleCompleted(p));
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        doneLp.setMargins(dp(8), 0, 0, 0);
        actions.addView(done, doneLp);
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
        toolbar.addView(smallButton("← Volver", this::showHome));
        TextView t = heading(p.title, 16, BLUE_DARK);
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
        actions.addView(secondaryButton("🗣 Pronunciación", () -> w.loadUrl(HOME + "p/pronunciacion-facil.html")), new LinearLayout.LayoutParams(0, dp(46), 1f));
        Button done = primaryButton(completedSet().contains(p.url) ? "✓ Completada" : "Completar ✓", () -> toggleCompleted(p));
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
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
        s.setUserAgentString(s.getUserAgentString() + " AprendeGratisInglesApp/2.0");
        w.addJavascriptInterface(new NativeTtsBridge(w), "AndroidTTS");
        w.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
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
        @JavascriptInterface public void stop() { runOnUiThread(() -> { if (textToSpeech != null) textToSpeech.stop(); }); }
    }

    private WebView pendingTtsWebView;

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
        runOnUiThread(() -> target.evaluateJavascript("window.__agiTtsDone&&window.__agiTtsDone('" + id.replace("'", "") + "');", null));
    }

    private void loadFeed(boolean forceToast) {
        topProgress.setVisibility(View.VISIBLE);
        topProgress.setIndeterminate(true);
        executor.execute(() -> {
            String json = fetchText(FEED);
            boolean fresh = json != null && !json.trim().isEmpty();
            if (!fresh) json = prefs.getString(KEY_FEED_CACHE, "");
            final String result = json == null ? "" : json;
            final boolean fromNetwork = fresh;
            runOnUiThread(() -> {
                topProgress.setIndeterminate(false);
                topProgress.setVisibility(View.GONE);
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
            conn.setRequestProperty("User-Agent", "AprendeGratisInglesApp/2.0");
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
        if ("lessons".equals(currentSection)) showLessons(posts);
        else if ("favorites".equals(currentSection)) showFavorites();
        else if ("progress".equals(currentSection)) showProgress();
        else if ("home".equals(currentSection)) showHome();
    }

    private PostItem getNextPost() {
        String last = prefs.getString(KEY_LAST_OPENED, "");
        if (!last.isEmpty()) {
            for (PostItem p : posts) if (last.equals(p.url) && !completedSet().contains(p.url)) return p;
        }
        for (PostItem p : posts) if (!completedSet().contains(p.url)) return p;
        return posts.isEmpty() ? null : posts.get(0);
    }

    private Set<String> favoriteSet() { return new HashSet<>(prefs.getStringSet(KEY_FAVORITES, Collections.emptySet())); }
    private Set<String> completedSet() { return new HashSet<>(prefs.getStringSet(KEY_COMPLETED, Collections.emptySet())); }

    private void toggleFavorite(PostItem p) {
        Set<String> set = favoriteSet();
        if (set.contains(p.url)) set.remove(p.url); else set.add(p.url);
        prefs.edit().putStringSet(KEY_FAVORITES, set).apply();
        Toast.makeText(this, set.contains(p.url) ? "Guardada en favoritos" : "Eliminada de favoritos", Toast.LENGTH_SHORT).show();
        refreshCurrentSection();
    }

    private void toggleCompleted(PostItem p) {
        Set<String> set = completedSet();
        if (set.contains(p.url)) set.remove(p.url); else set.add(p.url);
        prefs.edit().putStringSet(KEY_COMPLETED, set).apply();
        Toast.makeText(this, set.contains(p.url) ? "Lección completada ✓" : "Lección marcada como pendiente", Toast.LENGTH_SHORT).show();
        refreshCurrentSection();
    }

    private void toggleReminder() {
        boolean enabled = !prefs.getBoolean(KEY_REMINDER, true);
        prefs.edit().putBoolean(KEY_REMINDER, enabled).apply();
        if (enabled) scheduleDailyReminder(); else cancelReminder();
        showProgress();
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

    private void setContent(View view) {
        contentHost.removeAllViews();
        contentHost.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LinearLayout verticalBox() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout horizontal() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }

    private LinearLayout card() {
        LinearLayout c = verticalBox();
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(rounded(Color.WHITE, Color.rgb(220,231,242), 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(8));
        c.setLayoutParams(lp);
        c.setElevation(dp(2));
        return c;
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill); g.setCornerRadius(dp(radius)); g.setStroke(dp(1), stroke); return g;
    }

    private TextView heading(String text, int size, int color) {
        TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color); v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(3), 0, dp(6)); return v;
    }
    private TextView body(String text) {
        TextView v = new TextView(this); v.setText(text); v.setTextSize(15); v.setTextColor(MUTED); v.setLineSpacing(0, 1.15f); v.setPadding(0, dp(2), 0, dp(8)); return v;
    }
    private TextView label(String text, int color) {
        TextView v = new TextView(this); v.setText(text.toUpperCase(Locale.ROOT)); v.setTextSize(11); v.setTextColor(color); v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(2), 0, dp(6)); return v;
    }

    private Button primaryButton(String text, Runnable action) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setTextSize(14); b.setTypeface(Typeface.DEFAULT_BOLD); b.setBackground(rounded(BLUE, BLUE, 13)); b.setOnClickListener(v -> action.run()); return b;
    }
    private Button secondaryButton(String text, Runnable action) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextColor(BLUE_DARK); b.setTextSize(14); b.setBackground(rounded(Color.WHITE, Color.rgb(205,221,237), 13)); b.setOnClickListener(v -> action.run()); return b;
    }
    private Button smallButton(String text, Runnable action) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextColor(BLUE_DARK); b.setTextSize(12); b.setBackground(rounded(Color.WHITE, Color.rgb(220,231,242), 11)); b.setOnClickListener(v -> action.run()); return b;
    }
    private View spacer(int h) { View v = new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h))); return v; }

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

    @Override
    public void onBackPressed() {
        if (!"home".equals(currentSection)) showHome(); else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); }
        super.onDestroy();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
