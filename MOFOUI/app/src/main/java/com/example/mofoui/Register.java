package com.example.mofoui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import models.Constants;
import requests.Requests;

public class Register extends AppCompatActivity {

    private  String url = Constants.URl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final WebView webView = (WebView) findViewById(R.id.webview);
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();

        WebSettings webSettings = webView.getSettings();
        ((WebSettings) webSettings).setJavaScriptEnabled(true);
        /* WebViewClient must be set BEFORE calling loadUrl! */
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if(url.toLowerCase().indexOf("/account/login".toLowerCase()) > -1 || url.toLowerCase().indexOf("/account/registerStudent".toLowerCase()) > -1||url.toLowerCase().indexOf("/account/registerTeacher".toLowerCase()) > -1)
                {
                    return false;
                }else if(url.toLowerCase().indexOf("/account/emptyResult".toLowerCase()) > -1){
                    String cookies = CookieManager.getInstance().getCookie(url);
                    new GetAuthKey().execute(cookies);
                    //send auth check request
                    return false;
                }else {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                   return true;
                }
            }
            @Override
            public void onPageFinished(WebView view, String url)
            {

            }
        });
        webView.loadUrl(Constants.URl+"/account/login?authget=true");

    }
    public class GetAuthKey extends AsyncTask<String, Integer, models.RegisterUser> {
        @Override
        protected models.RegisterUser doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.RegisterUser registerUser = null;
            try {

                requestResponse = Requests.HttpRequest(url + "/account/getauthkey", "GET", urls[0]);
                Gson gson = new GsonBuilder().create();
                registerUser = gson.fromJson(requestResponse.JsonString, models.RegisterUser.class);
                if(registerUser.status.equals("OK")) {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("authKey", registerUser.auth);
                    editor.commit();
                    startActivity(new Intent(Register.this, Start.class));
                }


            }
            catch (IOException e) {
                e.printStackTrace();
            }


            return  registerUser;
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(models.RegisterUser result) {


            if(result==null){
                noInternet();
            }
           // TextView tv = findViewById(R.id.textbar);
            //    tv.setText(result.JsonString);

        }

    }
    private void noInternet(){
        new AlertDialog.Builder(this)
                .setTitle("Няма връзка с интернет!")
                .setMessage("За пълната функционалност на приложението се изисква интернет връзка.")
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .create().show();
    }
}
