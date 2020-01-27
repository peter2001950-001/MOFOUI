package com.example.mofoui;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {

    Context context;
    ArrayList<models.File> modelFeedArrayList;

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
    public void onBindViewHolder(MyViewHolder holder, int position) {
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

}
