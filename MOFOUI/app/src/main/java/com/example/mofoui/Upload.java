package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import models.Constants;

public class Upload extends AppCompatActivity {


    public String UploadFileFileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Button btn = (Button) findViewById(R.id.upload);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tx = (TextView) findViewById(R.id.editText3) ;
                String key = GetAuthKey();
                new UploadFileAsync().execute(tx.getText().toString(), key);
            }
        });
        FloatingActionButton chooseFile = (FloatingActionButton) findViewById(R.id.choose);
        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(Upload.this)
                        .withRequestCode(1).withRootPath(Environment.getExternalStorageDirectory().getPath())
                        .withFilterDirectories(false) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });
        TextView t3 = findViewById(R.id.editText3);
        t3.setVisibility((View.INVISIBLE));
        Button b1 = findViewById(R.id.upload);
        b1.setVisibility((View.INVISIBLE));
        ProgressBar p1 = findViewById(R.id.progressBar);
        p1.setVisibility((View.INVISIBLE));

        TextView t4 = findViewById(R.id.textView4);
        t4.setVisibility((View.INVISIBLE));
        TextView t5 = findViewById(R.id.textView5);
        t5.setVisibility((View.INVISIBLE));
        TextView t6 = findViewById(R.id.textView6);
        t6.setVisibility((View.INVISIBLE));

    }
    private  void createToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private String GetAuthKey(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Start", Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            if (path != null) {
                UploadFileFileName = path;
                String filename=path.substring(path.lastIndexOf("/")+1);
                FloatingActionButton f1 = findViewById(R.id.choose);
                f1.hide();
                TextView t1 = findViewById(R.id.textView2);
                t1.setVisibility((View.INVISIBLE));

                TextView t3 = findViewById(R.id.editText3);
                t3.setVisibility((View.VISIBLE));
                Button b1 = findViewById(R.id.upload);
                b1.setVisibility((View.VISIBLE));
                ProgressBar p1 = findViewById(R.id.progressBar);
                p1.setVisibility((View.VISIBLE));

                TextView t4 = findViewById(R.id.textView4);
                t4.setVisibility((View.VISIBLE));
                TextView t5 = findViewById(R.id.textView5);
                t5.setVisibility((View.VISIBLE));
                t5.setText(filename);
                TextView t6 = findViewById(R.id.textView6);
                t6.setVisibility((View.VISIBLE));
                //TextView textView = (TextView) findViewById(R.id.fileName);
               // textView.setText(UploadFileFileName);
            }
        }
    }
    private class UploadFileAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String path = Environment.getExternalStorageDirectory().getPath();

                String sourceFileUri =  UploadFileFileName;

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = Constants.URl+ "/file/uploadFile?type=1&message="+ params[0]+ "&auth="+ params[1];

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);


                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"bill\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);
                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();
                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();
                        return "Executed";
                    }
                    // dialog.dismiss();
                    return "Executed";

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();

            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            createToast("File uploaded");
            startActivity(new Intent(Upload.this, MainActivity.class));
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
}
