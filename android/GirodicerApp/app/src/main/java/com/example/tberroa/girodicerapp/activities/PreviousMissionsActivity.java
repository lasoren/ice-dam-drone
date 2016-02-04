package com.example.tberroa.girodicerapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.data.PreviousMissionsInfo;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.adapters.PreviousMissionsViewAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.data.UserInfo;
import com.example.tberroa.girodicerapp.helpers.Utilities;

import java.util.ArrayList;

public class PreviousMissionsActivity extends BaseActivity {

    private BroadcastReceiver receiver;
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

        // initialize recycler view
        RecyclerView previousMissionsRecyclerView =
                (RecyclerView)findViewById(R.id.previous_missions_recycler_view);
        int span = Utilities.getSpanGrid(this);
        previousMissionsRecyclerView.setLayoutManager(new GridLayoutManager(this, span));
        previousMissionsRecyclerView.addItemDecoration(new GridSpacingItemDecoration(
                span, Utilities.getSpacingGrid(this), true));

        // initialize loading spinner and text
        final ProgressBar loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        final TextView loadingText = (TextView) findViewById(R.id.loading_text);

        // setup and register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("FETCHING_COMPLETE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // done fetching data, dismiss loading spinner and text
                loadingSpinner.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
                // reload activity
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
                loadingSpinner.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
            }
            else { // begin fetching previous missions data and then show dialog
                Utilities.fetchPreviousMissionsData(this, username);
                loadingSpinner.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
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
