package com.example.tberroa.girodicerapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.activities.MissionActivity;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PMViewAdapter extends RecyclerView.Adapter<PMViewAdapter.MissionViewHolder> {

    private final Context context;
    private final ArrayList<Mission> missions;
    private final String username;

    public PMViewAdapter(Context c, String username, ArrayList<Mission> missions) {
        context = c;
        this.missions = missions;
        this.username = username;
    }

    public class MissionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView missionThumbnail;
        final TextView missionNumber;

        MissionViewHolder(View itemView) {
            super(itemView);
            missionThumbnail = (ImageView) itemView.findViewById(R.id.mission_thumbnail);
            missionNumber = (TextView) itemView.findViewById(R.id.mission_number);
            missionThumbnail.setOnClickListener(this);
            missionNumber.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            // extract clicked mission
            int i = getLayoutPosition();
            Mission mission = missions.get(i);

            // pack mission into JSON
            Type singleMission = new TypeToken<Mission>() {}.getType();
            String jsonMission = new Gson().toJson(mission, singleMission);

            // start mission activity, send mission JSON and mission number
            Intent missionIntent = new Intent(v.getContext(), MissionActivity.class);
            missionIntent.putExtra("mission", jsonMission);
            missionIntent.putExtra("mission_number", i + 1);
            v.getContext().startActivity(missionIntent);
        }
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    @Override
    public MissionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context c = viewGroup.getContext();
        View v = LayoutInflater.from(c).inflate(R.layout.mission_thumbnails, viewGroup, false);
        return new MissionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MissionViewHolder allMissionsVH, int i) {

        // set toolbar text
        String title = "Mission " + Integer.toString(i + 1);
        allMissionsVH.missionNumber.setText(title);

        // build thumbnail url
        String url = Utilities.ConstructImageURL(username, i + 1, "aerial1.jpg");

        // render thumbnail with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(allMissionsVH.missionThumbnail);
    }
}