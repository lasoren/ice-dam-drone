package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.Params;

public class ActiveMissionActivity extends BaseActivity {

    private BroadcastReceiver broadcastReceiver;
    private TextView noActiveMissionText;
    private TextView activeMissionText;
    private TextView transferPhaseText;
    private TextView uploadPhaseText;
    private ProgressBar loadingSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mission);

        // grab username
        username = userInfo.getUsername(this);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Mission");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        // initialize view elements
        noActiveMissionText = (TextView) findViewById(R.id.no_active_mission_text);
        activeMissionText = (TextView) findViewById(R.id.active_mission_text);
        transferPhaseText = (TextView) findViewById(R.id.transfer_phase_text);
        uploadPhaseText = (TextView) findViewById(R.id.upload_phase_text);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // populate view according to mission phase
        int missionPhase = activeMissionInfo.getMissionPhase(this);
        PopulateView(missionPhase);

        // set up receiver to update activity as necessary
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.RELOAD_AM_ACTIVITY);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // update view according to mission phase
                int phase = activeMissionInfo.getMissionPhase(ActiveMissionActivity.this);
                PopulateView(phase);
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    private void PopulateView(int missionPhase){
        noActiveMissionText.setVisibility(View.GONE);
        activeMissionText.setVisibility(View.GONE);
        transferPhaseText.setVisibility(View.GONE);
        uploadPhaseText.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        switch (missionPhase) {
            case 0:
                noActiveMissionText.setVisibility(View.VISIBLE);

                break;
            case 1:
                activeMissionText.setVisibility(View.VISIBLE);
                break;
            case 2:
                transferPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            case 5:
                transferPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            case 3:
                uploadPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: "+username);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
