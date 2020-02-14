package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import models.Constants;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import requests.Requests;

public class SendText  extends AppCompatActivity {
    EditText message;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button button = findViewById(R.id.upload);
        button.setText("Изпрати съобщение");
        button.setVisibility(View.VISIBLE);
        message=findViewById(R.id.editText3);
        TextView description = findViewById(R.id.textView6);
        description.setText("Напишете вашето съобщение");
        TextView fileTv = findViewById(R.id.textView4);
        TextView fileNameTv = findViewById(R.id.textView5);
        fileTv.setVisibility(View.GONE);
        fileNameTv.setVisibility(View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String authKey = GetAuthKey();
                new SendMessageSync().execute(message.getText().toString(), authKey);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private class SendMessageSync extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            if (!(TextUtils.isEmpty(message.getText().toString()))){
                Requests.RequestResponse requestResponse = null;
                models.ActiveUsers basicResponse = null;
                if (message.getText().toString().length() > 400) {
                    return "Съобщението трябва да е под 400 символа";
                }
                try {
                    requestResponse = Requests.HttpRequest(Constants.URl + "/file/uploadFile?type=0&message=" + params[0] + "&auth=" + params[1], "POST", null);
                    Gson gson = new GsonBuilder().create();
                    basicResponse =gson.fromJson(requestResponse.JsonString, models.ActiveUsers.class);
                    if (basicResponse.status.compareTo("ERR")==0) {
                        return "Сървърна грешка";
                    }
                    else if (basicResponse.status.compareTo("NO SESSION")==0) {
                        return "Сесията е прекратена";
                    }

                    return "Съобщението е изпратено";
                }catch (Exception e){
                    return "Сървърна грешка";
                }
            }
            else return "Сървърна грешка";
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SendText.this, MainActivity.class));
            finish();
        }

        @Override
        protected void onPreExecute() {
            Button bth = (Button) findViewById(R.id.upload);
            bth.setVisibility(View.INVISIBLE);
        }

        @Override

        protected void onProgressUpdate(Integer... values) {
            ProgressBar rb = (ProgressBar) findViewById(R.id.progressBar);
            rb.setProgress(values[0]);
        }
    }
    private String GetAuthKey(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
    }
}
