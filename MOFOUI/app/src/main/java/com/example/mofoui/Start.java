package com.example.mofoui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import requests.Requests;

public class Start extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private  String url = "https://mofoapp.azurewebsites.net";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button button = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  startActivity(new Intent(Start.this, MainActivity.class));
                TextView tv = (TextView) findViewById( R.id.editText2);
                new JoinRoom().execute(GetAuthKey(), tv.getText().toString());

            }
        });
        TextView  text = (TextView) findViewById(R.id.editText2);
        text.setVisibility(View.INVISIBLE);
        Button btn = findViewById(R.id.button2);
        btn.setVisibility(View.INVISIBLE);
        String authKey = GetAuthKey();
        TextView  text1 = (TextView) findViewById(R.id.textView3);
        text1.setVisibility(View.VISIBLE);
        if(authKey == null)
        {
            startActivity(new Intent(Start.this, Register.class));


        }else {
            new AuthKeyCheck().execute(authKey);
           // startActivity(new Intent(Start.this, MainActivity.class));
        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
    private void showWirelessSettings() {
        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
    }
    private  void createPendingIntent(){
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
            {
                showWirelessSettings();
            }else{
                pendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(getApplicationContext(), this.getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

            }
        }else{
            TextView  text = (TextView) findViewById(R.id.editText2);
            text.setVisibility(View.VISIBLE);
            Button btn = findViewById(R.id.button2);
            btn.setVisibility(View.VISIBLE);
            TextView  text1 = (TextView) findViewById(R.id.textView3);
            text1.setVisibility(View.INVISIBLE);
        }

    }
    private  void  showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private NfcAdapter getAdapter(){
        return NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // setIntent(intent);
        resolveIntent(intent);
    }
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)||NfcAdapter.ACTION_TAG_DISCOVERED.equals(action))
        {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Toast.makeText(this, "First if " + tag.toString(), Toast.LENGTH_SHORT).show();
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            byte[] tagId = tag.getId();
            String code = toHex(tagId);
            TextView  text = (TextView) findViewById(R.id.editText2);
            text.setText(code);
            new JoinRoom().execute(GetAuthKey(), code);

           // SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
          //  SharedPreferences.Editor editor = sharedPref.edit();
           // editor.putString("authKey", );
           // editor.commit();

            // TODO: Send request to server
            // TODO: JOIN ROOM
            // TODO: SAVE CODE
            // TODO: GO TO NEXT VIEW
        }
    }
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    private String GetAuthKey(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
    }
    public class AuthKeyCheck extends AsyncTask<String, Integer, models.BasicResponse> {
        @Override
        protected models.BasicResponse doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.BasicResponse basicResponse = null;
            try {

                requestResponse = Requests.HttpRequest(url+ "/room/authkeycheck?auth=" + urls[0], "GET");
                Gson gson = new GsonBuilder().create();
                 basicResponse =gson.fromJson(requestResponse.JsonString, models.BasicResponse.class);

               // Toast.makeText(getApplicationContext(), basicResponse.status.toString(), Toast.LENGTH_LONG).show();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return  basicResponse;
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(models.BasicResponse basicResponse) {
            showToast(basicResponse.status);

          if(basicResponse.status.compareTo("OK")==0){
              startActivity(new Intent(Start.this, MainActivity.class));
          }else if(basicResponse.status.compareTo("WRONG AUTH")==0) {
              startActivity(new Intent(Start.this, Register.class));
          }else{
             nfcAdapter = getAdapter();
             boolean check = nfcAdapter.isEnabled();
             if (nfcAdapter == null||!check) {

                finish();
                showWirelessSettings();
                return;
         }else {
                createPendingIntent();
            }
          }
        }

    }
    public class JoinRoom extends AsyncTask<String, Integer, models.BasicResponse> {
        @Override
        protected models.BasicResponse doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.BasicResponse basicResponse = null;
            try {

                requestResponse = Requests.HttpRequest(url+ "/room/joinroom?auth=" + urls[0]+ "&deskCode="+urls[1], "POST");
                Gson gson = new GsonBuilder().create();
                basicResponse =gson.fromJson(requestResponse.JsonString, models.BasicResponse.class);

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return  basicResponse;
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(models.BasicResponse basicResponse) {

            if(basicResponse.status.compareTo("OK")==0){
                Intent intent = new Intent(Start.this, MainActivity.class);// New activity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }else if(basicResponse.status.compareTo("NO DESK")==0) {
                showToast("Unauthorised card");
            }else{
                startActivity(new Intent(Start.this, Register.class));
            }
        }

    }
}


