package com.example.tberroa.girodicerapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.InspectionActivity;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;

public class PastInspectionsViewAdapter extends RecyclerView.Adapter<PastInspectionsViewAdapter.InspectionViewHolder> {

    private final Context context;
    private final List<Inspection> inspections;

    public PastInspectionsViewAdapter(Context context, List<Inspection> inspections) {
        this.context = context;
        this.inspections = inspections;
    }

    public class InspectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imageThumbnail;
        final TextView inspectionNumber;

        InspectionViewHolder(View itemView) {
            super(itemView);
            imageThumbnail = (ImageView) itemView.findViewById(R.id.inspection_thumbnail);
            inspectionNumber = (TextView) itemView.findViewById(R.id.inspection_number);
            imageThumbnail.setOnClickListener(this);
            inspectionNumber.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            // extract clicked inspection
            int i = getLayoutPosition();
            Inspection inspection = inspections.get(i);

            // pack inspection into JSON
            Type singleInspection = new TypeToken<Inspection>() {}.getType();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String inspectionJson = gson.toJson(inspection, singleInspection);

            // start inspection activity, send inspection JSON and inspection number
            Intent inspectionIntent = new Intent(v.getContext(), InspectionActivity.class);
            inspectionIntent.putExtra("inspection_json", inspectionJson);
            inspectionIntent.putExtra("inspection_number", i + 1);
            context.startActivity(inspectionIntent);
            if(context instanceof Activity){
                ((Activity)context).finish();
            }
        }
    }

    @Override
    public int getItemCount() {
        return inspections.size();
    }

    @Override
    public InspectionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context c = viewGroup.getContext();
        View v = LayoutInflater.from(c).inflate(R.layout.element_inspection_thumbnail, viewGroup, false);
        return new InspectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InspectionViewHolder inspectionViewHolder, int i) {

        // set inspection number text
        String title = "Inspection " + Integer.toString(i + 1);
        inspectionViewHolder.inspectionNumber.setText(title);

        // get thumbnail url
        String url = new LocalDB().getInspectionImages(inspections.get(i), "aerial").get(0).link;

        // render thumbnail with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(inspectionViewHolder.imageThumbnail);
    }
}