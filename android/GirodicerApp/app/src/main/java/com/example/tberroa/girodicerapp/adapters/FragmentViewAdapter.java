package com.example.tberroa.girodicerapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.database.LocalTestDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.squareup.picasso.Picasso;

public class FragmentViewAdapter extends RecyclerView.Adapter<FragmentViewAdapter.ImageViewHolder> {

    private final Context context;
    private final int numberOfImages;
    private final Inspection inspection;
    private final String tab;

    public FragmentViewAdapter(Context context, Inspection inspection, String tab){
        this.context = context;
        this.inspection = inspection;
        numberOfImages = new LocalTestDB().getNumberOfType(inspection, tab);
        this.tab = tab;
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
        String imageType;
        switch (tab){
            case Params.AERIAL_TAB:
                imageType = "aerial";
                break;
            case Params.THERMAL_TAB:
                imageType = "thermal";
                break;
            case Params.ICEDAM_TAB:
                imageType = "icedam";
                break;
            case Params.SALT_TAB:
                imageType = "salt";
                break;
            default:
                imageType = "aerial";
        }
        String url = new LocalTestDB().getInspectionImages(inspection, imageType).get(i).link;

        // render image with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(imageViewHolder.image);
    }
}
