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
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DisplayImageAdapter extends RecyclerView.Adapter<DisplayImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<InspectionImage> images;
    private final int numberOfImages;

    public DisplayImageAdapter(Context context, List<InspectionImage> images) {
        this.context = context;
        this.images = images;
        numberOfImages = images.size();
    }

    @Override
    public int getItemCount() {
        return numberOfImages;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View inspection = LayoutInflater.from(context).inflate(R.layout.element_display_image, viewGroup, false);
        return new ImageViewHolder(inspection);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        // get the inspection image pertaining to this position
        InspectionImage inspectionImage = images.get(i);

        // create url for this inspection image
        String url = Params.CLOUD_URL + inspectionImage.path + ".jpg";

        // get dimensions
        int width = Utilities.getScreenWidth(context);
        int height = Utilities.getScreenHeight(context);

        // render image with Picasso
        Picasso.with(context).load(url).resize(width, height).centerInside().into(imageViewHolder.image);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        final ImageView image;

        ImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}