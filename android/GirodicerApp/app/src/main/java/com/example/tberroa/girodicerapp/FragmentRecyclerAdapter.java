package com.example.tberroa.girodicerapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

// RecyclerView adapter
public class FragmentRecyclerAdapter extends RecyclerView.Adapter<FragmentRecyclerAdapter.ImageViewHolder> {

    Context context;
    int num_of_images;
    int mission_num;

    // constructor
    FragmentRecyclerAdapter(Context act_context, int mission_num, int num_of_images){
        context = act_context;
        this.mission_num = mission_num;
        this.num_of_images = num_of_images;
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
        return num_of_images;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View mission = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_layout, viewGroup, false);
        ImageViewHolder missionVH = new ImageViewHolder(mission);
        return missionVH;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder missionVH, int i) {
        // construct image url
        String url = "http://s3.amazonaws.com/missionphotos/Flight+"+Integer.toString(mission_num)+"/Aerial/aerial"+Integer.toString(i+1)+".jpg";
        // get screen dimensions
        int screenWidth = new Utilities().getScreenWidth(context);
        int screenHeight = new Utilities().getScreenWidth(context);
        // set image width and height
        int imageWidth;
        int imageHeight;
        if (screenWidth > screenHeight){    // landscape
            imageWidth = screenWidth / 4;
            imageHeight = screenHeight / 2;
        }
        else{   // vertical
            imageWidth = screenWidth / 2;
            imageHeight = screenHeight / 4;
        }
        // set image with Picasso
        Picasso.with(context)
                .load(url)
                .resize(imageWidth, imageHeight)
                .into(missionVH.Photo);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
