package com.csu.web2app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyCustomDialogFragment extends DialogFragment {

    private static final String PREF_NAME = "MyCustomDialogFragment";
    private static final String PREF_KEY_PASSWORD = "saved_password";
    private static final String PREF_KEY_EXPIRY = "password_expiry";
    private class PopupData {
        String title;
        String content;
        boolean popupFlag;
        boolean popupEnforce;
        boolean buttonFlag;
        JSONArray buttons;
        boolean kamiFlag;
        String kamiTitle;
        String kamiContent;
        JSONArray appPasswords;

        public PopupData(String title, String content, boolean popupFlag, boolean popupEnforce, boolean buttonFlag, JSONArray buttons, boolean kamiFlag, String kamiTitle, String kamiContent, JSONArray appPasswords) {
            this.title = title;
            this.content = content;
            this.popupFlag = popupFlag;
            this.popupEnforce = popupEnforce;
            this.buttonFlag = buttonFlag;
            this.buttons = buttons;
            this.kamiFlag = kamiFlag;
            this.kamiTitle = kamiTitle;
            this.kamiContent = kamiContent;
            this.appPasswords = appPasswords;
        }
    }

    private PopupData fetchData(String urlString) {
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

            JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
            String title = jsonObject.getString("popup_title");
            String content = jsonObject.getString("popup_content");
            boolean popupFlag = jsonObject.getString("popup_flag").equals("1");
            boolean popupEnforce = jsonObject.getString("popup_enforce").equals("1");
            boolean buttonFlag = jsonObject.getString("popup_button_flag").equals("1");
            JSONArray buttons = jsonObject.getJSONArray("app_button");
            boolean kamiFlag = jsonObject.getString("kami_flag").equals("1");
            String kamiTitle = jsonObject.getString("kami_title");
            String kamiContent = jsonObject.getString("kami_content");
            JSONArray appPasswords = jsonObject.getJSONArray("app_pass");

            return new PopupData(title, content, popupFlag, popupEnforce, buttonFlag, buttons, kamiFlag, kamiTitle, kamiContent, appPasswords);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查是否存在已保存的密码和过期日期
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedPassword = sharedPreferences.getString(PREF_KEY_PASSWORD, null);
        String passwordExpiry = sharedPreferences.getString(PREF_KEY_EXPIRY, null);

        if (savedPassword != null && passwordExpiry != null) {
            // 检查密码是否已过期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date expiryDate = sdf.parse(passwordExpiry);
                Date currentDate = new Date();
                if (!currentDate.after(expiryDate)) {
                    // 密码未过期,跳过密码输入对话框
                    // 在这里执行相应的操作,例如直接启动应用主界面
                    Toast.makeText(getActivity(), "密码有效时间: " + sdf.format(expiryDate), Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 在后台线程中执行网络请求
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                String urlString = "https://json.098975.xyz/api/records/apps/1?join=app_button&join=app_pass";
                PopupData popupData = fetchData(urlString);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (popupData != null) {
                            showDialog(popupData);
                        } else {
                            // Handle the error case
                            showDialog(new PopupData("Error", "Failed to load data.", false, false, false, new JSONArray(), false, "Error", "Failed to load data.", new JSONArray()));
                        }
                    }
                });
            }
        });
    }

    private void showDialog(PopupData popupData) {
        if (popupData.kamiFlag) {
            // 添加弹窗卡密相关配置
            final EditText inputPassword = new EditText(getActivity());
            inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            inputPassword.setHint("请输入密码");

            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(popupData.kamiTitle)
                    .setMessage(popupData.kamiContent)
                    .setView(inputPassword)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String enteredPassword = inputPassword.getText().toString().trim();
                            boolean passwordMatched = false;
                            boolean passwordExpired = false;

                            // 检查输入的密码是否匹配并且未过期
                            for (int i = 0; i < popupData.appPasswords.length(); i++) {
                                try {
                                    JSONObject appPasswordObj = popupData.appPasswords.getJSONObject(i);
                                    String password = appPasswordObj.getString("app_password");
                                    String passwordExpiry = appPasswordObj.getString("password_expiry");

                                    if (password.equals(enteredPassword)) {
                                        passwordMatched = true;

                                        // 检查密码是否过期
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        Date expiryDate = sdf.parse(passwordExpiry);
                                        Date currentDate = new Date();
                                        if (currentDate.after(expiryDate)) {
                                            passwordExpired = true;
                                        }

                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (passwordMatched && !passwordExpired) {

                                // 保存密码和过期日期到SharedPreferences
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(PREF_KEY_PASSWORD, enteredPassword);
                                for (int i = 0; i < popupData.appPasswords.length(); i++) {
                                    try {
                                        JSONObject appPasswordObj = popupData.appPasswords.getJSONObject(i);
                                        String password = appPasswordObj.getString("app_password");
                                        if (password.equals(enteredPassword)) {
                                            String passwordExpiry = appPasswordObj.getString("password_expiry");
                                            editor.putString(PREF_KEY_EXPIRY, passwordExpiry);

                                            // 显示过期日期的 Toast
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                            Date expiryDate = sdf.parse(passwordExpiry);
                                            Toast.makeText(getActivity(), "密码有效时间: " + sdf.format(expiryDate), Toast.LENGTH_LONG).show();

                                            break;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                editor.apply();

                                dialog.dismiss(); // 关闭对话框

                            } else {
                                // 密码不匹配或已过期，显示 Toast 提示
                                String toastMessage;
                                if (!passwordMatched) {
                                    toastMessage = "密码不正确";
                                } else {
                                    toastMessage = "密码已过期";
                                }
                                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();

                                // 强制显示弹窗
                                showDialog(popupData);
                            }
                        }
                    })
                    .setNegativeButton("获取", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/winsight"));
                            startActivity(browserIntent);
                            // 这里不要关闭对话框
                            showDialog(popupData);
                        }
                    })
                    .setCancelable(false);

            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(popupData.title)
                    .setMessage(popupData.content);

            if (popupData.popupEnforce) {
                // 强制显示弹窗，设置对话框不可取消
                builder.setCancelable(false);
            } else {
                // 设置对话框可取消
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // 在对话框消失时，移除背景蒙版
                        if (getDialog() != null && getDialog().getWindow() != null) {
                            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        }
                    }
                });
            }

            if (popupData.buttonFlag) {
                // 动态添加按钮，最多设置三个按钮
                try {
                    for (int i = 0; i < popupData.buttons.length() && i < 3; i++) {
                        JSONObject button = popupData.buttons.getJSONObject(i);
                        String buttonText = button.getString("button_text");
                        String buttonAction = button.getString("button_action");
                        String actionParameters = button.getString("action_parameters");

                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                handleButtonAction(buttonAction, actionParameters);
                                if (!popupData.popupEnforce) {
                                    dialog.dismiss(); // 关闭对话框
                                }
                            }
                        };

                        if (i == 0) {
                            builder.setPositiveButton(buttonText, listener);
                        } else if (i == 1) {
                            builder.setNegativeButton(buttonText, listener);
                        } else if (i == 2) {
                            builder.setNeutralButton(buttonText, listener);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 在这里处理确认按钮的点击事件
                                dialog.dismiss(); // 关闭对话框
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 在这里处理取消按钮的点击事件
                                dialog.dismiss(); // 关闭对话框
                                getActivity().finish();
                            }
                        });
            }

            // 如果popupFlag为true，则显示弹窗
            if (popupData.popupFlag) {
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // 当不显示弹窗时，也不显示背景蒙版
                if (getDialog() != null && getDialog().getWindow() != null) {
                    getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }
            }
        }
    }

    private void handleButtonAction(String action, String parameters) {
        switch (action) {
            case "jump":
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(parameters));
                startActivity(browserIntent);
                break;
            case "nothing":
                // 不做任何操作
                break;
            case "quit":
                getActivity().finish();
                break;
        }
    }
}

