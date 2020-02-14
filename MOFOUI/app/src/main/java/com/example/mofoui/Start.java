package com.example.mofoui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import android.content.DialogInterface;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import models.Constants;
import requests.Requests;

public class Start extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private  String url = Constants.URl;
    private int STORAGE_PERMISSION_CODE = 1;
    private  boolean authKeyCheckRepeat = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String authKey = GetAuthKey();
        if (ContextCompat.checkSelfPermission(Start.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Start.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Start.this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {

            if(authKey == null || authKey=="")
            {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                String cookie = sharedPref.getString("cookies", null);
                new GetAuthKey().execute(cookie);
            }else {
                new AuthKeyCheck().execute(authKey);
                // startActivity(new Intent(Start.this, MainActivity.class));
            }
        } else {
            requestStoragePermission();
        }

        TextView qrLink = (TextView) findViewById(R.id.openQR);
        String qrText = "Натиснете за да сканирате QR кода";
        SpannableString content = new SpannableString(qrText);
        content.setSpan(new UnderlineSpan(), 0, qrText.length(), 0);
        qrLink.setText(content);
        qrLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Start.this, QRScanner.class));
            }
        });
        TextView  text1 = (TextView) findViewById(R.id.textView3);
        text1.setVisibility(View.INVISIBLE);
        ProgressBar pb = findViewById(R.id.progressBar3);
        pb.setVisibility(View.VISIBLE);
        qrLink.setVisibility(View.INVISIBLE);

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
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }



    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)&& ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {

            new AlertDialog.Builder(this)
                    .setTitle("Изисква се разрешение!")
                    .setMessage("За пълната функционалност на приложението се изсква достъп до вътрешната памет и камерата.")
                    .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Start.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Отказ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new AlertDialog.Builder(getApplicationContext())
                                    .setTitle("Изисква се разрешение!")
                                    .setMessage("Разрешението е отказано. Приложението ще бъде затворено.")
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })                        .create().show();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String authKey = GetAuthKey();
                if(authKey == null || authKey=="")
                {
                    startActivity(new Intent(Start.this, Register.class));


                }else {
                    new AuthKeyCheck().execute(authKey);
            }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Изисква се разрешение!")
                        .setMessage("Разрешението е отказано. Приложението ще бъде затворено.")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })                        .create().show();
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String authKey = GetAuthKey();
        if(authKeyCheckRepeat){
            new AuthKeyCheck().execute(authKey);
        }
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                showWirelessSettings();
            }else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(nfcAdapter!=null){
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
    private void showWirelessSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Изисква се разрешение!")
                .setMessage("За пълната функционалност на приложението се изсква включено NFC.")
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                    }
                })
                .setNegativeButton("Отказ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle("Изисква се разрешение!")
                                .setMessage("Разрешението е отказано. Приложението ще бъде затворено.")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })                        .create().show();
                    }
                })
                .create().show();new AlertDialog.Builder(this)
                .setTitle("Изисква се разрешение!")
                .setMessage("За пълната функционалност на приложението се изсква включено NFC.")
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                })
                .setNegativeButton("Отказ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle("Изисква се разрешение!")
                                .setMessage("Разрешението е отказано. Приложението ще бъде затворено.")
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })                        .create().show();
                    }
                })
                .create().show();
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
                TextView qrTextView = findViewById(R.id.openQR);
                qrTextView.setVisibility(View.VISIBLE);
                TextView  text1 = (TextView) findViewById(R.id.textView3);
                text1.setVisibility(View.VISIBLE);
                ProgressBar pb = findViewById(R.id.progressBar3);
                pb.setVisibility(View.INVISIBLE);

            }
        }else{
            TextView  text1 = (TextView) findViewById(R.id.textView3);
            text1.setVisibility(View.INVISIBLE);
            ProgressBar pb = findViewById(R.id.progressBar3);
                    pb.setVisibility(View.INVISIBLE);
        }

    }
    private  void  showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private NfcAdapter getAdapter(){
        return NfcAdapter.getDefaultAdapter(this);
     //   return  null;
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
    public class GetAuthKey extends AsyncTask<String, Integer, models.RegisterUser> {
        @Override
        protected models.RegisterUser doInBackground(String... urls) {
            Requests.RequestResponse requestResponse = null;
            models.RegisterUser registerUser = null;
            try {

                requestResponse = Requests.HttpRequest(Constants.URl + "/account/getauthkey", "GET", urls[0]);
                Gson gson = new GsonBuilder().create();
                registerUser = gson.fromJson(requestResponse.JsonString, models.RegisterUser.class);
                if (registerUser.status.equals("OK")) {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("cookies", urls[0]);
                    editor.putString("authKey", registerUser.auth);
                    editor.commit();
                    new AuthKeyCheck().execute(registerUser.auth);
                }
                else{
                    startActivity(new Intent(Start.this, Register.class));
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


            return registerUser;
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sessions) {
            Intent intent = new Intent();
            startActivity(intent);

        } else if (id == R.id.nav_account) {
            Intent intent = new Intent(Start.this, Account.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class AuthKeyCheck extends AsyncTask<String, Integer, models.BasicResponse> {
        @Override
        protected models.BasicResponse doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.BasicResponse basicResponse = null;
            try {

                requestResponse = Requests.HttpRequest(url+ "/room/authkeycheck?auth=" + urls[0], "GET", null);
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
            if (basicResponse != null) {
                if (basicResponse.status.compareTo("OK") == 0) {
                    startActivity(new Intent(Start.this, MainActivity.class));
                } else if (basicResponse.status.compareTo("WRONG AUTH") == 0) {
                    startActivity(new Intent(Start.this, Register.class));
                } else {
                    nfcAdapter = getAdapter();
                    if(nfcAdapter!=null) {
                        boolean check = nfcAdapter.isEnabled();
                        if (nfcAdapter == null || !check) {
                            showWirelessSettings();
                        } else {
                            createPendingIntent();
                        }
                    }else{
                        TextView  text1 = (TextView) findViewById(R.id.textView3);
                        text1.setVisibility(View.INVISIBLE);
                        ProgressBar pb = findViewById(R.id.progressBar3);
                        pb.setVisibility(View.INVISIBLE);
                        TextView qrTextView = findViewById(R.id.openQR);
                        qrTextView.setVisibility(View.VISIBLE);
                    }
                }
                authKeyCheckRepeat = false;
            }else{
                authKeyCheckRepeat = true;
                noInternet();
            }
        }

    }
    public class JoinRoom extends AsyncTask<String, Integer, models.BasicResponse> {
        @Override
        protected models.BasicResponse doInBackground(String... urls)  {
            Requests.RequestResponse requestResponse = null;
            models.BasicResponse basicResponse = null;
            try {

                requestResponse = Requests.HttpRequest(url+ "/room/joinroom?auth=" + urls[0]+ "&deskCode="+urls[1], "POST", null);
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
            if (basicResponse != null) {
                if (basicResponse.status.compareTo("OK") == 0) {
                    Intent intent = new Intent(Start.this, MainActivity.class);// New activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                } else if (basicResponse.status.compareTo("NO DESK") == 0) {
                    showToast("Unauthorised card");
                } else {
                    startActivity(new Intent(Start.this, Register.class));
                }
            }else {
                noInternet();
            }
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


