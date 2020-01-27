package com.example.mofoui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import models.Constants;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {

    Context context;
    ArrayList<models.File> modelFeedArrayList;
    AppCompatActivity activity;
    String folder;
    private String url = Constants.URl;

    public FeedAdapter(Context context, ArrayList<models.File> modelFeedArrayList) {

        this.context = context;
        this.modelFeedArrayList = modelFeedArrayList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final models.File modelFeed = modelFeedArrayList.get(position);
        holder.username.setText(modelFeed.getUsername());
        holder.dateTimeUploaded.setText(modelFeed.getDateTimeUploaded());
        holder.fileName.setText(modelFeed.getFileName());
        if(modelFeed.getMessage().compareTo("")==0){
            holder.description.setVisibility(View.GONE);
        }else {
            holder.description.setVisibility(View.VISIBLE);
        }
        if(modelFeed.getFileName() == null || modelFeed.getFileName().compareTo("")==0){
            holder.fileView.setVisibility(View.GONE);
        }else {
            holder.fileView.setVisibility(View.VISIBLE);
            holder.fileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   FrameLayout frameLayout = activity.findViewById(R.id.progress_view);
                   frameLayout.setVisibility(View.VISIBLE);
                  new DownloadFile().execute(modelFeed.getDownloadCode(), GetAuthKey(), modelFeed.getFileName());
                }
            });
        }
        holder.description.setText(modelFeed.getMessage());
    }

    @Override
    public int getItemCount() {
        return modelFeedArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView username, dateTimeUploaded, description, fileName;
        LinearLayout fileView;

        public MyViewHolder(View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.username);
            dateTimeUploaded = (TextView) itemView.findViewById(R.id.dateTimeUploaded);
            description = (TextView) itemView.findViewById(R.id.description);
            fileName = (TextView) itemView.findViewById(R.id.fileName);
            fileView = (LinearLayout) itemView.findViewById(R.id.file_view);
        }
    }
    public String GetAuthKey(){
        SharedPreferences sharedPref = context.getSharedPreferences("Start", Context.MODE_PRIVATE);
        String key = sharedPref.getString("authKey", null);
        return  key;
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
            Uri uri = FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);

            if(Build.VERSION.SDK_INT<24) {
                uri = Uri.fromFile(file);
            }

            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(uri,
                    getMimeType(path));

            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            FrameLayout frameLayout = activity.findViewById(R.id.progress_view);
            frameLayout.setVisibility(View.GONE);
            activity.startActivity(install);
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
}
