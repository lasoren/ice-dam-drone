package com.example.tberroa.girodicerapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreviousMissionsViewAdapter extends RecyclerView.Adapter<PreviousMissionsViewAdapter.MissionViewHolder> {
    Context context;
    ArrayList<Mission> missions;

    // constructor
    PreviousMissionsViewAdapter(Context act_context, ArrayList<Mission> missions) {
        context = act_context;
        this.missions = missions;
    }

    public class MissionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView missionPhoto;
        TextView missionNum;

        MissionViewHolder(View itemView) {
            super(itemView);
            missionPhoto = (ImageView) itemView.findViewById(R.id.mission_photo);
            missionNum = (TextView) itemView.findViewById(R.id.mission_num);
            missionPhoto.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // extract clicked mission
            int i = getLayoutPosition();
            Mission mission = missions.get(i);
            // pack mission into json
            Gson gson = new Gson();
            Type singleMission = new TypeToken<Mission>() {
            }.getType();
            String jsonMission = gson.toJson(mission, singleMission);
            // start mission activity, send mission json and mission number
            Intent missionIntent = new Intent(v.getContext(), MissionActivity.class);
            missionIntent.putExtra("mission", jsonMission);
            missionIntent.putExtra("missionNumber", i + 1);
            v.getContext().startActivity(missionIntent);
        }
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    @Override
    public MissionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View allMissions = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout, viewGroup, false);
        return new MissionViewHolder(allMissions);
    }

    @Override
    public void onBindViewHolder(MissionViewHolder allMissionsVH, int i) {
        // set text
        allMissionsVH.missionNum.setText("Mission " + Integer.toString(i + 1));

        // set image with Picasso
        String url = "http://s3.amazonaws.com/missionphotos/Flight+" + Integer.toString(i + 1) + "/Aerial/aerial1.jpg";
        Picasso.with(context)
                .load(url)
                .resize(200, 200)
                .into(allMissionsVH.missionPhoto);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}