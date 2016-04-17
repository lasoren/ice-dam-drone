package com.example.tberroa.girodicerapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.InspectionActivity;
import com.example.tberroa.girodicerapp.data.InspectionId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PastInspectionsAdapter extends RecyclerView.Adapter<PastInspectionsAdapter.InspectionViewHolder> {

    private final Context context;
    private final List<Integer> ids;
    private final List<String> paths;
    private final List<String> labels;

    public PastInspectionsAdapter(Context context, List<Integer> ids, List<String> paths, List<String> labels) {
        this.context = context;
        this.ids = ids;
        this.paths = paths;
        this.labels = labels;
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    @Override
    public InspectionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context c = viewGroup.getContext();
        View v = LayoutInflater.from(c).inflate(R.layout.element_thumbnail, viewGroup, false);
        return new InspectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InspectionViewHolder inspectionViewHolder, int i) {
        // render thumbnail with Picasso
        String url = Params.CLOUD_URL + paths.get(i);
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(inspectionViewHolder.inspectionThumbnail);

        // set label
        inspectionViewHolder.inspectionDate.setText(labels.get(i));
    }

    public class InspectionViewHolder extends RecyclerView.ViewHolder {

        final ImageView inspectionThumbnail;
        final TextView inspectionDate;
        final RelativeLayout thumbnailLayout;

        InspectionViewHolder(View itemView) {
            super(itemView);
            inspectionThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            inspectionDate = (TextView) itemView.findViewById(R.id.label);
            thumbnailLayout = (RelativeLayout) itemView.findViewById(R.id.thumbnail_layout);
            thumbnailLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // extract clicked inspection
                    int i = getLayoutPosition();

                    // save inspection id
                    new InspectionId().set(context, ids.get(i));

                    // start inspection activity
                    context.startActivity(new Intent(v.getContext(), InspectionActivity.class));
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }
            });
        }
    }
}