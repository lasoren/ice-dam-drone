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

    private final Context context;
    private final int numberOfImages;
    private final int missionNumber;
    private final String username;
    private final String tab;

    public MissionViewAdapter(Context context, int missionNumber, int numberOfImages,
                              String tab, String username){
        this.context = context;
        this.missionNumber = missionNumber;
        this.numberOfImages = numberOfImages;
        this.tab = tab;
        this.username = username;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        final ImageView image;
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
                .inflate(R.layout.tab_images, viewGroup, false);
        return new ImageViewHolder(mission);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {

        // construct image url
        String x = Integer.toString(missionNumber);
        String urlStart = "http://s3.amazonaws.com/girodicer/"+username+"/mission"+x+"/";
        String urlEnd = Integer.toString(i+1)+".jpg";
        String url;
        switch (tab){
            case "aerial":
                url = urlStart+"aerial/aerial"+urlEnd;
                break;
            case "thermal":
                url = urlStart+"thermal/thermal"+urlEnd;
                break;
            case "iceDam":
                url = urlStart+"icedam/icedam"+urlEnd;
                break;
            case "salt":
                url = urlStart+"salt/salt"+urlEnd;
                break;
            default:
                url = urlStart+"aerial/aerial"+urlEnd;
        }

        // render image with Picasso
        Picasso.with(context)
                .load(url)
                .resize(Utilities.getImageWidthGrid(context), Utilities.getImageHeightGrid(context))
                .into(imageViewHolder.image);
    }
}
