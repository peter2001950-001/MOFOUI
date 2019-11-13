package com.example.mofoui;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import models.BasicResponse;
import requests.Requests;

public class Feed extends AppCompatActivity {
    private ListView feedListView;
    private  String url = "http://192.168.88.130:59192";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feedListView = findViewById(R.id.feedListView);
        String[] feed = { "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4", "file 1", "file 2", "file 3", "file 4" };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, feed);
        feedListView.setAdapter(arrayAdapter);
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

}
