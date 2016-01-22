package com.example.tberroa.girodicerapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

// RecyclerView adapter
public class TabViewAdapter extends RecyclerView.Adapter<TabViewAdapter.ImageViewHolder> {

    Context activityContext;
    int numberOfImages;
    int missionNumber;
    String username, type;

    // constructor
    TabViewAdapter(Context activityContext, int missionNumber, int numberOfImages, String type, String username){
        this.activityContext = activityContext;
        this.missionNumber = missionNumber;
        this.numberOfImages = numberOfImages;
        this.type = type;
        this.username = username;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView Photo;

        ImageViewHolder(View itemView) {
            super(itemView);
            Photo = (ImageView) itemView.findViewById(R.id.photo);
        }
    }

    @Override
    public int getItemCount() {
        return numberOfImages;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View mission = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_layout, viewGroup, false);
        return new ImageViewHolder(mission);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        // construct image url
        String urlBase = "http://s3.amazonaws.com/girodicer/"+username+"/Mission+";
        String url;
        switch (type){
            case "aerial":
                url = urlBase+Integer.toString(missionNumber)+"/Aerial/aerial"+Integer.toString(i+1)+".jpg";
                break;
            case "thermal":
                url = urlBase+Integer.toString(missionNumber)+"/Thermal/thermal"+Integer.toString(i+1)+".jpg";
                break;
            case "iceDam":
                url = urlBase+Integer.toString(missionNumber)+"/IceDam/icedam"+Integer.toString(i+1)+".jpg";
                break;
            case "salt":
                url = urlBase+Integer.toString(missionNumber)+"/Salt/salt"+Integer.toString(i+1)+".jpg";
                break;
            default:
                url = urlBase;
        }
        // get screen dimensions
        int screenWidth = Utilities.getScreenWidth(activityContext);
        int screenHeight = Utilities.getScreenHeight(activityContext);
        // set image width and height
        int imageWidth;
        int imageHeight;
        if (screenWidth > screenHeight){ // landscape
            imageWidth = screenWidth / 4;
            imageHeight = screenHeight / 2;
        }
        else{ // vertical
            imageWidth = screenWidth / 2;
            imageHeight = screenHeight / 4;
        }
        // set image with Picasso
        Picasso.with(activityContext)
                .load(url)
                .resize(imageWidth, imageHeight)
                .into(imageViewHolder.Photo);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
