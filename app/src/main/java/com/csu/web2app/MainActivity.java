package com.csu.web2app;

import android.annotation.SuppressLint;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏和横幅导航栏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.web_view_home);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMinimumFontSize(1);
        webSettings.setMinimumLogicalFontSize(1);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSaveFormData(false);


        // 隐藏 ActionBar
        getSupportActionBar().hide();


        webView.setWebViewClient(new WebViewController() {
            @Override
            public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
                // 取消连接
                handler.cancel();
            }
        });


        //webView.loadUrl(getString(R.string.web_url));
        webView.loadUrl(BuildConfig.web_url);

        bottomNavigationView.setOnItemSelectedListener((item) -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                webView.loadUrl(BuildConfig.web_url);
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                webView.loadUrl("https://www.baidu.com/");
                return true;
            } else if (itemId == R.id.nav_notifications) {
                webView.loadUrl("https://bing.com/");
                return true;
            }
            return false;
        });


        Button showDialogButton = findViewById(R.id.show_dialog_button);
        showDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCustomDialogFragment dialog = new MyCustomDialogFragment();
                dialog.show(getSupportFragmentManager(), "CustomDialogFragment");
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}