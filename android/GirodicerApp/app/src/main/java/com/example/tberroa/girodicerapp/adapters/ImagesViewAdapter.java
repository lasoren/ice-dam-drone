package com.example.tberroa.girodicerapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.example.tberroa.girodicerapp.models.InspectionImage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

public class ImagesViewAdapter extends RecyclerView.Adapter<ImagesViewAdapter.ImageViewHolder> {

    private final Context context;
    private final List<InspectionImage> inspectionImages;
    private final int numberOfImages;

    public ImagesViewAdapter(Context context, String inspectionJson, String tab){
        this.context = context;

        // deserialize the inspection
        Type singleInspection = new TypeToken<Inspection>(){}.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Inspection inspection = gson.fromJson(inspectionJson, singleInspection);

        // get all inspection images of type "tab"
        inspectionImages = new LocalDB().getInspectionImages(inspection, tab);

        // get number of images
        numberOfImages = inspectionImages.size();
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
        Context context = viewGroup.getContext();
        View inspection = LayoutInflater.from(context).inflate(R.layout.element_tab_images, viewGroup, false);
        return new ImageViewHolder(inspection);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        // get the inspection image pertaining to this position
        InspectionImage inspectionImage = inspectionImages.get(i);

        // get the url for this inspection image
        String url = inspectionImage.link;

        // render image with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(imageViewHolder.image);
    }
}
