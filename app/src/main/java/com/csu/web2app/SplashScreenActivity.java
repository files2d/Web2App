package com.csu.web2app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class SplashScreenActivity extends AppCompatActivity {
Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // 允许主线程进行网络操作
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // 获取 ImageView 控件
        ImageView imageView = findViewById(R.id.splash_image_view);

        // 使用 Glide 加载网络图片
//        String imageUrl = "https://img.wssss.org/file/c22290568d74c491f8450.png";

        String imageUrl = getYourImageUrl(); // 假设这是您获取图片URL的方法

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 加载网络图片
            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);
        } else {
            // URL为空或无效，不执行加载操作，ImageView将显示其背景（即默认图片）
        }

        // 隐藏 ActionBar
        getSupportActionBar().hide();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    private String getYourImageUrl() {
        // 返回图片URL，或者null/空字符串如果没有URL
        String urlString = "https://json.098975.xyz/app?id=2";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(jsonBuilder.toString());
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                return jsonObject.getString("image");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 返回null表示没有获取到有效的URL
    }
}