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
import com.example.tberroa.girodicerapp.data.InspectionId;
import com.example.tberroa.girodicerapp.database.LocalDB;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.example.tberroa.girodicerapp.models.Inspection;
import com.squareup.picasso.Picasso;

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
            imageThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            inspectionNumber = (TextView) itemView.findViewById(R.id.label);
            imageThumbnail.setOnClickListener(this);
            inspectionNumber.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            // extract clicked inspection
            int i = getLayoutPosition();
            Inspection inspection = inspections.get(i);

            // save inspection id
            new InspectionId().set(context, inspection.id);

            // start inspection activity
            context.startActivity(new Intent(v.getContext(), InspectionActivity.class));
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
        View v = LayoutInflater.from(c).inflate(R.layout.element_thumbnail, viewGroup, false);
        return new InspectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InspectionViewHolder inspectionViewHolder, int i) {

        // set inspection number text
        String title = inspections.get(i).created;
        inspectionViewHolder.inspectionNumber.setText(title);

        // get thumbnail url
        String url = new LocalDB().getInspectionImages(inspections.get(i), "aerial").get(0).link;

        // render thumbnail with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(inspectionViewHolder.imageThumbnail);
    }
}