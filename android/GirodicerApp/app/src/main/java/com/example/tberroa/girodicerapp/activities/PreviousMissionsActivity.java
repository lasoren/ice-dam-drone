package com.example.tberroa.girodicerapp.activities;

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

import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.adapters.PreviousMissionsViewAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

import java.util.ArrayList;

public class PreviousMissionsActivity extends BaseActivity {

    private BroadcastReceiver receiver;
    private ProgressDialog progressDialog;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_missions);

        // grab username
        username = new UserInfo().getUsername(this);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Previous Missions");
        setSupportActionBar(toolbar);

        // set recycler view
        RecyclerView previousMissionsRecyclerView =
                (RecyclerView)findViewById(R.id.previous_missions_recycler_view);
        previousMissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // set progress dialog
        progressDialog =  new ProgressDialog(this);
        progressDialog.setTitle(R.string.fetching_data);

        // setup and register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("FETCHING_COMPLETE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // done fetching data, dismiss dialog and reload activity
                progressDialog.dismiss();
                startActivity(getIntent());
                finish();
            }
        };
        registerReceiver(receiver, filter);

        // check if previous missions data is up to date
        Boolean isUpToDate = new PreviousMissionsInfo().isUpToDate(this);
        if (isUpToDate){
            // grab data of all previous missions
            ArrayList<Mission> missions = Utilities.getMissions(this);
            // use that data to populate the recycler view
            PreviousMissionsViewAdapter previousMissionsViewAdapter =
                    new PreviousMissionsViewAdapter(this, username, missions);
            previousMissionsRecyclerView.setAdapter(previousMissionsViewAdapter);
        }
        else{ // if previous missions data is not up to date
            if (new PreviousMissionsInfo().isFetching(this)){ // only show dialog
                progressDialog.show();
            }
            else { // begin fetching previous missions data and then show dialog
                Utilities.fetchPreviousMissionsData(this, username);
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
