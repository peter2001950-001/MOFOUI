package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import models.ActiveUsers;
import models.FeedSync;
import models.File;
import models.User;
import requests.Requests;

public class Users extends AppCompatActivity {

    private  String url = "https://mofoapp.azurewebsites.net";
    private ListView frontFeedListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        frontFeedListView = findViewById(R.id.usersListView);


        Timer timer = new Timer();
        TimerTask task = new Users.FeedTimerTask(GetAuthKey());
        timer.schedule(task, 0, 10000);
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
    public class ActiveUsersRequest extends AsyncTask<String, Integer, ActiveUsers> {
        @Override
        protected models.ActiveUsers doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.ActiveUsers basicResponse = null;
            try {

                requestResponse = Requests.HttpRequest(url+ "/room/getactiveusers?auth=" + urls[0], "GET");
                Gson gson = new GsonBuilder().create();
                basicResponse =gson.fromJson(requestResponse.JsonString, models.ActiveUsers.class);

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return  basicResponse;
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(models.ActiveUsers basicResponse) {

            if(basicResponse.status.compareTo("OK")==0){


                frontFeedListView = findViewById(R.id.usersListView);
                String[] feed = new String[basicResponse.users.size()];
                for (int i=0; i<basicResponse.users.size(); i++){
                    feed[i] =  basicResponse.users.get(i).userName;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, feed);
                frontFeedListView.setAdapter(arrayAdapter);

            }else if(basicResponse.status.compareTo("WRONG AUTH")==0) {
                startActivity(new Intent(Users.this, Register.class));
            }else if(basicResponse.status.compareTo("NO SESSION")==0){
                startActivity(new Intent(Users.this, Start.class));
            }
        }

    }
    public class FeedTimerTask extends TimerTask {
        private String AuthKey;
        public FeedTimerTask(String authKey){
            AuthKey = authKey;
        }
        public void run()
        {
            new Users.ActiveUsersRequest().execute(AuthKey);
        }
    }
    private String GetAuthKey(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
    }
}
