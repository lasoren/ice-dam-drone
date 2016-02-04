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
import android.widget.Toast;

import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.MissionStatus;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

public class ActiveMissionActivity extends BaseActivity {

    private String username;
    private BroadcastReceiver receiverMissionComplete;
    private BroadcastReceiver receiverImageTransferComplete;
    private BroadcastReceiver receiverImageUploadComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_mission);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Current Mission");
        setSupportActionBar(toolbar);

        // grab username
        username = new UserInfo().getUsername(this);

        // setup and register receiver for mission service
        final IntentFilter filterMissionComplete = new IntentFilter();
        filterMissionComplete.addAction("MISSION_COMPLETE");
        receiverMissionComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(ActiveMissionActivity.this, "mission completed",
                        Toast.LENGTH_LONG).show();
                Utilities.startImageTransfer(ActiveMissionActivity.this);
            }
        };
        registerReceiver(receiverMissionComplete, filterMissionComplete);

        // setup and register receiver for image transfer service
        final IntentFilter filterTransferComplete = new IntentFilter();
        filterTransferComplete.addAction("TRANSFER_COMPLETE");
        receiverImageTransferComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(ActiveMissionActivity.this, "transfer completed",
                        Toast.LENGTH_LONG).show();
                Bundle bundle = intent.getExtras();
                Utilities.startImageUpload(ActiveMissionActivity.this, bundle);
            }
        };
        registerReceiver(receiverImageTransferComplete, filterTransferComplete);

        // setup and register receiver for image upload service
        final IntentFilter filterUploadComplete = new IntentFilter();
        filterUploadComplete.addAction("UPLOAD_COMPLETE");
        receiverImageUploadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(ActiveMissionActivity.this, "upload completed",
                        Toast.LENGTH_LONG).show();
                // mission program is now completely over
                new MissionStatus().setMissionStatus(ActiveMissionActivity.this, false);
                // previous missions info is out of date
                new PreviousMissionsInfo().setUpToDate(ActiveMissionActivity.this, false);
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
        unregisterReceiver(receiverImageUploadComplete);
    }
}
