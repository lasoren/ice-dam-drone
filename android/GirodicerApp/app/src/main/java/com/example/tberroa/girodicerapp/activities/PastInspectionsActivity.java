package com.example.tberroa.girodicerapp.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tberroa.girodicerapp.helpers.DBManager;
import com.example.tberroa.girodicerapp.helpers.GridSpacingItemDecoration;
import com.example.tberroa.girodicerapp.data.Mission;
import com.example.tberroa.girodicerapp.adapters.PIViewAdapter;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;


import java.util.ArrayList;

public class PastInspectionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_inspections);

        // grab operatorName
        int operatorId = operatorInfo.getId(this);
        Log.d("test1", "operator id = " + Integer.toString(operatorId));
        operatorName = DBManager.getOperatorName(operatorId);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Previous Missions");
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        // initialize recycler view
        int span = Utilities.getSpanGrid(this);
        int spacing = Utilities.getSpacingGrid(this);
        final RecyclerView pMRView = (RecyclerView)findViewById(R.id.previous_missions_recycler_view);
        pMRView.setLayoutManager(new GridLayoutManager(this, span));
        pMRView.addItemDecoration(new GridSpacingItemDecoration(span, spacing));

        // grab data of all previous missions
        ArrayList<Mission> missions = Utilities.getMissions(this);
        // use that data to populate the recycler view
        PIViewAdapter pMVAdapter;
        pMVAdapter = new PIViewAdapter(this, operatorName, missions);
        pMRView.setAdapter(pMVAdapter);
    }

    // populate the navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        MenuItem item = menu.findItem(R.id.title);
        item.setTitle("Logged in as: " + operatorName);
        return true;
    }

}
