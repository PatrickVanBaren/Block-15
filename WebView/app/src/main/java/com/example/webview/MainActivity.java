package com.example.webview;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final Random mRandom = new Random();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private WebView mWebView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.webView);
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(final WebView view, final String url, final String message,
                                     final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("WebApp")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                result.confirm();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.addJavascriptInterface(new WebAppInterface(), "Android");

        if (savedInstanceState == null) mWebView.loadUrl("file:///android_asset/index.html");
        else mWebView.restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    private void evaluateJs(final String jsString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(jsString, null);
        } else {
            mWebView.loadUrl("javascript:" + jsString);
        }
    }

    private void evaluateJs2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript("document.getElementById('someText').value", value -> {
                final Intent intent = new Intent(MainActivity.this, TextActivity.class);
                intent.putExtra("textString", value);
                startActivity(intent);
            });
        } else {
            mWebView.loadUrl("javascript:" + "document.getElementById('msg').innerHTML");
        }
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void showToast(final String toast) {
            mHandler.post(() -> evaluateJs("document.getElementById('msg').innerHTML = '" + mRandom.nextLong() + "'"));
            Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void showText() {
            mHandler.post(() -> evaluateJs2());
        }
    }
}
