package com.example.tberroa.girodicerapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreviousMissionsActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;
    private Context app_context;
    private ProgressDialog progress;
    private ArrayList<Mission> missions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_missions);

        // set context
        app_context = this.getApplicationContext();

        // set toolbar
        Toolbar previousMissionsToolbar = (Toolbar) findViewById(R.id.previous_missions_toolbar);
        previousMissionsToolbar.setTitle("Previous Missions");
        setSupportActionBar(previousMissionsToolbar);

        // initialize recycler view
        RecyclerView previousMissionsRecyclerView = (RecyclerView)findViewById(R.id.previous_missions_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        previousMissionsRecyclerView.setLayoutManager(linearLayoutManager);

        // get users username
        String username = "missionphotos"; // hard code this for now

        // setup and register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("SOME_ACTION");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // done fetching data, dismiss dialog and reload activity
                progress.dismiss();
                finish();
                startActivity(getIntent());
            }
        };
        registerReceiver(receiver, filter);

        // check if bucket metadata has been fetched already
        BucketInfo bucketInfo = new BucketInfo(app_context);
        int number_of_missions = bucketInfo.getNumOfMissions(app_context);
        if (number_of_missions > 0){ //  if metadata has been fetched
            // grab list of missions
            String jsonString = bucketInfo.getMissions(this.getApplicationContext());
            Gson gson = new Gson();
            Type listOfMissions = new TypeToken<ArrayList<Mission>>(){}.getType();
            missions = gson.fromJson(jsonString, listOfMissions);
            // populate recyclerView
            CustomAdapter adapter = new CustomAdapter(this, missions);
            previousMissionsRecyclerView.setAdapter(adapter);
        }
        else{   // if metadata has not been fetched
            // check if the data is currently being fetched
            if (bucketInfo.getFetching(app_context)){ // if still fetching show loading dialog
                progress = new ProgressDialog(this);
                progress.setTitle("Fetching data from the cloud");
                progress.show();
            }
            else {
                // begin AmazonS3 intent service in order to fetch metadata
                Intent intent = new Intent(PreviousMissionsActivity.this, AmazonS3IntentService.class);
                intent.putExtra("username", username);
                startService(intent);
                // display loading dialog while the metadata is being fetched
                progress = new ProgressDialog(this);
                progress.setTitle("Fetching data from the cloud");
                progress.show();
            }
        }
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    // implement navigation functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_mission:
                // user chose the "End Mission" item
                ServiceStatus service_status = new ServiceStatus(app_context);
                if (!service_status.getServiceStatus()) {   // if there is no mission in progress
                    // tell user there is no mission in progress
                    AlertDialog no_active_mission = no_active_mission_createDialog();
                    no_active_mission.show();
                }
                else{   // otherwise, there is a mission in progress
                    // ask user for confirmation to end mission
                    AlertDialog confirm_end_mission = confirm_end_mission_createDialog();
                    confirm_end_mission.show();
                }
                return true;
            case R.id.current_mission:
                // user chose the "Current Mission" item, change activity
                Intent current_mission = new Intent(PreviousMissionsActivity.this,ActiveMissionActivity.class);
                startActivity(current_mission);
                return true;
            default:
                // the users action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog no_active_mission_createDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.no_active_mission)
                .setCancelable(true)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return alertDialogBuilder.create();
    }

    private AlertDialog confirm_end_mission_createDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.confirm_end_mission)
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService(new Intent(PreviousMissionsActivity.this, AdvertiseService.class));
                    }
                });
        return alertDialogBuilder.create();
    }

    // RecyclerView adapter
    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MissionViewHolder> {
        Context context;
        ArrayList<Mission> missions;

        // constructor
        CustomAdapter(Context act_context, ArrayList<Mission> missions){
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
                Type singleMission = new TypeToken<Mission>(){}.getType();
                String jsonMission = gson.toJson(mission, singleMission);
                // start mission activity, send mission json and mission number
                Intent missionIntent = new Intent(PreviousMissionsActivity.this,MissionActivity.class);
                missionIntent.putExtra("mission", jsonMission);
                missionIntent.putExtra("mission_num", i+1);
                startActivity(missionIntent);
            }
        }

        @Override
        public int getItemCount() {
            return missions.size();
        }

        @Override
        public MissionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View allMissions = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout, viewGroup, false);
            MissionViewHolder allMissionsVH = new MissionViewHolder(allMissions);
            return allMissionsVH;
        }

        @Override
        public void onBindViewHolder(MissionViewHolder allMissionsVH, int i) {
            // set text
            allMissionsVH.missionNum.setText("Mission "+Integer.toString(i+1));

            // set image with Picasso
            String url = "http://s3.amazonaws.com/missionphotos/Flight+"+Integer.toString(i+1)+"/Aerial/aerial1.jpg";
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
