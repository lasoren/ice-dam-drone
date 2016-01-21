package com.example.tberroa.girodicerapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class PreviousMissionsActivity extends BaseActivity {

    private BroadcastReceiver receiver;
    private ProgressDialog progressDialog;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_missions);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Previous Missions");
        setSupportActionBar(toolbar);

        RecyclerView previousMissionsRecyclerView = (RecyclerView)findViewById(R.id.previous_missions_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        previousMissionsRecyclerView.setLayoutManager(linearLayoutManager);

        // grab username
        UserInfo userInfo = new UserInfo();
        username = userInfo.getUsername(this.getApplicationContext());

        // setup and register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("SOME_ACTION");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // done fetching data, dismiss dialog and reload activity
                progressDialog.dismiss();
                finish();
                startActivity(getIntent());
            }
        };
        registerReceiver(receiver, filter);

        // check if bucket metadata has been fetched already
        BucketInfo bucketInfo = new BucketInfo();
        int numberOfMissions = bucketInfo.getNumOfMissions(this.getApplicationContext());
        if (numberOfMissions > 0){ //  if metadata has been fetched
            ArrayList<Mission> missions = Utilities.getMissions(this.getApplicationContext());
            // populate recycler view
            PreviousMissionsViewAdapter adapter = new PreviousMissionsViewAdapter(this, missions);
            previousMissionsRecyclerView.setAdapter(adapter);
        }
        else{ // if metadata has not been fetched
            if (bucketInfo.isFetching(this.getApplicationContext())){ // currently fetching, show loading dialog
                progressDialog =  Utilities.progressDialog(this, getString(R.string.fetching_data));
                progressDialog.show();
            }
            else { // begin fetching metadata
                Utilities.fetchMetaData(this, username);
                progressDialog =  Utilities.progressDialog(this, getString(R.string.fetching_data));
                progressDialog.show();
            }
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
        unregisterReceiver(receiver);
    }
}
