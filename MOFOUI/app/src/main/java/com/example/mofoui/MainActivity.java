package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import 	android.webkit.MimeTypeMap;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import models.BasicResponse;
import models.FeedSync;
import models.File;
import requests.Requests;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView frontFeedListView;
    private  String url = "https://mofoapp.azurewebsites.net";
    private File[] files;
    Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Upload.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Timer timer = new Timer();
        TimerTask task = new FeedTimerTask(GetAuthKey());
        timer.schedule(task, 0, 10000);
        frontFeedListView = findViewById(R.id.frontFeedListView);
        frontFeedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = files[position];
                new DownloadFile().execute(file.downloadCode, GetAuthKey(), file.fileName);

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_feed) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_users) {
            Intent intent = new Intent(this, Users.class);
            startActivity(intent);

        } else if (id == R.id.nav_account) {
            Intent intent = new Intent(this, Account.class);
            startActivity(intent);
        }else if(id== R.id.leaveRoom){
            new LeaveRoom().execute(GetAuthKey());
            Intent intent = new Intent(this, Start.class);
            timer.cancel();
            timer = null;
            finish();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private  void  showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    public class FeedSyncRequest extends AsyncTask<String, Integer, FeedSync> {
        @Override
        protected models.FeedSync doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.FeedSync basicResponse = null;
            try {

                requestResponse = Requests.HttpRequest(url+ "/file/filesync?auth=" + urls[0], "GET");
                Gson gson = new GsonBuilder().create();
                basicResponse =gson.fromJson(requestResponse.JsonString, models.FeedSync.class);

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return  basicResponse;
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(models.FeedSync basicResponse) {

            if(basicResponse.status.compareTo("OK")==0){

                frontFeedListView = findViewById(R.id.frontFeedListView);
                String[] feed = new String[basicResponse.files.size()];
                files = new File[basicResponse.files.size()];
                if(basicResponse.files.size()!=0){
                    TextView t1 = findViewById(R.id.textView7);
                    t1.setVisibility(View.INVISIBLE);
                }else {
                    TextView t1 = findViewById(R.id.textView7);
                    t1.setVisibility(View.VISIBLE);
                }
                for (int i=0; i<basicResponse.files.size(); i++){
                    feed[i] = "\n" + basicResponse.files.get(i).username+ " качи: "+  "\nОписание: "+ basicResponse.files.get(i).message + "\nФайл: " + basicResponse.files.get(i).fileName + "\n"+ basicResponse.files.get(i).dateTimeUploaded +  "\n";
                    files[i] = basicResponse.files.get(i);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, feed);
                frontFeedListView.setAdapter(arrayAdapter);

            }else if(basicResponse.status.compareTo("WRONG AUTH")==0) {
                startActivity(new Intent(MainActivity.this, Register.class));
                timer.cancel();
                finish();
            }else if(basicResponse.status.compareTo("NO SESSION")==0){
                startActivity(new Intent(MainActivity.this, Start.class));
                timer.cancel();
                timer = null;
                finish();
            }
        }

    }
    public class DownloadFile extends AsyncTask<String, String, String> {


        private String fileName;
        private String folder;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL URL = new URL(url + "/file/downloadfile?downloadcode=" + f_url[0] + "&auth="+ f_url[1]);

                URLConnection connection = URL.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(URL.openStream(), 8192);



                //Append timestamp to file name
                fileName = f_url[2];

                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + java.io.File.separator + "androiddeft/";

                //Create androiddeft folder if it does not exist
                java.io.File directory = new java.io.File(folder);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }
                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return folder + fileName;

            } catch (Exception e) {

                return "Something went wrong";
            }

            /**
             * Updating progress bar
             */
        }
        @Override
        protected void onPostExecute(String path){
            java.io.File file = new java.io.File(path);
            Uri uri =FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);

           if(Build.VERSION.SDK_INT<24) {
               uri = Uri.fromFile(file);
           }

            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(uri,
                    getMimeType(path));

            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(install);
        }
        protected void openFile(String fileName) {

        }
        public String getMimeType(String url) {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            return type;
        }
       /*@Override
        protected  void onPostExecute(String path){
           showToast("File Downloaded successfully");
       }*/
        }
    public class LeaveRoom extends AsyncTask<String, Integer, models.RegisterUser> {
        @Override
        protected models.RegisterUser doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.RegisterUser registerUser = null;
            try {

                requestResponse = Requests.HttpRequest(url + "/room/logout?auth="+ urls[0], "POST");
                Gson gson = new GsonBuilder().create();
                registerUser =gson.fromJson(requestResponse.JsonString, models.RegisterUser.class);

                startActivity(new Intent(MainActivity.this, Start.class));
                finish();

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
    public class FeedTimerTask extends TimerTask{
        private String AuthKey;
        public FeedTimerTask(String authKey){
            AuthKey = authKey;
        }
        public void run()
        {
          new FeedSyncRequest().execute(AuthKey);
        }
    }
    private String GetAuthKey(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
    }
}
