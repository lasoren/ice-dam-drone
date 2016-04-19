package com.example.tberroa.girodicerapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.dialogs.DisplayImageDialog;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

public class InspectionImagesAdapter extends RecyclerView.Adapter<InspectionImagesAdapter.ImageViewHolder> {

    private final Context context;
    private final List<InspectionImage> inspectionImages;
    private final int numberOfImages;

    public InspectionImagesAdapter(Context context, String inspectionImagesJson) {
        this.context = context;

        // deserialize the inspection images
        Type inspectionImagesList = new TypeToken<List<InspectionImage>>() {
        }.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        inspectionImages = gson.fromJson(inspectionImagesJson, inspectionImagesList);

        // get number of images
        numberOfImages = this.inspectionImages.size();
    }

    @Override
    public int getItemCount() {
        return numberOfImages;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View inspection = LayoutInflater.from(context).inflate(R.layout.element_tab_images, viewGroup, false);
        return new ImageViewHolder(inspection);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        // get the inspection image pertaining to this position
        InspectionImage inspectionImage = inspectionImages.get(i);

        // get the url for this inspection image
        String url = Params.CLOUD_URL + inspectionImage.path + ".jpg";

        // render image with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(imageViewHolder.image);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        final ImageView image;

        ImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.photo);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get position
                    int i = getLayoutPosition();

                    // show image full screen in dialog
                    new DisplayImageDialog(v.getContext(), inspectionImages, i).show();
                }
            });
        }
    }
}
