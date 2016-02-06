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
import com.example.tberroa.girodicerapp.data.ActiveMissionInfo;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.data.UserInfo;

public class ActiveMissionActivity extends BaseActivity {

    private String username;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mission);

        // grab username
        username = new UserInfo().getUsername(this);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Mission");
        setSupportActionBar(toolbar);

        // initialize views and progress bars
        final TextView noActiveMissionText = (TextView) findViewById(R.id.no_active_mission_text);
        final TextView activeMissionText = (TextView) findViewById(R.id.active_mission_text);
        final TextView transferPhaseText = (TextView) findViewById(R.id.transfer_phase_text);
        final TextView uploadPhaseText = (TextView) findViewById(R.id.upload_phase_text);
        final ProgressBar loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        noActiveMissionText.setVisibility(View.GONE);
        activeMissionText.setVisibility(View.GONE);
        transferPhaseText.setVisibility(View.GONE);
        uploadPhaseText.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);

        // set up receiver to reload activity as services complete
        IntentFilter filter = new IntentFilter();
        filter.addAction(Params.RELOAD_ACTIVE_MISSION_ACTIVITY);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startActivity(getIntent());
                finish();
            }
        };
        registerReceiver(broadcastReceiver, filter);

        // populate view according to mission phase
        int missionPhase = new ActiveMissionInfo().getMissionPhase(this);
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
