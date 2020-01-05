package com.example.mofoui;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import androidx.appcompat.app.AppCompatActivity;
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
        Intent intent = new Intent();
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
        intent.setDataAndType(uri,"*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"),1);
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
            Uri fileUri = data.getData();
            String path = getRealPathFromUri(getApplicationContext(),fileUri);
            if (path != null) {
                UploadFileFileName = path;
                String filename=path.substring(path.lastIndexOf("/")+1);

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
            }
            else{
                Toast.makeText(getApplicationContext(), "Can't upload a virtual file", Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if (resultCode == RESULT_CANCELED){
            finish();
        }
    }
    public String getRealPathFromUri(Context context, Uri uri){
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    uri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("image".equals(type)) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    selection = "_id=?";
                    selectionArgs = new String[]{ split[1] };
                }else if (isGoogleDriveUri(uri)) {
                    uri = Uri.parse(getDriveFilePath(uri, context.getApplicationContext()));
                    return uri.getPath();
                }
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if(cursor!=null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                }
            } catch (Exception e) {
                return e.toString();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    private boolean isVirtualFile(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
                return false;
            }

            Cursor cursor = getApplicationContext().getContentResolver().query(
                    uri,
                    new String[]{DocumentsContract.Document.COLUMN_FLAGS},
                    null, null, null);
            int flags = 0;
            if (cursor.moveToFirst()) {
                flags = cursor.getInt(0);
            }
            cursor.close();
            return (flags & DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0;
        } else {
            return false;
        }
    }
    private String getDriveFilePath(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        File file = null;
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        if(isVirtualFile(returnUri)){
            return null;
        }else {
            file = new File(context.getCacheDir(), name);
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read = 0;
                int maxBufferSize = 1 * 1024 * 1024;
                int bytesAvailable = inputStream.available();

                //int bufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                final byte[] buffers = new byte[bufferSize];
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);
                }
                inputStream.close();
                outputStream.close();
            } catch (Exception e) {
                return e.toString();
            }
        }
        return file.getPath();
    }
    private class UploadFileAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

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



                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();
                        if (serverResponseCode == 500) {
                            return "Server error";
                        }
                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();
                        return "Server error";
                    }
                    // dialog.dismiss();
                    return "File uploaded";

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();

            }
            return "Not a file";
        }

        @Override
        protected void onPostExecute(String result) {
            createToast(result);
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
