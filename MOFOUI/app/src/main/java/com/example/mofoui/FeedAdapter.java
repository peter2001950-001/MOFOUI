package com.example.mofoui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    @Override
    public int getItemCount() {
        return modelFeedArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView username, dateTimeUploaded, description, fileName;

        public MyViewHolder(View itemView) {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.username);
            dateTimeUploaded = (TextView) itemView.findViewById(R.id.dateTimeUploaded);
            description = (TextView) itemView.findViewById(R.id.description);
            fileName = (TextView) itemView.findViewById(R.id.fileName);
        }
    }
}
