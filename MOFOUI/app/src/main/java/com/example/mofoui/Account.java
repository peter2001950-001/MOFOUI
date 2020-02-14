
package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import models.Constants;
import models.RegisterUser;
import requests.Requests;

public class Account extends AppCompatActivity {
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        final WebView webView = (WebView) findViewById(R.id.webview);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CookieManager.getInstance().setCookie(Constants.URl, GetAuthToken());
        WebSettings webSettings = webView.getSettings();
        CookieSyncManager.createInstance(getApplicationContext());
        CookieSyncManager.getInstance().startSync();
        ((WebSettings) webSettings).setJavaScriptEnabled(true);
        /* WebViewClient must be set BEFORE calling loadUrl! */
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.toLowerCase().indexOf("/user/emptyResult".toLowerCase()) > -1) {
                    String cookies = CookieManager.getInstance().getCookie(url);
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("cookies", cookies);
                    editor.putString("authKey", "");
                    editor.commit();
                    startActivity(new Intent(Account.this, Start.class));

                    return false;
                }
                else{
                    return false;
                }
            }
        });
        webView.loadUrl(Constants.URl+"/user/settings?mobile=true");
    }
    private String GetAuthToken(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String key = sharedPref.getString("cookies", null);
        return key;
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(Account.this, Start.class));
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            startActivity(new Intent(Account.this, Start.class));
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
