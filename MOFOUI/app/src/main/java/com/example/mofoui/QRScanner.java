package com.example.mofoui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.os.Bundle;
import android.provider.Settings;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import models.BasicResponse;
import models.Constants;
import requests.Requests;

public class QRScanner extends AppCompatActivity {
    String url = Constants.URl;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        surfaceView = (SurfaceView) findViewById(R.id.cameraPreview);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setFacing(0).setAutoFocusEnabled(true).setRequestedPreviewSize(1024, 768).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(QRScanner.this)
                            .setTitle("Изисква се разрешение!")
                            .setMessage("За пълната функционалност на приложението се изсква достъп до камерата.")
                            .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(QRScanner.this,
                                            new String[]  {Manifest.permission.CAMERA}, 0);
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
                try
                {
                    cameraSource.start(holder);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if (qrCodes.size()!=0){
                    Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(50);
                    barcodeDetector.release();
                    new JoinRoom().execute(GetAuthKey(), qrCodes.valueAt(0).displayValue);
                }
            }
        });
    }
    private String GetAuthKey(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(QRScanner.this, Start.class));
        finish();
    }

    public class JoinRoom extends AsyncTask<String, Integer, BasicResponse> {
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
                    Intent intent = new Intent(QRScanner.this, MainActivity.class);// New activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                } else if (basicResponse.status.compareTo("NO DESK") == 0) {
                    Toast.makeText(QRScanner.this,"Unauthorised card", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(QRScanner.this, Register.class));
                }
            }else {
                    new AlertDialog.Builder(QRScanner.this)
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

    }
}
