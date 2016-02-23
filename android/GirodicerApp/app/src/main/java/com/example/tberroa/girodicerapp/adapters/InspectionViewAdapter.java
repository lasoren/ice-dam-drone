package com.example.tberroa.girodicerapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.squareup.picasso.Picasso;

public class InspectionViewAdapter extends RecyclerView.Adapter<InspectionViewAdapter.ImageViewHolder> {

    private final Context context;
    private final int numberOfImages;
    private final int missionNumber;
    private final String username;
    private final String tab;

    public InspectionViewAdapter(Context c, int missionNum, int numOfImages, String tab, String user){
        context = c;
        missionNumber = missionNum;
        numberOfImages = numOfImages;
        this.tab = tab;
        username = user;
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
        Context c = viewGroup.getContext();
        View mission = LayoutInflater.from(c).inflate(R.layout.element_tab_images, viewGroup, false);
        return new ImageViewHolder(mission);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        // construct image url
        String imageName;
        switch (tab){
            case Params.AERIAL_TAB:
                imageName = "aerial"+Integer.toString(i+1)+".jpg";
                break;
            case Params.THERMAL_TAB:
                imageName = "thermal"+Integer.toString(i+1)+".jpg";
                break;
            case Params.ICEDAM_TAB:
                imageName = "icedam"+Integer.toString(i+1)+".jpg";
                break;
            case Params.SALT_TAB:
                imageName = "salt"+Integer.toString(i+1)+".jpg";
                break;
            default:
                imageName = "aerial"+Integer.toString(i+1)+".jpg";
        }
        String url = Utilities.ConstructImageURL(username, missionNumber, imageName);

        // render image with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(imageViewHolder.image);
    }
}
