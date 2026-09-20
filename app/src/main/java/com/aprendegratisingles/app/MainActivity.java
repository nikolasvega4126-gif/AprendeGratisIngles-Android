package com.aprendegratisingles.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String HOME = "https://www.aprendegratisingles.com/";
    private static final int BLUE = Color.rgb(13,115,217);
    private static final int BLUE_DARK = Color.rgb(7,63,141);
    private static final int GREEN = Color.rgb(24,184,75);

    private WebView webView;
    private ProgressBar progressBar;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BLUE_DARK);
        initTextToSpeech();
        buildUi();
        configureWebView();
        webView.loadUrl(HOME);
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
                textToSpeech.setSpeechRate(0.88f);

                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) { }

                    @Override
                    public void onDone(String utteranceId) {
                        notifyWebSpeechEnded(utteranceId);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        notifyWebSpeechEnded(utteranceId);
                    }
                });
            }
        });
    }

    private void notifyWebSpeechEnded(String utteranceId) {
        if (webView == null || utteranceId == null) return;
        runOnUiThread(() -> webView.evaluateJavascript(
                "window.__agiTtsDone && window.__agiTtsDone('" + utteranceId + "');",
                null));
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Aprende gratis inglés");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(18), dp(12), dp(18), dp(12));
        title.setBackgroundColor(BLUE_DARK);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        root.addView(progressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(3)));

        webView = new WebView(this);
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(webView, webParams);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(5), dp(7), dp(5), dp(7));
        nav.setBackgroundColor(Color.WHITE);

        addNavButton(nav, "Inicio", HOME, BLUE);
        addNavButton(nav, "Lecciones", HOME + "p/lecciones.html", BLUE);
        addNavButton(nav, "Pronunciación", HOME + "p/pronunciacion-facil.html", GREEN);
        addNavButton(nav, "PDF", HOME + "p/guias-pdf.html", BLUE);

        root.addView(nav, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void addNavButton(LinearLayout parent, String text, String url, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setPadding(dp(5), 0, dp(5), 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(12));
        button.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        parent.addView(button, lp);
        button.setOnClickListener(v -> webView.loadUrl(url));
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " AprendeGratisInglesApp/1.1");

        webView.addJavascriptInterface(new NativeTtsBridge(), "AndroidTTS");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if (scheme.equals("http") || scheme.equals("https")) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "No se pudo abrir este enlace", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectNativeSpeechSynthesis();
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No se pudo iniciar la descarga", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void injectNativeSpeechSynthesis() {
        String js = "(function(){"
                + "if(!window.AndroidTTS){return;}"
                + "var pending={};var seq=0;"
                + "window.SpeechSynthesisUtterance=function(text){this.text=text||'';this.lang='en-US';this.rate=1;this.voice=null;this.onend=null;this.onerror=null;};"
                + "window.__agiTtsDone=function(id){var u=pending[id];if(!u)return;delete pending[id];if(typeof u.onend==='function'){try{u.onend({utterance:u});}catch(e){}}};"
                + "window.speechSynthesis={"
                + "getVoices:function(){return [{name:'Android English',lang:'en-US',default:true}];},"
                + "cancel:function(){pending={};AndroidTTS.stop();},"
                + "speak:function(u){if(!u)return;var id='agi_'+(++seq);pending[id]=u;AndroidTTS.speak(String(u.text||''),Number(u.rate||0.88),String(u.lang||'en-US'),id);},"
                + "pause:function(){},resume:function(){}"
                + "};"
                + "})();";
        webView.evaluateJavascript(js, null);
    }

    private class NativeTtsBridge {
        @JavascriptInterface
        public void speak(String text, double rate, String lang, String utteranceId) {
            runOnUiThread(() -> {
                if (!ttsReady || textToSpeech == null) {
                    Toast.makeText(MainActivity.this,
                            "El audio todavía se está preparando. Inténtalo de nuevo en un momento.",
                            Toast.LENGTH_SHORT).show();
                    notifyWebSpeechEnded(utteranceId);
                    return;
                }

                Locale locale = Locale.US;
                if (lang != null && lang.toLowerCase(Locale.ROOT).startsWith("en-gb")) {
                    locale = Locale.UK;
                }
                textToSpeech.setLanguage(locale);

                float safeRate = (float) Math.max(0.3, Math.min(1.5, rate));
                textToSpeech.setSpeechRate(safeRate);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(() -> {
                if (textToSpeech != null) textToSpeech.stop();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
