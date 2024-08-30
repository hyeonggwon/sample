package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreenViewProvider;
import androidx.lifecycle.ViewModelProvider;

import com.obigo.WebViewClient;
import com.obigo.WebView;

import org.chromium.base.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private ImageView mSplashImage;
    private JSONObject jsonObject;
    private String TAG = "MainActivity";
    private String jsonFilePath;
    private String URL;
    private boolean isNetworkConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        SplashScreen.installSplashScreen(MainActivity.this);
        setContentView(R.layout.activity_main);

        createAppDirectory(this);

        jsonFilePath = Objects.requireNonNull(this.getExternalFilesDir(null)).getPath() + "/config.json";
        jsonObject = readJSONFromFile(jsonFilePath);
        try {
            URL = jsonObject.getString("url");
        } catch (JSONException e) {
            Log.e(TAG, "Json Error");
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mSplashImage = findViewById(R.id.splashImage);
        mWebView = findViewById(R.id.wvLayout);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (X11; ccNC; Linux aarch64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.120 Safari/537.36");
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setInitialScale(100);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "[onPageFinished] URL : " + URL + ", paremeter url : " + url);
                if (url.equals(URL) || url.equals(URL + "/")) {
                    Log.i(TAG, "[LOAD Done] URL : " + URL + ", paremeter url : " + url);
                    try {
                        //mViewModel.setReady(true);
                        hideSplash();
                    } catch (Exception e) {
                        Log.e(TAG, "[LOAD Error] URL : " + URL + ", paremeter url : " + url);
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                hideSplash();
            }
        });

        // ConnectivityManager 가져오기
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // NetworkCallback 설정
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // 네트워크 연결됨
                if (!isNetworkConnected) {
                    isNetworkConnected = true;
                    performActionOnNetworkAvailable();
                }
            }
        });
    }

    private void performActionOnNetworkAvailable() {
        // 네트워크 연결 시 수행할 작업
        runOnUiThread(() -> mWebView.loadUrl(URL));
    }

    private void showSplash() {
        mSplashImage.setVisibility(View.VISIBLE);
        mSplashImage.setAlpha(0f);
        mSplashImage.animate()
                .alpha(1f)
                .setDuration(500);
    }

    private void hideSplash() {
        mSplashImage.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {
                    mSplashImage.setVisibility(View.GONE);
                });
    }

    private void createAppDirectory(Context context) {
        // 앱의 외부 저장소 경로 가져오기
        File externalStorageDir = context.getExternalFilesDir(null);

        if (externalStorageDir != null) {
            // 폴더가 없으면 생성
            if (!externalStorageDir.exists()) {
                boolean isCreated = externalStorageDir.mkdirs();
                if (isCreated) {
                    // 폴더 생성 성공
                    Log.d(TAG, "App directory created at " + externalStorageDir.getPath());
                } else {
                    // 폴더 생성 실패
                    Log.e(TAG, "Failed to create app directory.");
                    return;
                }
            } else {
                // 이미 폴더가 존재
                Log.d(TAG, "App directory already exists at " + externalStorageDir.getPath());
            }

            String fileName = "config.json";
            File externalFile = new File(externalStorageDir, fileName);
            if (!externalFile.exists()) {
                try {
                    InputStream is = context.getAssets().open(fileName);
                    OutputStream os = new FileOutputStream(externalFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    os.flush();
                    os.close();
                    is.close();
                    Log.d(TAG, "config.json file created at " + externalStorageDir.getPath());
                } catch (IOException e) {
                    Log.e(TAG, "config.json create Error");
                }
            }
        }
    }

    public JSONObject readJSONFromFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                InputStream is = new FileInputStream(file);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String jsonString = new String(buffer, "UTF-8");
                return new JSONObject(jsonString);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "readJSONFromFile Error");
            }
        }
        return null;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}