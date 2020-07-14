package com.example.jayde.a4ease;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by jayde on 03/06/2018.
 */

public class Google extends AppCompatActivity {

    private WebView webView;
    private String urlentered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Intent intent = getIntent();
        urlentered = intent.getStringExtra("url");
        webView = (WebView) findViewById(R.id.webView1);
        webView.loadUrl(urlentered);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
    }
}

