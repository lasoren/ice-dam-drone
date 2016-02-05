package com.example.tberroa.girodicerapp.activities;

import android.app.DownloadManager;
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
import com.example.tberroa.girodicerapp.data.MissionStatus;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class ActiveMissionActivity extends BaseActivity {

    private String username;
    private BroadcastReceiver receiverMissionComplete;
    private BroadcastReceiver receiverImageTransferComplete;
    private BroadcastReceiver receiverDownloadComplete;
    private BroadcastReceiver receiverImageUploadComplete;
    public static int numberOfDownloads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mission);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Mission");
        setSupportActionBar(toolbar);

        // initialize loading text views and spinner
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

        // grab username
        username = new UserInfo().getUsername(this);

        // populate view according to mission phase
        int missionPhase = new MissionStatus().getMissionPhase(this);
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
            case 3:
                uploadPhaseText.setVisibility(View.VISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        // setup and register receiver for mission service
        final IntentFilter filterMissionComplete = new IntentFilter();
        filterMissionComplete.addAction("MISSION_COMPLETE");
        receiverMissionComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Utilities.startImageTransfer(ActiveMissionActivity.this);
                // reload activity
                startActivity(getIntent());
                finish();
            }
        };
        registerReceiver(receiverMissionComplete, filterMissionComplete);

        // setup and register receiver for image transfer service
        final IntentFilter filterTransferComplete = new IntentFilter();
        filterTransferComplete.addAction("TRANSFER_COMPLETE");
        receiverImageTransferComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // reload activity
                startActivity(getIntent());
                finish();
            }
        };
        registerReceiver(receiverImageTransferComplete, filterTransferComplete);

        // setup and register receiver for download manager (comes from transfer service)
        final IntentFilter filterDownloadComplete = new IntentFilter();
        filterDownloadComplete.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                numberOfDownloads++;
                if (numberOfDownloads == 20){
                    Utilities.startImageUpload(ActiveMissionActivity.this);
                }
            }
        };
        registerReceiver(receiverDownloadComplete, filterDownloadComplete);

        // setup and register receiver for image upload service
        final IntentFilter filterUploadComplete = new IntentFilter();
        filterUploadComplete.addAction("UPLOAD_COMPLETE");
        receiverImageUploadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // reload activity
                startActivity(getIntent());
                finish();
            }
        };
        registerReceiver(receiverImageUploadComplete, filterUploadComplete);
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
        unregisterReceiver(receiverMissionComplete);
        unregisterReceiver(receiverImageTransferComplete);
        unregisterReceiver(receiverDownloadComplete);
        unregisterReceiver(receiverImageUploadComplete);
    }
}
