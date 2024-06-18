package com.csu.web2app;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private BottomNavigationView bottomNavigationView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web_view_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

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

        // 隐藏 ActionBar，顶部导航栏
        getSupportActionBar().hide();

        webView.setWebViewClient(new WebViewController() {
            @Override
            public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
                // 取消连接
                handler.cancel();
            }
        });

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

        // 显示自定义对话框
        MyCustomDialogFragment dialog = new MyCustomDialogFragment();
        dialog.show(getSupportFragmentManager(), "CustomDialogFragment");

        // 检查当前屏幕方向，并根据方向显示或隐藏底部导航栏
        adjustBottomNavigationView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 每当屏幕方向改变时调用
        adjustBottomNavigationView();
    }

    private void adjustBottomNavigationView() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时隐藏底部导航栏
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            // 竖屏时显示底部导航栏
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
