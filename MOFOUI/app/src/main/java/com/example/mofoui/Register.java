package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import requests.Requests;

public class Register extends AppCompatActivity {

    private  String url = "https://mofoapp.azurewebsites.net";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button button = (Button) findViewById(R.id.registerButton);
        final TextView textView = (TextView) findViewById(R.id.nameEditText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new RegisterUser().execute(textView.getText().toString());
              //  startActivity(new Intent(Register.this, MainActivity.class));
            }
        });


    }
    public class RegisterUser extends AsyncTask<String, Integer, models.RegisterUser> {
        @Override
        protected models.RegisterUser doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.RegisterUser registerUser = null;
            try {

                requestResponse = Requests.HttpRequest(url + "/user/registeruser?name="+ urls[0], "POST");
                Gson gson = new GsonBuilder().create();
                registerUser =gson.fromJson(requestResponse.JsonString, models.RegisterUser.class);
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("authKey", registerUser.auth);
                    editor.commit();
                    startActivity(new Intent(Register.this, Start.class));


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

           // TextView tv = findViewById(R.id.textbar);
            //    tv.setText(result.JsonString);

        }

    }
}
