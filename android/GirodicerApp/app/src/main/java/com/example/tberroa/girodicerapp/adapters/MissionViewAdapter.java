package com.example.tberroa.girodicerapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.squareup.picasso.Picasso;

public class MissionViewAdapter extends RecyclerView.Adapter<MissionViewAdapter.ImageViewHolder> {

    private Context context;
    private int numberOfImages, missionNumber;
    private String username, tab;

    public MissionViewAdapter(Context context, int missionNumber, int numberOfImages,
                              String tab, String username){
        this.context = context;
        this.missionNumber = missionNumber;
        this.numberOfImages = numberOfImages;
        this.tab = tab;
        this.username = username;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        ImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.photo);
        }
    }

    @Override
    public int getItemCount() {
        return numberOfImages;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View mission = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_layout, viewGroup, false);
        return new ImageViewHolder(mission);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {

        // construct image url
        String urlStart = "http://s3.amazonaws.com/girodicer/"+username+"/Mission+"
                +Integer.toString(missionNumber);
        String urlEnd = Integer.toString(i+1)+".jpg";
        String url;
        switch (tab){
            case "aerial":
                url = urlStart+"/Aerial/aerial"+urlEnd;
                break;
            case "thermal":
                url = urlStart+"/Thermal/thermal"+urlEnd;
                break;
            case "iceDam":
                url = urlStart+"/IceDam/iceDam"+urlEnd;
                break;
            case "salt":
                url = urlStart+"/Salt/salt"+urlEnd;
                break;
            default:
                url = urlStart+"/Aerial/aerial"+urlEnd;
        }

        // get screen dimensions
        int screenWidth = Utilities.getScreenWidth(context);
        int screenHeight = Utilities.getScreenHeight(context);

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

        // render image with Picasso
        Picasso.with(context)
                .load(url)
                .resize(imageWidth, imageHeight)
                .into(imageViewHolder.image);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
